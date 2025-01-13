package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.sonicarrow;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class sonicarrowModel extends GeoModel<sonicarrow> {
    @Override
    public ResourceLocation getModelResource(sonicarrow animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "geo/item/sonicarrow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(sonicarrow animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/item/sonicarrow.png");
    }

    @Override
    public ResourceLocation getAnimationResource(sonicarrow animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "animations/item/sonicarrow.animation.json");
    }
}