package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ooo;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

/**
 * Kamen Rider OOO Geo实体模型类
 * 定义细胞硬币斩的3D模型和动画
 */
public class OOOGeoModel extends GeoModel<OOOGeoEntity> {
    
    @Override
    public ResourceLocation getModelResource(OOOGeoEntity object) {
        // 返回模型文件的路径
        // 这里使用基础模型，实际项目中需要为不同联组创建不同的模型
        return new ResourceLocation(MOD_ID, "geo/rider/ooo/ooo_geo.geo.json");
    }
    
    @Override
    public ResourceLocation getTextureResource(OOOGeoEntity object) {
        // 统一使用tatoba纹理
        return new ResourceLocation(MOD_ID, "textures/rider/ooo/ooo_geo.png");
    }
    
    @Override
    public ResourceLocation getAnimationResource(OOOGeoEntity animatable) {
        // 统一使用基础动画文件
        return new ResourceLocation(MOD_ID, "animations/rider/ooo/ooo_geo.animation.json");
    }
}