package com.xiaoshi2022.kamen_rider_weapon_craft.Item.combineds.client.combineds.sonicarrow_melon;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class sonicarrowMelonModel extends GeoModel<sonicarrow> {
    @Override
    public ResourceLocation getModelResource(sonicarrow animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "geo/item/sonicarrow-melon.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(sonicarrow animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/item/sonicarrow-melon.png");
    }

    @Override
    public ResourceLocation getAnimationResource(sonicarrow animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "animations/item/sonicarrow-melon.animation.json");
    }
}