package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.Faiz;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

public class FaizEmptySetGeoModel extends GeoModel<FaizEmptySetEntity> {
    @Override
    public ResourceLocation getModelResource(FaizEmptySetEntity object) {
        // 加载Faiz空集的Geo模型文件，路径参考Hibiki
        return new ResourceLocation(MOD_ID, "geo/rider/faiz/faiz_empty_set.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FaizEmptySetEntity object) {
        // 返回Faiz空集的纹理文件，路径参考Hibiki
        return new ResourceLocation(MOD_ID, "textures/rider/faiz/faiz_empty_set.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FaizEmptySetEntity object) {
        // 加载Faiz空集的动画文件，路径参考Hibiki
        return new ResourceLocation(MOD_ID, "animations/rider/faiz/faiz_empty_set.animation.json");
    }
}