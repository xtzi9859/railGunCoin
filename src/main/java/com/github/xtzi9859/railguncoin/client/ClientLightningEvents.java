package com.github.xtzi9859.railguncoin.client;

import com.github.xtzi9859.railguncoin.RailgunCoinMod;
import com.github.xtzi9859.railguncoin.network.LightningVisualPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = RailgunCoinMod.MOD_ID, value = Dist.CLIENT)
public final class ClientLightningEvents {
    private static final List<LightningBolt> LIGHTNING_BOLTS = new ArrayList<>();

    private ClientLightningEvents() {
    }

    public static void addLightning(LightningVisualPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(minecraft.level);
        if (lightning == null) {
            return;
        }
        lightning.moveTo(payload.x(), payload.y(), payload.z());
        lightning.seed = payload.seed();
        LIGHTNING_BOLTS.add(lightning);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            LIGHTNING_BOLTS.clear();
            return;
        }
        Iterator<LightningBolt> iterator = LIGHTNING_BOLTS.iterator();
        while (iterator.hasNext()) {
            LightningBolt lightning = iterator.next();
            if (lightning.level() != minecraft.level || lightning.isRemoved()) {
                iterator.remove();
                continue;
            }
            lightning.tick();
            if (lightning.isRemoved()) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void renderLightning(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES
                || LIGHTNING_BOLTS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Vec3 cameraPosition = event.getCamera().getPosition();
        double maximumVisibleDistance = Math.max(
                16.0D,
                minecraft.options.getEffectiveRenderDistance() * 16.0D - 16.0D
        );
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        var bufferSource = minecraft.renderBuffers().bufferSource();

        for (LightningBolt lightning : LIGHTNING_BOLTS) {
            Vec3 cameraOffset = lightning.position().subtract(cameraPosition);
            double distance = cameraOffset.length();
            if (distance > maximumVisibleDistance) {
                cameraOffset = cameraOffset.scale(maximumVisibleDistance / distance);
            }
            minecraft.getEntityRenderDispatcher().render(
                    lightning,
                    cameraOffset.x, cameraOffset.y, cameraOffset.z,
                    lightning.getYRot(),
                    partialTick,
                    event.getPoseStack(),
                    bufferSource,
                    LightTexture.FULL_BRIGHT
            );
        }
        bufferSource.endBatch(RenderType.lightning());
    }
}
