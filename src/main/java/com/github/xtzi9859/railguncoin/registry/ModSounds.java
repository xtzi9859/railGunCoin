package com.github.xtzi9859.railguncoin.registry;

import com.github.xtzi9859.railguncoin.RailgunCoinMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(
            Registries.SOUND_EVENT, RailgunCoinMod.MOD_ID
    );

    public static final Holder<SoundEvent> LOCK_ON = REGISTER.register(
            "lock_on", () -> SoundEvent.createVariableRangeEvent(RailgunCoinMod.id("lock_on"))
    );

    private ModSounds() {
    }
}
