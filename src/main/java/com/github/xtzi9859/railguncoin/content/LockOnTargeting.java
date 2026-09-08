package com.github.xtzi9859.railguncoin.content;

import blusunrize.immersiveengineering.common.items.RailgunItem;
import com.github.xtzi9859.railguncoin.registry.ModItems;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;

public final class LockOnTargeting {
    public static final double RANGE = 32.0D;
    private static final double MIN_VIEW_DOT = 0.5D;

    private LockOnTargeting() {
    }

    public static boolean isHoldingLoadedCoinRailgun(Player player) {
        for (ItemStack held : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (held.getItem() instanceof RailgunItem
                    && RailgunItem.findAmmo(held, player).is(ModItems.SILVER_COIN.get())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasSelectedCoinAmmo(ItemStack railgun, Player player) {
        return railgun.getItem() instanceof RailgunItem
                && RailgunItem.findAmmo(railgun, player).is(ModItems.SILVER_COIN.get());
    }

    @Nullable
    public static Monster findTarget(Player player) {
        if (!isHoldingLoadedCoinRailgun(player)) {
            return null;
        }
        return findVisibleTarget(player);
    }

    @Nullable
    public static Monster findVisibleTarget(Player player) {
        Vec3 eyes = player.getEyePosition();
        Vec3 view = player.getViewVector(1.0F).normalize();
        return player.level().getEntitiesOfClass(
                        Monster.class,
                        player.getBoundingBox().inflate(RANGE),
                        mob -> isValid(player, mob, eyes, view)
                ).stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }

    private static boolean isValid(Player player, Monster mob, Vec3 eyes, Vec3 view) {
        if (!mob.isAlive() || mob instanceof EnderMan || player.distanceToSqr(mob) > RANGE * RANGE) {
            return false;
        }
        Vec3 target = mob.getEyePosition().subtract(eyes);
        return target.lengthSqr() > 0.0001D
                && view.dot(target.normalize()) >= MIN_VIEW_DOT
                && player.hasLineOfSight(mob);
    }
}
