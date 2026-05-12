package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.build;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BuildRiderEntityRenderer extends GeoEntityRenderer<BuildRiderEntity> {

    public BuildRiderEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BuildRiderModel());
        this.shadowRadius = 0.5f;
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;
    }

    @Override
    protected float getDeathMaxRotation(BuildRiderEntity entityLivingBaseIn) {
        return 0.0f;
    }
}
