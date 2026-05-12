package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.den_o;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DenOTrainRenderer extends GeoEntityRenderer<DenOTrainEntity> {
    
    public DenOTrainRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DenOTrainModel());
        this.shadowRadius = 0.5f;
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;
    }
    
    @Override
    protected float getDeathMaxRotation(DenOTrainEntity entityLivingBaseIn) {
        return 0.0f;
    }
}