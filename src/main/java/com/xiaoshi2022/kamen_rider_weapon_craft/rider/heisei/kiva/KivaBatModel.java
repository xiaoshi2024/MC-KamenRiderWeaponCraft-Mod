package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kiva;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

/**
 * Kamen Rider Kiva 蝙蝠模型类
 * 定义蝙蝠群的3D模型和动画
 */
public class KivaBatModel extends GeoModel<KivaBatEntity> {
    
    @Override
    public ResourceLocation getModelResource(KivaBatEntity object) {
        // 返回模型文件的路径
        return new ResourceLocation(MOD_ID, "geo/rider/kiva/kiva_bat.geo.json");
    }
    
    @Override
    public ResourceLocation getTextureResource(KivaBatEntity object) {
        // 返回纹理文件的路径
        return new ResourceLocation(MOD_ID, "textures/rider/kiva/kiva_bat.png");
    }
    
    @Override
    public ResourceLocation getAnimationResource(KivaBatEntity animatable) {
        // 返回动画文件的路径，使用"bats"作为动画名称，与要求一致
        return new ResourceLocation(MOD_ID, "animations/rider/kiva/kiva_bat.animation.json");
    }
}