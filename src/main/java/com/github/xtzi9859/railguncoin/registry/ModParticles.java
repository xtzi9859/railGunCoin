package com.github.xtzi9859.railguncoin.registry;

import com.github.xtzi9859.railguncoin.RailgunCoinMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTER = DeferredRegister.create(
            BuiltInRegistries.PARTICLE_TYPE, RailgunCoinMod.MOD_ID
    );

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SONIC_RING = REGISTER.register(
            "sonic_ring", () -> new SimpleParticleType(false)
    );

    private ModParticles() {
    }
}
