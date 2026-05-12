package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.ghost;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GhostHeroicSoulRenderer extends GeoEntityRenderer<GhostHeroicSoulEntity> {

    public GhostHeroicSoulRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GhostHeroicSoulModel());
    }

    @Override
    public void render(GhostHeroicSoulEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                      MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GhostHeroicSoulEntity entity) {
        return this.model.getTextureResource(entity);
    }
}
