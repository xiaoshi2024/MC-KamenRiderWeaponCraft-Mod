package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.build;

import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Kamen Rider Build 实体渲染器
 * 用于渲染Build骑士的geo实体特效
 */
public class BuildRiderEntityRenderer extends GeoEntityRenderer<BuildRiderEntity> {
    
    public BuildRiderEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BuildRiderModel());
        // 设置阴影半径
        this.shadowRadius = 0.5f;
        // 设置模型缩放
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;
    }
    
    @Override
    protected float getDeathMaxRotation(BuildRiderEntity entityLivingBaseIn) {
        // 死亡动画的最大旋转角度
        return 0.0f; // 我们的特效实体不需要旋转
    }

}