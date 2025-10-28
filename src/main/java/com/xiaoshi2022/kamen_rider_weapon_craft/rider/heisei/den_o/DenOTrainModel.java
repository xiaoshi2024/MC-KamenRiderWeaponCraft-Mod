package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.den_o;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;


public class DenOTrainModel extends GeoModel<DenOTrainEntity> {
    // 根据要求，只实现圣剑模型
    private static final String SWORD_MODEL = "geo/rider/den_o/den_o_sword.geo.json";
    private static final String SWORD_ANIMATION = "animations/rider/den_o/den_o_sword.animation.json";

    @Override
    public ResourceLocation getModelResource(DenOTrainEntity entity) {
        return new ResourceLocation(MOD_ID, SWORD_MODEL);
    }

    @Override
    public ResourceLocation getTextureResource(DenOTrainEntity entity) {
        return new ResourceLocation(MOD_ID, "textures/rider/den_o//den_o_sword.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DenOTrainEntity entity) {
        return new ResourceLocation(MOD_ID, SWORD_ANIMATION);
    }
    
    /**
     * 设置当前武器类型（保留接口但只处理圣剑）
     * @param weaponType 武器类型
     */
    public void setCurrentWeaponType(String weaponType) {
        // 按照要求，这里只处理圣剑模型，忽略其他类型
    }
    
    /**
     * 直接获取圣剑模型的方法，不依赖武器类型
     * @return 圣剑模型的资源位置
     */
    public ResourceLocation getSwordModelLocation() {
        return getModelResource(null);
    }
    
    /**
     * 直接获取圣剑纹理的方法，不依赖武器类型
     * @return 圣剑纹理的资源位置
     */
    public ResourceLocation getSwordTextureLocation() {
        return getTextureResource(null);
    }
    
    /**
     * 直接获取圣剑动画的方法，不依赖武器类型
     * @return 圣剑动画的资源位置
     */
    public ResourceLocation getSwordAnimationFileLocation() {
        return getAnimationResource(null);
    }
}