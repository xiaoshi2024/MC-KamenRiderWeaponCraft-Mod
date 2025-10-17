package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.wizard;

import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class WizardRiderEntityRenderer extends GeoEntityRenderer<WizardRiderEntity> {
    
    public WizardRiderEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WizardRiderModel());
        // 设置更大的阴影半径，符合人形魔龙的体型
        this.shadowRadius = 1.5f;
    }
    
    // 根据不同元素魔龙类型设置不同的缩放比例
    public float getScale(WizardRiderEntity entity) {
        // 根据元素魔龙类型设置不同的缩放比例
        if (entity.getDragonMagicType() != null) {
            switch (entity.getDragonMagicType()) {
                case FlameDragon:
                    return 2.0f; // 火焰魔龙体积较大
                case WaterDragon:
                    return 1.8f;
                case HurricaneDragon:
                    return 2.2f; // 飓风魔龙体积最大，给人一种轻盈但庞大的感觉
                case LandDragon:
                    return 1.9f; // 土地魔龙较为厚重
                default:
                    return 1.0f;
            }
        }
        return 1.0f;
    }
}