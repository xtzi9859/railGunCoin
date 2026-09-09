package com.github.xtzi9859.railguncoin.content;

import blusunrize.immersiveengineering.common.items.RailgunItem;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import com.github.xtzi9859.railguncoin.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;

@SuppressWarnings("resource")
public final class LockOnTargeting {
    public static final double RANGE = 32.0D;
    private static final double MIN_VIEW_DOT = 0.5D;

    private LockOnTargeting() {
    }

    public static boolean isHoldingLoadedCoinRailgun(Player player) {
        for (ItemStack held : new ItemStack[]{player.getMainHandItem(), player.getOffhandItem()}) {
            if (held.getItem() instanceof RailgunItem
                    && EnergyHelper.getEnergyStored(held) > 0
                    && isCoinAmmo(RailgunItem.findAmmo(held, player))) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasSelectedCoinAmmo(ItemStack railgun, Player player) {
        return railgun.getItem() instanceof RailgunItem
                && isCoinAmmo(RailgunItem.findAmmo(railgun, player));
    }

    public static boolean isCoinAmmo(ItemStack stack) {
        return stack.is(ModItems.SILVER_COIN.get()) || isChargedCoin(stack);
    }

    public static boolean isChargedCoin(ItemStack stack) {
        return stack.is(ModItems.CHARGED_COIN.get());
    }

    @Nullable
    public static LivingEntity findTarget(Player player) {
        if (!isHoldingLoadedCoinRailgun(player)) {
            return null;
        }
        return findVisibleTarget(player);
    }

    @Nullable
    public static LivingEntity findVisibleTarget(Player player) {
        Vec3 eyes = player.getEyePosition();
        Vec3 view = player.getViewVector(1.0F).normalize();
        return player.level().getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(RANGE),
                        mob -> mob instanceof Enemy && isValid(player, mob, eyes, view)
                ).stream()
                .min(Comparator
                        .comparingInt(LockOnTargeting::targetPriority)
                        .thenComparingDouble(mob -> getAimPoint(mob).distanceToSqr(eyes)))
                .orElse(null);
    }

    private static int targetPriority(LivingEntity target) {
        return target instanceof EnderDragon || target instanceof WitherBoss ? 0 : 1;
    }

    private static boolean isValid(Player player, LivingEntity mob, Vec3 eyes, Vec3 view) {
        if (!mob.isAlive()) {
            return false;
        }
        Vec3 target = getAimPoint(mob).subtract(eyes);
        if (target.lengthSqr() < 0.0001D || target.lengthSqr() > RANGE * RANGE
                || view.dot(target.normalize()) < MIN_VIEW_DOT) {
            return false;
        }
        BlockHitResult obstruction = player.level().clip(new ClipContext(
                eyes, eyes.add(target), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player
        ));
        return obstruction.getType() == HitResult.Type.MISS;
    }

    public static Vec3 getAimPoint(LivingEntity target) {
        if (target instanceof EnderDragon dragon) {
            return dragon.head.getBoundingBox().getCenter();
        }
        double verticalInset = Math.min(0.5D, target.getBbHeight() * 0.15D);
        return target.getEyePosition().add(0.0D, -verticalInset, 0.0D);
    }
}
