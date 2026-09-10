package com.github.xtzi9859.railguncoin.network;

import com.github.xtzi9859.railguncoin.RailgunCoinMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record LightningVisualPayload(double x, double y, double z, long seed)
        implements CustomPacketPayload {
    public static final Type<LightningVisualPayload> TYPE = new Type<>(
            RailgunCoinMod.id("lightning_visual")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, LightningVisualPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeDouble(payload.x());
                        buffer.writeDouble(payload.y());
                        buffer.writeDouble(payload.z());
                        buffer.writeLong(payload.seed());
                    },
                    buffer -> new LightningVisualPayload(
                            buffer.readDouble(),
                            buffer.readDouble(),
                            buffer.readDouble(),
                            buffer.readLong()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
