package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.exaid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ExAidSlashEffectRenderer extends GeoEntityRenderer<ExAidSlashEffectEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("kamenriderweaponcraft", "textures/rider/exaid/effect18.png");

    public ExAidSlashEffectRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ExAidSlashEffectModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(ExAidSlashEffectEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        int lightValue = 15728880;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        poseStack.scale(1.2F, 1.2F, 1.2F);

        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, lightValue);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ExAidSlashEffectEntity entity) {
        return TEXTURE;
    }
}
