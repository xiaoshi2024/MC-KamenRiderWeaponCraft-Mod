package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.drive;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DriveRiderRenderer extends GeoEntityRenderer<DriveRiderEntity> {
    
    public DriveRiderRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DriveRiderModel());
        this.shadowRadius = 0.5f;
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;
    }
    
    @Override
    protected float getDeathMaxRotation(DriveRiderEntity entityLivingBaseIn) {
        return 0.0f;
    }
}