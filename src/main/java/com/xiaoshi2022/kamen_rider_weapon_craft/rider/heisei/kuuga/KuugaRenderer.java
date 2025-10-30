package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kuuga;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 空我实体渲染器
 * 负责渲染Kuuga特效实体
 */
public class KuugaRenderer extends GeoEntityRenderer<KuugaRiderEntity> {
    
    public KuugaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KuugaModel());
        this.shadowRadius = 0.3f;
    }
    
    @Override
    protected float getDeathMaxRotation(KuugaRiderEntity entityLivingBaseIn) {
        return 0.0F;
    }
}