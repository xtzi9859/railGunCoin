package com.github.xtzi9859.railguncoin.mixin;

import net.neoforged.neoforge.gametest.GameTestHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameTestHooks.class, remap = false)
public abstract class GameTestHooksMixin {
    @Inject(method = "registerGametests", at = @At("HEAD"), cancellable = true)
    private static void railguncoin$skipRunServerGameTests(CallbackInfo callback) {
        if (Boolean.getBoolean("railguncoin.disableGameTestRegistration")) {
            callback.cancel();
        }
    }
}
