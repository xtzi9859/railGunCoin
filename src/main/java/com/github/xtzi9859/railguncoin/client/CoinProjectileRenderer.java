package com.github.xtzi9859.railguncoin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.github.xtzi9859.railguncoin.RailgunCoinMod;
import com.github.xtzi9859.railguncoin.content.CoinProjectileEntity;
import com.github.xtzi9859.railguncoin.registry.ModItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public final class CoinProjectileRenderer extends EntityRenderer<CoinProjectileEntity> {
    private static final ResourceLocation TEXTURE = RailgunCoinMod.id("textures/item/silver_coin.png");
    private final ItemRenderer itemRenderer;

    public CoinProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            CoinProjectileEntity entity, float yaw, float partialTick,
            PoseStack poseStack, MultiBufferSource buffers, int packedLight
    ) {
        poseStack.pushPose();
        Vec3 movement = entity.getDeltaMovement();
        if (movement.lengthSqr() > 0.0001D) {
            double horizontalSpeed = Math.sqrt(movement.x * movement.x + movement.z * movement.z);
            float movementYaw = (float)Math.toDegrees(Math.atan2(movement.x, movement.z));
            float movementPitch = (float)Math.toDegrees(Math.atan2(-movement.y, horizontalSpeed));
            poseStack.mulPose(Axis.YP.rotationDegrees(movementYaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(movementPitch + 90.0F));
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        }
        poseStack.scale(0.35F, 0.35F, 0.35F);
        itemRenderer.renderStatic(
                ModItems.SILVER_COIN.get().getDefaultInstance(),
                ItemDisplayContext.NONE,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffers,
                entity.level(),
                entity.getId()
        );
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CoinProjectileEntity entity) {
        return TEXTURE;
    }
}
