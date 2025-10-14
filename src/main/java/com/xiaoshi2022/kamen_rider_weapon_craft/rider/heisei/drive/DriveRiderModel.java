package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.drive;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Kamen Rider Drive 模型类
 * 用于渲染Drive骑士的车轮特效
 * 支持根据车轮类型加载不同的纹理资源
 */
public class DriveRiderModel extends GeoModel<DriveRiderEntity> {

    @Override
    public ResourceLocation getModelResource(DriveRiderEntity animatable) {
        // 根据车轮类型选择不同的模型文件
        if (animatable == null) {
            // 默认使用工程车轮模型
            return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "geo/rider/drive/effect16_engineer.geo.json");
        }
        
        DriveRiderEntity.WheelType type = animatable.getWheelType();
        switch (type) {
            case FIRE:
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "geo/rider/drive/effect16_fire.geo.json");
            case NINJA:
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "geo/rider/drive/effect16_ninja.geo.json");
            case ENGINEER:
            default:
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "geo/rider/drive/effect16_engineer.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(DriveRiderEntity animatable) {
        // 根据车轮类型选择不同的纹理
        if (animatable == null) {
            // 默认使用工程车轮纹理
            return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "textures/rider/drive/effect16_engineer.png");
        }
        
        DriveRiderEntity.WheelType type = animatable.getWheelType();
        switch (type) {
            case FIRE:
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "textures/rider/drive/effect16_fire.png");
            case NINJA:
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "textures/rider/drive/effect16_ninja.png");
            case ENGINEER:
            default:
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "textures/rider/drive/effect16_engineer.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(DriveRiderEntity animatable) {
        // 根据车轮类型选择不同的动画文件
        if (animatable == null) {
            // 默认使用工程车轮动画
            return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "animations/rider/drive/effect16_engineer.animation.json");
        }
        
        DriveRiderEntity.WheelType type = animatable.getWheelType();
        switch (type) {
            case FIRE:
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "animations/rider/drive/effect16_fire.animation.json");
            case NINJA:
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "animations/rider/drive/effect16_ninja.animation.json");
            case ENGINEER:
            default:
                return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "animations/rider/drive/effect16_engineer.animation.json");
        }
    }
}