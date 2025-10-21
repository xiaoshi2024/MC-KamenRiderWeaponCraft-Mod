package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.wizard;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WizardRiderModel extends GeoModel<WizardRiderEntity> {
    // 人形魔龙模型基本资源路径
    private static final String BASE_PATH = "dragon_wizard";
    
    @Override
    public ResourceLocation getModelResource(WizardRiderEntity animatable) {
        // 只使用一个人形魔龙模型，不再根据类型区分
        return new ResourceLocation("kamen_rider_weapon_craft", "geo/rider/wizard/" + BASE_PATH + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WizardRiderEntity animatable) {
        // 只使用一个人形魔龙纹理
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/rider/wizard/" + BASE_PATH + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(WizardRiderEntity animatable) {
        // 返回统一的动画文件，一个动画文件可以包含多个动画序列
        return new ResourceLocation("kamen_rider_weapon_craft", "animations/rider/wizard/" + BASE_PATH + ".animation.json");
    }
    
    // 注意：需要创建以下资源文件：
    // 1. geo文件：
    //    - dragon_wizard.geo.json (单个统一的人形魔龙模型)
    // 2. 纹理文件：
    //    - dragon_wizard.png (单个统一的人形魔龙纹理)
    // 3. 动画文件：
    //    - dragon_wizard.animation.json (包含所有四种元素魔龙的动画序列)
    //    
    // 动画文件中应包含以下动画序列名称：
    // - flamedragon
    // - waterdragon
    // - hurricanedragon
    // - landdragon
}