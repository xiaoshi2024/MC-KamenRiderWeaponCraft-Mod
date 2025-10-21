package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ooo;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Kamen Rider OOO Geo实体渲染器
 * 用于在客户端渲染细胞硬币斩实体
 */
public class OOOGeoEntityRenderer extends GeoEntityRenderer<OOOGeoEntity> {
    
    public OOOGeoEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new OOOGeoModel());
        this.shadowRadius = 0.3f; // 设置阴影半径
    }
    
    @Override
    protected float getDeathMaxRotation(OOOGeoEntity entityLivingBaseIn) {
        // 死亡动画的最大旋转角度
        return 0.0F;
    }

}