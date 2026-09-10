package com.github.xtzi9859.railguncoin.network;

import com.github.xtzi9859.railguncoin.client.ClientLightningEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModNetwork {
    private static final String NETWORK_VERSION = "1";

    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(NETWORK_VERSION).playToClient(
                LightningVisualPayload.TYPE,
                LightningVisualPayload.STREAM_CODEC,
                ModNetwork::handleLightningVisual
        );
    }

    private static void handleLightningVisual(
            LightningVisualPayload payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context
    ) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientLightningEvents.addLightning(payload);
        }
    }
}
