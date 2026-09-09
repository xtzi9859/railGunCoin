package com.github.xtzi9859.railguncoin.mixin;

import blusunrize.immersiveengineering.common.items.RailgunItem;
import com.github.xtzi9859.railguncoin.registry.ModItems;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RailgunItem.class, remap = false)
public abstract class RailgunItemMixin {
    private static final int NORMAL_COIN_SHOT_ENERGY = 4000;
    private static final int CHARGED_COIN_SHOT_ENERGY = 8000;

    @ModifyExpressionValue(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/common/ModConfigSpec$IntValue;get()Ljava/lang/Object;"
            )
    )
    private Object railguncoin$useEnergy(Object original, Level level, Player player, InteractionHand hand) {
        return railguncoin$getShotEnergy(original, player.getItemInHand(hand), player);
    }

    @ModifyExpressionValue(
            method = "releaseUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/common/ModConfigSpec$IntValue;get()Ljava/lang/Object;"
            )
    )
    private Object railguncoin$releaseEnergy(
            Object original, ItemStack railgun, Level level, LivingEntity user, int timeLeft
    ) {
        return user instanceof Player player
                ? railguncoin$getShotEnergy(original, railgun, player)
                : original;
    }

    private static Object railguncoin$getShotEnergy(Object original, ItemStack railgun, Player player) {
        ItemStack ammo = RailgunItem.findAmmo(railgun, player);
        if (ammo.is(ModItems.CHARGED_COIN.get())) {
            return CHARGED_COIN_SHOT_ENERGY;
        }
        if (ammo.is(ModItems.SILVER_COIN.get())) {
            return NORMAL_COIN_SHOT_ENERGY;
        }
        return original;
    }
}
