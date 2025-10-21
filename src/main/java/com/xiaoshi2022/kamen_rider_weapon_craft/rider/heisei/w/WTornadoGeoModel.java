package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.w;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

/**
 * Kamen Rider W 龙卷风Geo实体模型类
 * 定义龙卷风特效的3D模型和动画
 */
public class WTornadoGeoModel extends GeoModel<WTornadoEntity> {
    
    @Override
    public ResourceLocation getModelResource(WTornadoEntity object) {
        // 返回模型文件的路径
        return new ResourceLocation(MOD_ID, "geo/rider/w/tornado.geo.json");
    }
    
    @Override
    public ResourceLocation getTextureResource(WTornadoEntity object) {
        // 返回纹理文件的路径
        return new ResourceLocation(MOD_ID, "textures/rider/w/tornado.png");
    }
    
    @Override
    public ResourceLocation getAnimationResource(WTornadoEntity animatable) {
        // 返回动画文件的路径
        return new ResourceLocation(MOD_ID, "animations/rider/w/tornado.animation.json");
    }
}