package com.github.xtzi9859.railguncoin.mixin;

import com.github.xtzi9859.railguncoin.content.ModDamageSources;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(CombatTracker.class)
public abstract class CombatTrackerMixin {
    @Shadow
    @Final
    private LivingEntity mob;

    @Shadow
    @Final
    private List<CombatEntry> entries;

    @Inject(method = "getDeathMessage", at = @At("HEAD"), cancellable = true)
    private void railguncoin$useRecoilFallMessage(CallbackInfoReturnable<Component> callback) {
        if (entries.size() < 2
                || !entries.getLast().source().is(DamageTypes.FALL)) {
            return;
        }

        for (int index = entries.size() - 2; index >= 0; index--) {
            CombatEntry entry = entries.get(index);
            if (entry.source().is(DamageTypes.FALL)) {
                continue;
            }

            Component railgunName = ModDamageSources.getRecoilRailgunName(entry.source());
            if (railgunName != null) {
                callback.setReturnValue(Component.translatable(
                        "death.attack.railguncoin.recoil_fall",
                        mob.getDisplayName(),
                        railgunName
                ));
            }
            return;
        }
    }
}
