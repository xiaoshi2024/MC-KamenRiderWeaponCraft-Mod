package com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.arrowx;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.prop.arrowx.AonicxEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AonicxRenderer extends GeoEntityRenderer<AonicxEntity> {
   public static final ResourceLocation SONICX_ARROWX = new ResourceLocation("kamen_rider_weapon_craft:textures/entity/arrowx/sonicx_arrow.png");

   public AonicxRenderer(EntityRendererProvider.Context context) {
        super(context,new AonicxModel());
   }

    @Override
    public ResourceLocation getTextureLocation(AonicxEntity aonicxEntity) {
        return SONICX_ARROWX;
    }
}
