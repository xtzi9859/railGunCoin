package com.github.xtzi9859.railguncoin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.github.xtzi9859.railguncoin.RailgunCoinMod;
import com.github.xtzi9859.railguncoin.content.LockOnTargeting;
import com.github.xtzi9859.railguncoin.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = RailgunCoinMod.MOD_ID, value = Dist.CLIENT)
public final class ClientLockOnEvents {
    private static final ResourceLocation CURSOR = RailgunCoinMod.id("textures/gui/lock_on.png");
    private static final float CURSOR_SIZE_MULTIPLIER = 2.0F;
    private static final int FADE_DURATION_TICKS = 5;
    private static int lockedEntityId = -1;
    private static int renderedEntityId = -1;
    private static int transitionTicks;
    private static CursorTransition transition = CursorTransition.HIDDEN;
    private static ClientLevel trackedLevel;

    private ClientLockOnEvents() {
    }

    @SubscribeEvent
    public static void onClientPlayerTick(PlayerTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getEntity() != minecraft.player) {
            return;
        }
        if (minecraft.level != trackedLevel) {
            trackedLevel = minecraft.level;
            resetCursor();
        }

        advanceTransition();
        LivingEntity target = LockOnTargeting.findTarget(minecraft.player);
        int nextId = target == null ? -1 : target.getId();

        if (lockedEntityId == -1 && nextId != -1) {
            minecraft.player.playSound(ModSounds.LOCK_ON.value(), 0.8F, 1.0F);
            renderedEntityId = nextId;
            transitionTicks = 0;
            transition = CursorTransition.FADING_IN;
        } else if (lockedEntityId != -1 && nextId == -1) {
            renderedEntityId = lockedEntityId;
            transitionTicks = 0;
            transition = CursorTransition.FADING_OUT;
        } else if (lockedEntityId != -1 && nextId != lockedEntityId) {
            renderedEntityId = nextId;
        }
        lockedEntityId = nextId;
    }

    @SubscribeEvent
    public static void renderCursor(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null
                || !(minecraft.level.getEntity(renderedEntityId) instanceof LivingEntity target)) {
            return;
        }
        Entity markerTarget = target instanceof EnderDragon dragon ? dragon.head : target;
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float transitionProgress = smoothStep(Mth.clamp(
                (transitionTicks + partialTick) / FADE_DURATION_TICKS,
                0.0F,
                1.0F
        ));
        float sizeMultiplier = switch (transition) {
            case FADING_IN -> Mth.lerp(transitionProgress, 2.0F, 1.0F);
            case FADING_OUT -> Mth.lerp(transitionProgress, 1.0F, 2.0F);
            default -> 1.0F;
        };
        float opacity = switch (transition) {
            case FADING_IN -> transitionProgress;
            case FADING_OUT -> 1.0F - transitionProgress;
            case VISIBLE -> 1.0F;
            case HIDDEN -> 0.0F;
        };
        int alpha = Mth.clamp(Math.round(235.0F * opacity), 0, 235);
        double targetWidth = markerTarget.getBbWidth();
        double targetHeight = markerTarget.getBbHeight();
        Vec3 targetCenter = new Vec3(
                Mth.lerp(partialTick, markerTarget.xo, markerTarget.getX()),
                Mth.lerp(partialTick, markerTarget.yo, markerTarget.getY()) + targetHeight * 0.5D,
                Mth.lerp(partialTick, markerTarget.zo, markerTarget.getZ())
        );
        Vec3 cameraPosition = event.getCamera().getPosition();
        Vec3 cameraOffset = cameraPosition.subtract(targetCenter);
        Vec3 towardCamera = cameraOffset.lengthSqr() < 1.0E-6D
                ? new Vec3(0.0D, 0.0D, 1.0D)
                : cameraOffset.normalize();
        double modelClearance = Math.max(
                0.75D,
                Math.max(targetWidth, targetHeight) * 0.15D
        );
        double surfaceOffset = distanceToBoundingBoxSurface(targetWidth, targetHeight, towardCamera) + modelClearance;
        float targetSize = Mth.clamp(
                (float) Math.max(targetWidth, targetHeight) * 0.45F,
                0.8F,
                2.5F
        );
        double cursorDistance = Math.max(0.25D, cameraOffset.length() - surfaceOffset);
        float cursorSize = targetSize
                * CURSOR_SIZE_MULTIPLIER
                * sizeMultiplier
                * (float) (cursorDistance / 12.0D);
        Vec3 cursorPosition = targetCenter.add(towardCamera.scale(surfaceOffset));

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(
                cursorPosition.x - cameraPosition.x,
                cursorPosition.y - cameraPosition.y,
                cursorPosition.z - cameraPosition.z
        );
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(cursorSize, cursorSize, cursorSize);

        RenderType renderType = RenderType.entityTranslucentEmissive(CURSOR);
        var bufferSource = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(renderType);
        Matrix4f matrix = poseStack.last().pose();
        vertex(consumer, poseStack, matrix, -0.5F, -0.5F, 0.0F, 1.0F, alpha);
        vertex(consumer, poseStack, matrix, 0.5F, -0.5F, 1.0F, 1.0F, alpha);
        vertex(consumer, poseStack, matrix, 0.5F, 0.5F, 1.0F, 0.0F, alpha);
        vertex(consumer, poseStack, matrix, -0.5F, 0.5F, 0.0F, 0.0F, alpha);
        bufferSource.endBatch(renderType);
        poseStack.popPose();
    }

    private static double distanceToBoundingBoxSurface(
            double targetWidth, double targetHeight, Vec3 direction
    ) {
        double halfWidth = targetWidth * 0.5D;
        double halfHeight = targetHeight * 0.5D;
        return Math.min(
                axisDistance(halfWidth, direction.x),
                Math.min(axisDistance(halfHeight, direction.y), axisDistance(halfWidth, direction.z))
        );
    }

    private static double axisDistance(double halfExtent, double directionComponent) {
        double component = Math.abs(directionComponent);
        return component < 1.0E-6D ? Double.POSITIVE_INFINITY : halfExtent / component;
    }

    private static void advanceTransition() {
        if (transition != CursorTransition.FADING_IN && transition != CursorTransition.FADING_OUT) {
            return;
        }
        transitionTicks++;
        if (transitionTicks < FADE_DURATION_TICKS) {
            return;
        }
        if (transition == CursorTransition.FADING_IN) {
            transition = CursorTransition.VISIBLE;
        } else {
            transition = CursorTransition.HIDDEN;
            renderedEntityId = -1;
        }
        transitionTicks = FADE_DURATION_TICKS;
    }

    private static void resetCursor() {
        lockedEntityId = -1;
        renderedEntityId = -1;
        transitionTicks = 0;
        transition = CursorTransition.HIDDEN;
    }

    private static float smoothStep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    private static void vertex(
            VertexConsumer consumer, PoseStack poseStack, Matrix4f matrix,
            float x, float y, float u, float v, int alpha
    ) {
        consumer.addVertex(matrix, x, y, 0.0F)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0x00F000F0)
                .setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
    }

    private enum CursorTransition {
        HIDDEN,
        FADING_IN,
        VISIBLE,
        FADING_OUT
    }
}
