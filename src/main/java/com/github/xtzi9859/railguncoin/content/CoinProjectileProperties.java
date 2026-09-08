package com.github.xtzi9859.railguncoin.content;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import com.github.xtzi9859.railguncoin.RailgunCoinMod;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public final class CoinProjectileProperties implements RailgunHandler.IRailgunProjectile {
    public static final CoinProjectileProperties INSTANCE = new CoinProjectileProperties();

    private CoinProjectileProperties() {
    }

    @Override
    public Entity getProjectile(@Nullable Player shooter, ItemStack ammo, Entity defaultProjectile) {
        if (shooter == null) {
            return defaultProjectile;
        }
        LivingEntity target = LockOnTargeting.findVisibleTarget(shooter);
        shooter.level().playSound(
                null, shooter.getX(), shooter.getY(), shooter.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0F, 1.0F
        );
        if (shooter instanceof ServerPlayer serverPlayer) {
            AdvancementHolder advancement = serverPlayer.server.getAdvancements().get(
                    RailgunCoinMod.id("a_certain_scientific")
            );
            if (advancement != null) {
                serverPlayer.getAdvancements().award(advancement, "fired_coin");
            }
        }
        return new CoinProjectileEntity(shooter.level(), shooter, target);
    }

    @Override
    public double getBreakChance(@Nullable java.util.UUID shooter, ItemStack ammo) {
        return 1.0D;
    }

    @Override
    public boolean isValidForTurret() {
        return false;
    }
}
