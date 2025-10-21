package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.decade;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Kamen Rider Decade 模型类
 * 用于渲染Decade骑士的各种形态和动画
 */
public class DecadeRiderModel extends GeoModel<DecadeRiderEntity> {

    @Override
    public ResourceLocation getModelResource(DecadeRiderEntity animatable) {
        return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "geo/rider/decade/dcd.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DecadeRiderEntity animatable) {
        return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "textures/rider/decade/dcd.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DecadeRiderEntity animatable) {
        return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "animations/rider/decade/dcd.animation.json");
    }
}