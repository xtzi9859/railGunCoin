package jp.example.railguncoin.mixin;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.common.entities.RailgunShotEntity;
import blusunrize.immersiveengineering.common.register.IEItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = RailgunShotEntity.class, remap = false)
public abstract class RailgunShotEntityMixin {
    @Shadow
    public abstract ItemStack getAmmo();

    @ModifyVariable(method = "onHitEntity", at = @At(value = "STORE"), ordinal = 0)
    private double railguncoin$addProjectileDamage(double original, EntityHitResult hit) {
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
