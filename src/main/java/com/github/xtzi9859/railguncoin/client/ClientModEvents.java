package com.github.xtzi9859.railguncoin.client;

import com.github.xtzi9859.railguncoin.RailgunCoinMod;
import com.github.xtzi9859.railguncoin.registry.ModEntityTypes;
import com.github.xtzi9859.railguncoin.registry.ModParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = RailgunCoinMod.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SONIC_RING.get(), SonicRingParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.COIN_PROJECTILE.get(), CoinProjectileRenderer::new);
    }
}
