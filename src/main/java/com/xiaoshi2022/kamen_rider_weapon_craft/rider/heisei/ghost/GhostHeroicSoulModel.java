package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ghost;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Kamen Rider Ghost 伟人魂模型类
 * 用于渲染不同伟人魂实体的3D模型
 * 根据不同的伟人类型返回不同的模型、纹理和动画
 */
public class GhostHeroicSoulModel extends GeoModel<GhostHeroicSoulEntity> {
    @Override
    public ResourceLocation getModelResource(GhostHeroicSoulEntity animatable) {
        // 根据伟人魂类型选择不同的模型文件
        if (animatable == null) {
            // 默认使用武藏模型
            return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "geo/rider/ghost/musashi.geo.json");
        }
        
        String soulType = animatable.getSoulType();
        switch (soulType) {
            case "MUSASHI":
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "geo/rider/ghost/musashi.geo.json");
            case "EDISON":
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "geo/rider/ghost/edison.geo.json");
            case "NEWTON":
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "geo/rider/ghost/newton.geo.json");
            default:
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "geo/rider/ghost/musashi.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(GhostHeroicSoulEntity animatable) {
        // 根据伟人魂类型选择不同的纹理
        if (animatable == null) {
            // 默认使用武藏纹理
            return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "textures/rider/ghost/musashi.png");
        }
        
        String soulType = animatable.getSoulType();
        switch (soulType) {
            case "MUSASHI":
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "textures/rider/ghost/musashi.png");
            case "EDISON":
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "textures/rider/ghost/edison.png");
            case "NEWTON":
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "textures/rider/ghost/newton.png");
            default:
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "textures/rider/ghost/musashi.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(GhostHeroicSoulEntity animatable) {
        // 根据伟人魂类型选择不同的动画文件
        if (animatable == null) {
            // 默认使用武藏动画
            return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "animations/rider/ghost/musashi.animation.json");
        }
        
        String soulType = animatable.getSoulType();
        switch (soulType) {
            case "MUSASHI":
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "animations/rider/ghost/musashi.animation.json");
            case "EDISON":
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "animations/rider/ghost/edison.animation.json");
            case "NEWTON":
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "animations/rider/ghost/newton.animation.json");
            default:
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "animations/rider/ghost/musashi.animation.json");
        }
    }
}