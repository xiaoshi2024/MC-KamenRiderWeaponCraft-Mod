package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.drive;

import software.bernie.geckolib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * Kamen Rider Drive 实体渲染器
 * 用于渲染Drive骑士的车轮特效实体
 */
public class DriveRiderEntityRenderer extends GeoEntityRenderer<DriveRiderEntity> {
    
    public DriveRiderEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DriveRiderModel());
        // 设置阴影半径
        this.shadowRadius = 0.5f;
        // 设置模型缩放
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;
    }
    
    @Override
    protected float getDeathMaxRotation(DriveRiderEntity entityLivingBaseIn) {
        // 死亡动画的最大旋转角度
        return 0.0f; // 我们的特效实体不需要旋转
    }

}