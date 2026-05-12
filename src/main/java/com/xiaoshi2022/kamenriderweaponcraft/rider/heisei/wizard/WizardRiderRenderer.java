package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.wizard;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WizardRiderRenderer extends GeoEntityRenderer<WizardRiderEntity> {
    
    public WizardRiderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WizardRiderModel());
        this.shadowRadius = 1.5f;
    }
    
    public float getScale(WizardRiderEntity entity) {
        if (entity.getDragonMagicType() != null) {
            switch (entity.getDragonMagicType()) {
                case FlameDragon:
                    return 2.0f;
                case WaterDragon:
                    return 1.8f;
                case HurricaneDragon:
                    return 2.2f;
                case LandDragon:
                    return 1.9f;
                default:
                    return 1.0f;
            }
        }
        return 1.0f;
    }
}