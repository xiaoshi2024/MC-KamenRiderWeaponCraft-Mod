package com.xiaoshi2022.kamenriderweaponcraft.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xiaoshi2022.kamenriderweaponcraft.entity.SonicArrowLaserEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/**
 * 音速弓激光渲染器：什么都不渲染（雷电弧由 SonicArrowLaserPacket 在 RenderLevelStageEvent 中绘制）
 */
public class SonicArrowLaserRenderer extends EntityRenderer<SonicArrowLaserEntity> {

    public SonicArrowLaserRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SonicArrowLaserEntity entity) {
        return null;
    }

    @Override
    public void render(SonicArrowLaserEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 不渲染实体本体，仅保留粒子与雷电弧特效
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    protected int getBlockLightLevel(SonicArrowLaserEntity entity, BlockPos pos) {
        return 15; // 发光
    }
}
