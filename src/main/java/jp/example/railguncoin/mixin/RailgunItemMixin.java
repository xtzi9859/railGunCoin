package jp.example.railguncoin.mixin;

import blusunrize.immersiveengineering.common.items.RailgunItem;
import jp.example.railguncoin.content.LockOnTargeting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = RailgunItem.class, remap = false)
public abstract class RailgunItemMixin {
    private static final int COIN_SHOT_ENERGY = 4000;

    @ModifyVariable(method = "use", at = @At(value = "STORE"), ordinal = 0)
    private int railguncoin$useEnergy(int original, Level level, Player player, InteractionHand hand) {
        return LockOnTargeting.hasSelectedCoinAmmo(player.getItemInHand(hand), player)
                ? COIN_SHOT_ENERGY : original;
    }

    @ModifyVariable(method = "releaseUsing", at = @At(value = "STORE"), ordinal = 2)
    private int railguncoin$releaseEnergy(
            int original, ItemStack railgun, Level level, LivingEntity user, int timeLeft
    ) {
        return user instanceof Player player && LockOnTargeting.hasSelectedCoinAmmo(railgun, player)
                ? COIN_SHOT_ENERGY : original;
    }
}
