package com.github.xtzi9859.railguncoin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.github.xtzi9859.railguncoin.RailgunCoinMod;
import com.github.xtzi9859.railguncoin.content.LockOnTargeting;
import com.github.xtzi9859.railguncoin.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = RailgunCoinMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientLockOnEvents {
    private static final ResourceLocation CURSOR = RailgunCoinMod.id("textures/gui/lock_on.png");
    private static final float CURSOR_SIZE_MULTIPLIER = 2.0F;
    private static int lockedEntityId = -1;

    private ClientLockOnEvents() {
    }

    @SubscribeEvent
    public static void onClientPlayerTick(PlayerTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getEntity() != minecraft.player) {
            return;
        }
        LivingEntity target = LockOnTargeting.findTarget(minecraft.player);
        int nextId = target == null ? -1 : target.getId();
        if (nextId != -1 && nextId != lockedEntityId) {
            minecraft.player.playSound(ModSounds.LOCK_ON.value(), 0.8F, 1.0F);
        }
        lockedEntityId = nextId;
    }

    @SubscribeEvent
    public static void renderCursor(RenderLivingEvent.Post<LivingEntity, ?> event) {
        if (event.getEntity().getId() != lockedEntityId) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        LivingEntity target = event.getEntity();
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 targetCenter = new Vec3(
                Mth.lerp(event.getPartialTick(), target.xo, target.getX()),
                Mth.lerp(event.getPartialTick(), target.yo, target.getY()) + target.getBbHeight() * 0.5D,
                Mth.lerp(event.getPartialTick(), target.zo, target.getZ())
        );
        Vec3 cameraOffset = minecraft.gameRenderer.getMainCamera().getPosition().subtract(targetCenter);
        Vec3 towardCamera = cameraOffset.lengthSqr() < 1.0E-6D
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : cameraOffset.normalize();
        double modelClearance = Math.max(
                0.75D,
                Math.max(target.getBbWidth(), target.getBbHeight()) * 0.15D
        );
        double surfaceOffset = distanceToBoundingBoxSurface(target, towardCamera) + modelClearance;
        float targetSize = Mth.clamp(
                Math.max(target.getBbWidth(), target.getBbHeight()) * 0.45F,
                0.8F,
                2.5F
        );
        double cursorDistance = Math.max(0.25D, cameraOffset.length() - surfaceOffset);
        float cursorSize = targetSize * CURSOR_SIZE_MULTIPLIER * (float) (cursorDistance / 12.0D);

        poseStack.pushPose();
        poseStack.translate(
                towardCamera.x * surfaceOffset,
                target.getBbHeight() * 0.5D + towardCamera.y * surfaceOffset,
                towardCamera.z * surfaceOffset
        );
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(cursorSize, cursorSize, cursorSize);

        VertexConsumer consumer = event.getMultiBufferSource().getBuffer(RenderType.entityTranslucentEmissive(CURSOR));
        Matrix4f matrix = poseStack.last().pose();
        vertex(consumer, poseStack, matrix, -0.5F, -0.5F, 0.0F, 1.0F);
        vertex(consumer, poseStack, matrix, 0.5F, -0.5F, 1.0F, 1.0F);
        vertex(consumer, poseStack, matrix, 0.5F, 0.5F, 1.0F, 0.0F);
        vertex(consumer, poseStack, matrix, -0.5F, 0.5F, 0.0F, 0.0F);
        poseStack.popPose();
    }

    private static double distanceToBoundingBoxSurface(LivingEntity target, Vec3 direction) {
        double halfWidth = target.getBbWidth() * 0.5D;
        double halfHeight = target.getBbHeight() * 0.5D;
        return Math.min(
                axisDistance(halfWidth, direction.x),
                Math.min(axisDistance(halfHeight, direction.y), axisDistance(halfWidth, direction.z))
        );
    }

    private static double axisDistance(double halfExtent, double directionComponent) {
        double component = Math.abs(directionComponent);
        return component < 1.0E-6D ? Double.POSITIVE_INFINITY : halfExtent / component;
    }

    private static void vertex(
            VertexConsumer consumer, PoseStack poseStack, Matrix4f matrix,
            float x, float y, float u, float v
    ) {
        consumer.addVertex(matrix, x, y, 0.0F)
                .setColor(255, 255, 255, 235)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0x00F000F0)
                .setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
    }
}
