package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.needlekunai;

import software.bernie.geckolib.model.GeoModel;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.NeedleKunai;
import net.minecraft.resources.ResourceLocation;

public class NeedleKunaiModel extends GeoModel<NeedleKunai> {
    @Override
    public ResourceLocation getModelResource(NeedleKunai animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "geo/item/needle_kunai.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NeedleKunai animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/item/needle_kunai.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NeedleKunai animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "animations/item/needle_kunai.animation.json");
    }
}