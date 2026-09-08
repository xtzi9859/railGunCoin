package com.github.xtzi9859.railguncoin.mixin;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.common.entities.RailgunShotEntity;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RailgunShotEntity.class, remap = false)
public abstract class RailgunShotEntityMixin {
    @Shadow
    public abstract ItemStack getAmmo();

    @ModifyExpressionValue(
            method = "onHitEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lblusunrize/immersiveengineering/api/tool/RailgunHandler$IRailgunProjectile;getDamage(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Ljava/util/UUID;Lnet/minecraft/world/entity/Entity;)D"
            )
    )
    private double railguncoin$addProjectileDamage(double original) {
        ItemStack ammo = getAmmo();
        if (ammo.is(IEItems.Misc.GRAPHITE_ELECTRODE.asItem())) {
            return original + 6.0D;
        }
        if (ammo.is(IETags.metalRods)
                || ammo.is(Tags.Items.RODS_BLAZE)
                || ammo.is(Tags.Items.RODS_BREEZE)
                || ammo.is(Items.END_ROD)) {
            return original + 4.0D;
        }
        return original;
    }
}
