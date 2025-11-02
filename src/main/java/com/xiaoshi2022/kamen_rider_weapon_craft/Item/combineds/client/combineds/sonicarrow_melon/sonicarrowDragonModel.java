package com.xiaoshi2022.kamen_rider_weapon_craft.Item.combineds.client.combineds.sonicarrow_melon;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class sonicarrowDragonModel extends GeoModel<sonicarrow> {

    @Override
    public ResourceLocation getAnimationResource(sonicarrow animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "animations/item/sonic_arrow_dragon.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(sonicarrow animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "geo/item/sonic_arrow_dragon.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(sonicarrow animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/item/sonic_arrow_dragon.png");
    }
}