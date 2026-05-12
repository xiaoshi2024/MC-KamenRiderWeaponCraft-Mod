package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.hibiki;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HibikiDrumEffectRenderer extends GeoEntityRenderer<HibikiDrumEffectEntity> {
    
    public HibikiDrumEffectRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HibikiDrumEffectModel());
        this.shadowRadius = 0.5f;
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;
    }
    
    @Override
    protected float getDeathMaxRotation(HibikiDrumEffectEntity entityLivingBaseIn) {
        return 0.0f;
    }
}