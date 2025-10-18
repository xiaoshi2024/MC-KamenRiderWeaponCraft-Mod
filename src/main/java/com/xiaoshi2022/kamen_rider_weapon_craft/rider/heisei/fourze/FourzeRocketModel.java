package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.fourze;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Fourze火箭炮的模型类
 * 用于引用和加载geo模型文件
 */
public class FourzeRocketModel extends GeoModel<FourzeRocketEntity> {
    // 模型资源路径
    private static final ResourceLocation MODEL_RESOURCE = new ResourceLocation("kamen_rider_weapon_craft", "geo/rider/fourze/fourze_rocket.geo.json");
    // 纹理资源路径
    private static final ResourceLocation TEXTURE_RESOURCE = new ResourceLocation("kamen_rider_weapon_craft", "textures/rider/fourze/fourze_rocket.png");
    // 动画资源路径
    private static final ResourceLocation ANIMATION_RESOURCE = new ResourceLocation("kamen_rider_weapon_craft", "animations/rider/fourze/fourze_rocket.animation.json");

    @Override
    public ResourceLocation getModelResource(FourzeRocketEntity animatable) {
        return MODEL_RESOURCE;
    }

    @Override
    public ResourceLocation getTextureResource(FourzeRocketEntity animatable) {
        return TEXTURE_RESOURCE;
    }

    @Override
    public ResourceLocation getAnimationResource(FourzeRocketEntity animatable) {
        return ANIMATION_RESOURCE;
    }
}