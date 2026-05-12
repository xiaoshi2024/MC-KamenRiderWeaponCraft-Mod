package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.faiz;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FaizEmptySetRenderer extends GeoEntityRenderer<FaizEmptySetEntity> {
    
    public FaizEmptySetRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FaizEmptySetGeoModel());
        this.shadowRadius = 0.5f;
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;
    }
    
    @Override
    protected float getDeathMaxRotation(FaizEmptySetEntity entityLivingBaseIn) {
        return 0.0f;
    }
}