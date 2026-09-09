package com.github.xtzi9859.railguncoin.content;

import blusunrize.immersiveengineering.common.entities.IEProjectileEntity;
import com.github.xtzi9859.railguncoin.registry.ModEntityTypes;
import com.github.xtzi9859.railguncoin.registry.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@SuppressWarnings("resource")
public final class CoinProjectileEntity extends IEProjectileEntity {
    private static final double TRAIL_SPACING = 1.0D;
    private static final float FLIGHT_SPEED = 8.0F;
    private static final float EXPLOSION_FIRE_SECONDS = 5.0F;

    @Nullable
    private UUID targetUuid;
    private double distanceToNextRing = TRAIL_SPACING;

    public CoinProjectileEntity(EntityType<? extends CoinProjectileEntity> type, Level level) {
        super(type, level);
        setTickLimit(80);
    }

    public CoinProjectileEntity(Level level, LivingEntity shooter, @Nullable LivingEntity target) {
        super(ModEntityTypes.COIN_PROJECTILE.get(), level, shooter, FLIGHT_SPEED, 0.0F);
        this.targetUuid = target == null ? null : target.getUUID();
        setTickLimit(80);
    }

    @Override
    public void tick() {
        Vec3 start = position();
        if (level() instanceof ServerLevel serverLevel) {
            EntityHitResult guaranteedHit = steerTowardTarget();
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
            serverLevel.sendParticles(
                    ModParticles.SONIC_RING.get(),
                    point.x, point.y, point.z,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
            along += TRAIL_SPACING;
        }
        distanceToNextRing = along - length;
    }

    @Override
    protected void onHitEntity(@Nonnull EntityHitResult result) {
        if (!level().isClientSide) {
            result.getEntity().hurt(ModDamageSources.coinImpact(level(), this, getOwner()), 20.0F);
            explode(result.getLocation());
        }
        discard();
    }

    @Override
    protected void onHitBlock(@Nonnull BlockHitResult result) {
        if (!level().isClientSide) {
            explode(result.getLocation());
        }
        discard();
    }

    private void explode(Vec3 position) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    position.x, position.y, position.z,
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
        }
        Entity shooter = getOwner();
        ExplosionDamageCalculator damageCalculator = new ExplosionDamageCalculator() {
            @Override
            public boolean shouldDamageEntity(@Nonnull Explosion explosion, @Nonnull Entity entity) {
                if (entity == shooter) {
                    return false;
                }
                entity.igniteForSeconds(EXPLOSION_FIRE_SECONDS);
                return true;
            }
        };
        level().explode(
                this,
                null,
                damageCalculator,
                position,
                8.0F,
                false,
                Level.ExplosionInteraction.NONE
        );
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        targetUuid = tag.hasUUID("Target") ? tag.getUUID("Target") : null;
    }
}
