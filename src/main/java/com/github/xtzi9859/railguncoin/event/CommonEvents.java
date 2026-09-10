package com.github.xtzi9859.railguncoin.event;

import blusunrize.immersiveengineering.common.items.RailgunItem;
import com.github.xtzi9859.railguncoin.RailgunCoinMod;
import com.github.xtzi9859.railguncoin.content.CoinProjectileEntity;
import com.github.xtzi9859.railguncoin.content.LockOnTargeting;
import com.github.xtzi9859.railguncoin.content.ModDamageSources;
import com.github.xtzi9859.railguncoin.registry.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid = RailgunCoinMod.MOD_ID)
@SuppressWarnings("resource")
public final class CommonEvents {
    private static final int NORMAL_OVERCHARGE_TICKS = 4 * 20;
    private static final int CHARGED_OVERCHARGE_TICKS = 3 * 20;
    private static final int FUSE_WARNING_TICKS = 30;
    private static final float NORMAL_EXPLOSION_POWER = 4.0F;
    private static final float CHARGED_EXPLOSION_POWER = 7.0F;

    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onRailgunUseTick(LivingEntityUseItemEvent.Tick event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack railgun = event.getItem();
        if (!(railgun.getItem() instanceof RailgunItem) || !LockOnTargeting.hasSelectedCoinAmmo(railgun, player)) {
            return;
        }

        ItemStack ammo = RailgunItem.findAmmo(railgun, player);
        boolean charged = LockOnTargeting.isChargedCoin(ammo);
        int overchargeTicks = charged ? CHARGED_OVERCHARGE_TICKS : NORMAL_OVERCHARGE_TICKS;
        int detonationTick = RailgunItem.getChargeTime(railgun) + overchargeTicks;
        int ticksUsing = player.getTicksUsingItem();
        if (ticksUsing == detonationTick - FUSE_WARNING_TICKS) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (ticksUsing >= detonationTick) {
            selfDestruct(player, railgun, charged);
        }
    }

    private static void selfDestruct(ServerPlayer player, ItemStack railgun, boolean charged) {
        var source = ModDamageSources.misfire(
                player.level(),
                player,
                railgun
        );
        ItemStack ammo = RailgunItem.findAmmo(railgun, player);
        if (!ammo.isEmpty()) {
            ammo.shrink(1);
        }
        player.setItemInHand(player.getUsedItemHand(), ItemStack.EMPTY);
        player.stopUsingItem();

        player.hurt(source, Float.MAX_VALUE);
        if (player.isAlive()) {
            player.setHealth(0.0F);
            player.die(source);
        }
        ServerLevel level = player.serverLevel();
        sendExplosionEmitter(level, player);
        if (charged) {
            CoinProjectileEntity.summonSelfDestructLightning(level, player.position(), player);
        }
        level.explode(
                player,
                null,
                null,
                player.getX(), player.getY(), player.getZ(),
                charged ? CHARGED_EXPLOSION_POWER : NORMAL_EXPLOSION_POWER,
                false,
                Level.ExplosionInteraction.NONE,
                ModParticles.NO_EXPLOSION.get(),
                ModParticles.NO_EXPLOSION.get(),
                SoundEvents.GENERIC_EXPLODE
        );
    }

    private static void sendExplosionEmitter(ServerLevel level, ServerPlayer sourcePlayer) {
        for (ServerPlayer player : level.players()) {
            level.sendParticles(
                    player,
                    ParticleTypes.EXPLOSION_EMITTER,
                    true,
                    sourcePlayer.getX(), sourcePlayer.getY(), sourcePlayer.getZ(),
                    1,
                    0.0D, 0.0D, 0.0D,
                    0.0D
            );
        }
    }
}
