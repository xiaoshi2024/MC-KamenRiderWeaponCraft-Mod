package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.decade;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Kamen Rider Decade 实体渲染器
 * 用于渲染Decade骑士的次元踢特效实体
 */
public class DecadeRiderEntityRenderer extends GeoEntityRenderer<DecadeRiderEntity> {
    
    public DecadeRiderEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DecadeRiderModel());
        // 设置阴影半径
        this.shadowRadius = 0.7f;
        // 设置模型缩放
        this.scaleHeight = 1.2f;
        this.scaleWidth = 1.2f;
    }
    
    @Override
    protected float getDeathMaxRotation(DecadeRiderEntity entityLivingBaseIn) {
        // 死亡动画的最大旋转角度
        return 0.0f; // 特效实体不需要旋转
    }
}