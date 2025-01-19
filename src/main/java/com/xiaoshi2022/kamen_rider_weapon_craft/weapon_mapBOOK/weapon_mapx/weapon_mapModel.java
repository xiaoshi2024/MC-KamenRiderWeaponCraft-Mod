package com.xiaoshi2022.kamen_rider_weapon_craft.weapon_mapBOOK.weapon_mapx;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedGeoModel;

public class weapon_mapModel<T extends GeoAnimatable> extends DefaultedGeoModel<T> {
    public weapon_mapModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    protected String subtype() {
        return "item";
    }

    @Override
    public weapon_mapModel<T> withAltModel(ResourceLocation altPath) {
        return (weapon_mapModel<T>) super.withAltModel(new ResourceLocation("kamen_rider_weapon_craft", "geo/item/weapon_map.geo.json"));
    }

    @Override
    public weapon_mapModel<T> withAltAnimations(ResourceLocation altPath) {
        return (weapon_mapModel<T>) super.withAltAnimations(new ResourceLocation("kamen_rider_weapon_craft", "animations/item/weapon_map.animation.json"));
    }

    @Override
    public weapon_mapModel<T> withAltTexture(ResourceLocation altPath) {
        return (weapon_mapModel<T>) super.withAltTexture(new ResourceLocation("kamen_rider_weapon_craft", "textures/item/weapon_map.png"));
    }
}
