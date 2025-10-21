package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

public class ExAidSlashEffectModel extends GeoModel<ExAidSlashEffectEntity> {
    @Override
    public ResourceLocation getModelResource(ExAidSlashEffectEntity object) {
        // 加载之前创建的Geo模型文件
        return new ResourceLocation(MOD_ID, "geo/rider/exaid/effect18.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ExAidSlashEffectEntity object) {
        // 返回特效的纹理文件，与渲染器使用相同的路径
        return new ResourceLocation(MOD_ID, "textures/rider/exaid/effect18.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ExAidSlashEffectEntity object) {
        // 加载之前创建的动画文件
        return new ResourceLocation(MOD_ID, "animations/rider/exaid/effect18.animation.json");
    }
}