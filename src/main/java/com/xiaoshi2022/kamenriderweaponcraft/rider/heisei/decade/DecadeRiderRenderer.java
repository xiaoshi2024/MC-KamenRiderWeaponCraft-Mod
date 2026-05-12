package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.decade;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DecadeRiderRenderer extends GeoEntityRenderer<DecadeRiderEntity> {
    
    public DecadeRiderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DecadeRiderModel());
        this.shadowRadius = 0.7f;
        this.scaleHeight = 1.2f;
        this.scaleWidth = 1.2f;
    }
    
    @Override
    protected float getDeathMaxRotation(DecadeRiderEntity entityLivingBaseIn) {
        return 0.0f;
    }
}