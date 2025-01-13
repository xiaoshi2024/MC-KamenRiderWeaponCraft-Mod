package com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.arrowx;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.prop.arrowx.AonicxEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AonicxModel extends GeoModel<AonicxEntity>{

    @Override
    public ResourceLocation getModelResource(AonicxEntity aonicxEntity) {
        return new ResourceLocation("kamen_rider_weapon_craft","geo/arrowx/sonicx_arrow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AonicxEntity aonicxEntity) {
        return new ResourceLocation("kamen_rider_weapon_craft","textures/entity/arrowx/sonicx_arrow.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AonicxEntity aonicxEntity) {
        return new ResourceLocation("kamen_rider_weapon_craft","animations/arrowx/sonicx_arrow.animation.json");
    }
}
