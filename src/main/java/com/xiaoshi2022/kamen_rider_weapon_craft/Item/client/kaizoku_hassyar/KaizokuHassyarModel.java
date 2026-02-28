package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.kaizoku_hassyar;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.KaizokuHassyar;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KaizokuHassyarModel extends GeoModel<KaizokuHassyar> {
    @Override
    public ResourceLocation getModelResource(KaizokuHassyar animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "geo/item/kaizoku_hassyar.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KaizokuHassyar animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/item/kaizoku_hassyar.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KaizokuHassyar animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "animations/item/kaizoku_hassyar.animation.json");
    }
}