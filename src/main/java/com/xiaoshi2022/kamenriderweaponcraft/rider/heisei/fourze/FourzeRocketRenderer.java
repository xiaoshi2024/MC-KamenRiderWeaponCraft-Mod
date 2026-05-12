package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.fourze;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FourzeRocketRenderer extends GeoEntityRenderer<FourzeRocketEntity> {
    
    public FourzeRocketRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FourzeRocketModel());
        this.shadowRadius = 0.5f;
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;
    }
    
    @Override
    protected float getDeathMaxRotation(FourzeRocketEntity entityLivingBaseIn) {
        return 0.0f;
    }
}