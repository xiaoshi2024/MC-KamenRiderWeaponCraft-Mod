package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.display.Time_traveler_studio_block;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.display.time_traveler_studio_item;
import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class time_traveler_studio_block_itemModel extends GeoModel<time_traveler_studio_item> {
    @Override
    public ResourceLocation getAnimationResource(time_traveler_studio_item animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "animations/item/time_traveler_studio_block_item.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(time_traveler_studio_item animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "geo/item/time_traveler_studio_block_item.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(time_traveler_studio_item entity) {
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/block/time_traveler_studio_block.png");
    }
}
