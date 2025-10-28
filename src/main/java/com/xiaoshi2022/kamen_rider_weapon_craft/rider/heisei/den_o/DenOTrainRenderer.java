package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.den_o;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;


public class DenOTrainRenderer extends GeoEntityRenderer<DenOTrainEntity> {
    // 只保留圣剑模型的相关配置
    private static final String SWORD_TEXTURE = "textures/rider/den_o/den_o_sword.png";

    public DenOTrainRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DenOTrainModel());
        this.shadowRadius = 0.3f;
    }

    @Override
    public ResourceLocation getTextureLocation(DenOTrainEntity entity) {
        // 只返回圣剑的纹理
        return new ResourceLocation(MOD_ID, SWORD_TEXTURE);
    }

    public void scale(DenOTrainEntity entity, PoseStack poseStack, float partialTickTime) {
        // 只使用圣剑的缩放
        poseStack.scale(1.2f, 1.2f, 1.2f);
    }
}