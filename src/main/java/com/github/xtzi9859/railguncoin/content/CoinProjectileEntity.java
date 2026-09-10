package com.github.xtzi9859.railguncoin.content;

import blusunrize.immersiveengineering.common.entities.IEProjectileEntity;
import com.github.xtzi9859.railguncoin.registry.ModEntityTypes;
import com.github.xtzi9859.railguncoin.registry.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@SuppressWarnings("resource")
public final class CoinProjectileEntity extends IEProjectileEntity {
    private static final EntityDataAccessor<Vector3f> DATA_RENDER_DIRECTION = SynchedEntityData.defineId(
            CoinProjectileEntity.class, EntityDataSerializers.VECTOR3
    );
    private static final EntityDataAccessor<Boolean> DATA_CHARGED = SynchedEntityData.defineId(
            CoinProjectileEntity.class, EntityDataSerializers.BOOLEAN
    );
    private static final double TRAIL_SPACING = 1.0D;
    private static final float FLIGHT_SPEED = 8.0F;
    private static final float NORMAL_DIRECT_HIT_DAMAGE = 24.0F;
    private static final float CHARGED_DIRECT_HIT_DAMAGE = 42.0F;
    private static final float NORMAL_EXPLOSION_POWER = 4.0F;
    private static final float CHARGED_EXPLOSION_POWER = 7.0F;
    private static final float CHARGED_LIGHTNING_DAMAGE = 5.0F;
    private static final int CHARGED_EXPLOSION_LIGHTNING_COUNT = 3;
    private static final int CHARGED_BLOCK_HIT_LIGHTNING_COUNT = 10;
    private static final double LIGHTNING_DAMAGE_RADIUS = 3.0D;
    private static final float EXPLOSION_FIRE_SECONDS = 5.0F;

    @Nullable
    private UUID targetUuid;
    private double distanceToNextRing = TRAIL_SPACING;

    public CoinProjectileEntity(EntityType<? extends CoinProjectileEntity> type, Level level) {
        super(type, level);
        setTickLimit(80);
    }

    public CoinProjectileEntity(
            Level level, LivingEntity shooter, @Nullable LivingEntity target, boolean charged
    ) {
        super(ModEntityTypes.COIN_PROJECTILE.get(), level, shooter, FLIGHT_SPEED, 0.0F);
        this.targetUuid = target == null ? null : target.getUUID();
        entityData.set(DATA_CHARGED, charged);
        syncRenderDirection(getDeltaMovement());
        setTickLimit(80);
    }

