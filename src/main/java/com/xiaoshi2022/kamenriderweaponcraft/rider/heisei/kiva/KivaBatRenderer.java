package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.kiva;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KivaBatRenderer extends GeoEntityRenderer<KivaBatEntity> {
    
    public KivaBatRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KivaBatModel());
        this.shadowRadius = 0.5f;
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;
    }
    
    @Override
    protected float getDeathMaxRotation(KivaBatEntity entityLivingBaseIn) {
        return 0.0f;
    }
}