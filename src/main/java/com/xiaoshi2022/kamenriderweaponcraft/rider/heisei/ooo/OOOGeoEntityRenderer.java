package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.ooo;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class OOOGeoEntityRenderer extends GeoEntityRenderer<OOOGeoEntity> {
    
    public OOOGeoEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new OOOGeoModel());
        this.shadowRadius = 0.5f;
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;
    }
    
    @Override
    protected float getDeathMaxRotation(OOOGeoEntity entityLivingBaseIn) {
        return 0.0f;
    }
}