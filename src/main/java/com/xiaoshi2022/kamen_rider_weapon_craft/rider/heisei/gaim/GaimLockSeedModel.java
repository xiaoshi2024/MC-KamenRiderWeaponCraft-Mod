package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.gaim;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

/**
 * 铠武锁种模型类
 * 根据锁种类型动态加载不同的模型、纹理和动画
 */
public class GaimLockSeedModel extends GeoModel<GaimLockSeedEntity> {
    @Override
    public ResourceLocation getModelResource(GaimLockSeedEntity object) {
        // 根据锁种类型加载对应的模型文件
        String lockSeedType = object.getLockSeedType().toLowerCase();
        return new ResourceLocation(MOD_ID, "geo/rider/gaim/" + lockSeedType + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GaimLockSeedEntity object) {
        // 根据锁种类型加载对应的纹理文件
        String lockSeedType = object.getLockSeedType().toLowerCase();
        return new ResourceLocation(MOD_ID, "textures/rider/gaim/" + lockSeedType + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(GaimLockSeedEntity object) {
        // 根据锁种类型加载对应的动画文件
        String lockSeedType = object.getLockSeedType().toLowerCase();
        return new ResourceLocation(MOD_ID, "animations/rider/gaim/" + lockSeedType + ".animation.json");
    }
}