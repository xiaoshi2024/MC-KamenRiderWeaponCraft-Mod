package com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.arrowx;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.entity.LaserBeamEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;


public class LaserBeamEntityRenderer extends EntityRenderer<LaserBeamEntity> {
    public LaserBeamEntityRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public ResourceLocation getTextureLocation(LaserBeamEntity p_114482_) {
        return new ResourceLocation("textures/particle/aonicx.png");
    }
}
