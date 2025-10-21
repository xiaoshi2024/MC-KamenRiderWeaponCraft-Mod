package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.build;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Kamen Rider Build 模型类
 * 用于渲染Build骑士的各种形态和动画
 */
public class BuildRiderModel extends GeoModel<BuildRiderEntity> {

    @Override
    public ResourceLocation getModelResource(BuildRiderEntity animatable) {
        return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "geo/rider/build/effect19.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BuildRiderEntity animatable) {
        return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "textures/rider/build/effect19.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BuildRiderEntity animatable) {
        return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "animations/rider/build/effect19.animation.json");
    }
}