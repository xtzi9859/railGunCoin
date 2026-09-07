package jp.example.railguncoin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import jp.example.railguncoin.RailgunCoinMod;
import jp.example.railguncoin.content.CoinProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class CoinProjectileRenderer extends EntityRenderer<CoinProjectileEntity> {
    private static final ResourceLocation TEXTURE = RailgunCoinMod.id("textures/item/silver_coin.png");

    public CoinProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(
            CoinProjectileEntity entity, float yaw, float partialTick,
            PoseStack poseStack, MultiBufferSource buffers, int packedLight
    ) {
        poseStack.pushPose();
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.35F, 0.35F, 0.35F);
        drawQuad(poseStack, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), packedLight);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
    }

    private static void drawQuad(PoseStack poseStack, VertexConsumer consumer, int light) {
        Matrix4f matrix = poseStack.last().pose();
        vertex(consumer, poseStack, matrix, -0.5F, -0.5F, 0.0F, 1.0F, light);
        vertex(consumer, poseStack, matrix, 0.5F, -0.5F, 1.0F, 1.0F, light);
        vertex(consumer, poseStack, matrix, 0.5F, 0.5F, 1.0F, 0.0F, light);
        vertex(consumer, poseStack, matrix, -0.5F, 0.5F, 0.0F, 0.0F, light);
    }

    private static void vertex(
            VertexConsumer consumer, PoseStack poseStack, Matrix4f matrix,
            float x, float y, float u, float v, int light
    ) {
        consumer.addVertex(matrix, x, y, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(CoinProjectileEntity entity) {
        return TEXTURE;
    }
}
