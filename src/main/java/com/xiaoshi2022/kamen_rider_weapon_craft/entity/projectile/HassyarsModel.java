package com.xiaoshi2022.kamen_rider_weapon_craft.entity.projectile;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Hassyars子弹的模型类
 * 用于引用和加载geo模型文件
 */
public class HassyarsModel extends GeoModel<HassyarsEntity> {
    // 模型资源路径
    private static final ResourceLocation MODEL_RESOURCE = new ResourceLocation("kamen_rider_weapon_craft", "geo/entity/hassyars.geo.json");
    // 纹理资源路径
    private static final ResourceLocation TEXTURE_RESOURCE = new ResourceLocation("kamen_rider_weapon_craft", "textures/entity/hassyars.png");
    // 动画资源路径
    private static final ResourceLocation ANIMATION_RESOURCE = new ResourceLocation("kamen_rider_weapon_craft", "animations/entity/hassyars.animation.json");

    @Override
    public ResourceLocation getModelResource(HassyarsEntity animatable) {
        return MODEL_RESOURCE;
    }

    @Override
    public ResourceLocation getTextureResource(HassyarsEntity animatable) {
        return TEXTURE_RESOURCE;
    }

    @Override
    public ResourceLocation getAnimationResource(HassyarsEntity animatable) {
        return ANIMATION_RESOURCE;
    }
}
