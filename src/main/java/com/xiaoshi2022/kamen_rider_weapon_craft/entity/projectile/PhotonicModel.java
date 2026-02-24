package com.xiaoshi2022.kamen_rider_weapon_craft.entity.projectile;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Photonic子弹的模型类
 * 用于引用和加载geo模型文件
 */
public class PhotonicModel extends GeoModel<PhotonicEntity> {
    // 模型资源路径
    private static final ResourceLocation MODEL_RESOURCE = new ResourceLocation("kamen_rider_weapon_craft", "geo/entity/photonic.geo.json");
    // 纹理资源路径
    private static final ResourceLocation TEXTURE_RESOURCE = new ResourceLocation("kamen_rider_weapon_craft", "textures/entity/photonic.png");
    // 动画资源路径
    private static final ResourceLocation ANIMATION_RESOURCE = new ResourceLocation("kamen_rider_weapon_craft", "animations/entity/photonic.animation.json");

    @Override
    public ResourceLocation getModelResource(PhotonicEntity animatable) {
        return MODEL_RESOURCE;
    }

    @Override
    public ResourceLocation getTextureResource(PhotonicEntity animatable) {
        return TEXTURE_RESOURCE;
    }

    @Override
    public ResourceLocation getAnimationResource(PhotonicEntity animatable) {
        return ANIMATION_RESOURCE;
    }
}
