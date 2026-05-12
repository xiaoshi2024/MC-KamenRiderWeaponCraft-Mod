package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.w;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WTornadoRenderer extends GeoEntityRenderer<WTornadoEntity> {
    
    public WTornadoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WTornadoGeoModel());
        this.shadowRadius = 0.5f;
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;
    }
    
    @Override
    protected float getDeathMaxRotation(WTornadoEntity entityLivingBaseIn) {
        return 0.0f;
    }
}