    @Override
    protected void defineSynchedData(@Nonnull SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_RENDER_DIRECTION, new Vector3f(0.0F, 0.0F, 1.0F));
        builder.define(DATA_CHARGED, false);
    }

    @Override
    public void tick() {
        Vec3 start = position();
        if (level() instanceof ServerLevel serverLevel) {
            EntityHitResult guaranteedHit = steerTowardTarget();
            syncRenderDirection(getDeltaMovement());
            if (guaranteedHit != null) {
                Vec3 hitLocation = guaranteedHit.getLocation();
                setPos(hitLocation.x, hitLocation.y, hitLocation.z);
                emitTrail(serverLevel, start, hitLocation);
                onHitEntity(guaranteedHit);
                return;
            }
        }
        super.tick();
        if (level() instanceof ServerLevel serverLevel) {
            emitTrail(serverLevel, start, position());
        }
    }

    public Vec3 getRenderDirection() {
        Vector3f direction = entityData.get(DATA_RENDER_DIRECTION);
        return new Vec3(direction.x, direction.y, direction.z);
    }

    private void syncRenderDirection(Vec3 movement) {
        if (movement.lengthSqr() < 0.0001D) {
            return;
        }
        Vec3 direction = movement.normalize();
        entityData.set(DATA_RENDER_DIRECTION, new Vector3f(
                (float)direction.x, (float)direction.y, (float)direction.z
        ));
    }

    @Nullable
    private EntityHitResult steerTowardTarget() {
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            return null;
        }
        Vec3 current = getDeltaMovement();
        Vec3 aimPoint = LockOnTargeting.getAimPoint(target);
        Vec3 toTarget = aimPoint.subtract(position());
        if (current.lengthSqr() < 0.0001D || toTarget.lengthSqr() < 0.0001D || !hasClearPath(aimPoint)) {
            return null;
        }
        double speed = current.length();
        double distance = toTarget.length();
        setDeltaMovement(toTarget.scale(Math.min(speed, distance) / distance));
        Entity hitEntity = target instanceof EnderDragon dragon ? dragon.head : target;
        return distance <= speed ? new EntityHitResult(hitEntity, aimPoint) : null;
    }

    private boolean hasClearPath(Vec3 aimPoint) {
        BlockHitResult obstruction = level().clip(new ClipContext(
                position(), aimPoint, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this
        ));
        return obstruction.getType() == HitResult.Type.MISS;
    }

    @Nullable
    private LivingEntity getTarget() {
        if (targetUuid == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity target = serverLevel.getEntity(targetUuid);
        return target instanceof LivingEntity living ? living : null;
    }

    private void emitTrail(ServerLevel serverLevel, Vec3 start, Vec3 end) {
        Vec3 segment = end.subtract(start);
        double length = segment.length();
        if (length < 0.0001D) {
            return;
        }
        Vec3 direction = segment.scale(1.0D / length);
        double along = distanceToNextRing;
        while (along <= length) {
            Vec3 point = start.add(direction.scale(along));
            sendLongDistanceParticles(
                    serverLevel,
                    isCharged() ? ModParticles.SONIC_RING_CHARGED.get() : ModParticles.SONIC_RING.get(),
                    point,
                    0,
                    direction.x, direction.y, direction.z,
                    1.0D
            );
            along += TRAIL_SPACING;
        }
        distanceToNextRing = along - length;
    }

    public boolean isCharged() {
        return entityData.get(DATA_CHARGED);
    }

    private static <T extends ParticleOptions> void sendLongDistanceParticles(
            ServerLevel serverLevel, T particle, Vec3 position, int count,
            double xOffset, double yOffset, double zOffset, double speed
    ) {
        for (ServerPlayer player : serverLevel.players()) {
            serverLevel.sendParticles(
                    player, particle, true,
                    position.x, position.y, position.z,
                    count, xOffset, yOffset, zOffset, speed
            );
        }
    }

    @Override
    protected void onHitEntity(@Nonnull EntityHitResult result) {
        if (!level().isClientSide) {
            float damage = isCharged() ? CHARGED_DIRECT_HIT_DAMAGE : NORMAL_DIRECT_HIT_DAMAGE;
            result.getEntity().hurt(
                    ModDamageSources.coinImpact(level(), this, getOwner(), isCharged()),
                    damage
            );
            explode(result.getLocation());
        }
        discard();
    }

    @Override
    protected void onHitBlock(@Nonnull BlockHitResult result) {
        if (level() instanceof ServerLevel serverLevel) {
            explode(result.getLocation());
            if (isCharged()) {
                summonBlockHitLightning(serverLevel, result.getLocation(), getOwner());
            }
        }
        discard();
    }

    private void explode(Vec3 position) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        sendLongDistanceParticles(
                serverLevel,
                ParticleTypes.EXPLOSION_EMITTER,
                position,
                1,
                0.0D, 0.0D, 0.0D,
                0.0D
        );
        Entity shooter = getOwner();
        ExplosionDamageCalculator damageCalculator = new ExplosionDamageCalculator() {
            @Override
            public boolean shouldDamageEntity(@Nonnull Explosion explosion, @Nonnull Entity entity) {
                if (entity == shooter) {
                    return false;
                }
                if (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrb)) {
                    entity.igniteForSeconds(EXPLOSION_FIRE_SECONDS);
                }
                if (isCharged() && entity instanceof LivingEntity) {
                    for (int strike = 0; strike < CHARGED_EXPLOSION_LIGHTNING_COUNT; strike++) {
                        summonDamagingLightning(serverLevel, (LivingEntity)entity);
                    }
                }
                return true;
            }
        };
        level().explode(
                this,
                ModDamageSources.coinExplosion(level(), shooter, position),
                damageCalculator,
                position.x, position.y, position.z,
                isCharged() ? CHARGED_EXPLOSION_POWER : NORMAL_EXPLOSION_POWER,
                false,
                Level.ExplosionInteraction.NONE,
                ModParticles.NO_EXPLOSION.get(),
                ModParticles.NO_EXPLOSION.get(),
                SoundEvents.GENERIC_EXPLODE
        );
    }

    private static void summonDamagingLightning(ServerLevel level, LivingEntity target) {
        LightningBolt lightning = createLightning(level, target.position());
        if (lightning == null) {
            return;
        }
        strikeWithLightning(level, lightning, target);
        addVisualLightning(level, lightning);
    }

    private static void summonBlockHitLightning(
            ServerLevel level, Vec3 position, @Nullable Entity shooter
    ) {
        AABB strikeArea = new AABB(
                position.x - LIGHTNING_DAMAGE_RADIUS,
                position.y - LIGHTNING_DAMAGE_RADIUS,
                position.z - LIGHTNING_DAMAGE_RADIUS,
                position.x + LIGHTNING_DAMAGE_RADIUS,
                position.y + LIGHTNING_DAMAGE_RADIUS * 3.0D,
                position.z + LIGHTNING_DAMAGE_RADIUS
        );
        for (int strike = 0; strike < CHARGED_BLOCK_HIT_LIGHTNING_COUNT; strike++) {
            LightningBolt lightning = createLightning(level, position);
            if (lightning == null) {
                continue;
            }
            for (LivingEntity target : level.getEntitiesOfClass(
                    LivingEntity.class,
                    strikeArea,
                    entity -> entity != shooter && entity.isAlive()
            )) {
                strikeWithLightning(level, lightning, target);
            }
            addVisualLightning(level, lightning);
        }
    }

    @Nullable
    private static LightningBolt createLightning(ServerLevel level, Vec3 position) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(position.x, position.y, position.z);
            lightning.setDamage(CHARGED_LIGHTNING_DAMAGE);
        }
        return lightning;
    }

    private static void strikeWithLightning(
            ServerLevel level, LightningBolt lightning, LivingEntity target
    ) {
        target.invulnerableTime = 0;
        target.thunderHit(level, lightning);
    }

    private static void addVisualLightning(ServerLevel level, LightningBolt lightning) {
        lightning.setVisualOnly(true);
        level.addFreshEntity(lightning);
    }

    @Override
    public double getDefaultGravity() {
        return 0.0D;
    }

    @Override
    protected float getMotionDecayFactor() {
        return 1.0F;
    }

    @Nonnull
    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (targetUuid != null) {
            tag.putUUID("Target", targetUuid);
        }
        tag.putBoolean("Charged", isCharged());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        targetUuid = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
        entityData.set(DATA_CHARGED, tag.getBoolean("Charged"));
    }
}
