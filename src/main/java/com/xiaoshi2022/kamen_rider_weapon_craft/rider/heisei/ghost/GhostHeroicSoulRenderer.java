package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ghost;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 伟人魂渲染器类
 * 处理伟人魂实体的渲染，包括动态颜色
 */
public class GhostHeroicSoulRenderer extends GeoEntityRenderer<GhostHeroicSoulEntity> {

    public GhostHeroicSoulRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GhostHeroicSoulModel());
        // 添加发光效果层
//        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void render(GhostHeroicSoulEntity entity, float entityYaw, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight) {
        // 渲染前的准备
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GhostHeroicSoulEntity entity) {
        return this.model.getTextureResource(entity);
    }
}