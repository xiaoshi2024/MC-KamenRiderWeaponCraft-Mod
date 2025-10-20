package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.w;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Kamen Rider W 龙卷风实体渲染器
 * 负责渲染龙卷风特效实体
 */
public class WTornadoRenderer extends GeoEntityRenderer<WTornadoEntity> {
    
    public WTornadoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WTornadoGeoModel());
        this.shadowRadius = 0.3f;
    }
    
    @Override
    protected float getDeathMaxRotation(WTornadoEntity entityLivingBaseIn) {
        return 0.0F;
    }
}