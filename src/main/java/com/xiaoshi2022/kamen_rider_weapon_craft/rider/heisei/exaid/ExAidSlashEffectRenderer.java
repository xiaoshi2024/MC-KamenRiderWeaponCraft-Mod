package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

public class ExAidSlashEffectRenderer extends GeoEntityRenderer<ExAidSlashEffectEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(MOD_ID, "textures/rider/exaid/effect18.png");

    public ExAidSlashEffectRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ExAidSlashEffectModel());
        this.shadowRadius = 0.0F; // 特效不需要阴影
    }

    @Override
    protected void applyRotations(ExAidSlashEffectEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        // 这个方法用于应用旋转，我们不需要额外的旋转，因为已经在render方法中处理了
        // 不调用super.applyRotations，这样我们可以完全控制旋转
    }

    @Override
    public void render(ExAidSlashEffectEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 设置足够的光照值，确保特效清晰可见
        int lightValue = 15728880; // 最大光照值
        
        // 在渲染前应用旋转，确保特效朝向正确的方向
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
        
        // 调整特效大小
        poseStack.scale(1.2F, 1.2F, 1.2F);
        
        // 调用父类的render方法进行实际渲染
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, lightValue);
        
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ExAidSlashEffectEntity entity) {
        return TEXTURE;
    }
}