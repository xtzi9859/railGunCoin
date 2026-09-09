package com.github.xtzi9859.railguncoin.event;

import blusunrize.immersiveengineering.common.items.RailgunItem;
import com.github.xtzi9859.railguncoin.RailgunCoinMod;
import com.github.xtzi9859.railguncoin.content.LockOnTargeting;
import com.github.xtzi9859.railguncoin.content.ModDamageSources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid = RailgunCoinMod.MOD_ID)
public final class CommonEvents {
    private static final int OVERCHARGE_TICKS = 4 * 20;
    private static final int FUSE_WARNING_TICKS = 30;

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

        int detonationTick = RailgunItem.getChargeTime(railgun) + OVERCHARGE_TICKS;
        int ticksUsing = player.getTicksUsingItem();
        if (ticksUsing == detonationTick - FUSE_WARNING_TICKS) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        if (ticksUsing >= detonationTick) {
            selfDestruct(player, railgun);
        }
    }

    private static void selfDestruct(ServerPlayer player, ItemStack railgun) {
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
        player.level().explode(player, player.getX(), player.getY(), player.getZ(),
                8.0F, false, Level.ExplosionInteraction.NONE);
    }
}
