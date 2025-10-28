package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.hibiki;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

public class HibikiDrumEffectModel extends GeoModel<HibikiDrumEffectEntity> {
    @Override
    public ResourceLocation getModelResource(HibikiDrumEffectEntity object) {
        // 加载响鬼鼓的Geo模型文件
        return new ResourceLocation(MOD_ID, "geo/rider/hibiki/hibiki_drum.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HibikiDrumEffectEntity object) {
        // 返回响鬼鼓的纹理文件
        return new ResourceLocation(MOD_ID, "textures/rider/hibiki/hibiki_drum.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HibikiDrumEffectEntity object) {
        // 加载响鬼鼓的动画文件
        return new ResourceLocation(MOD_ID, "animations/rider/hibiki/hibiki_drum.animation.json");
    }
}