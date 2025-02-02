package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.display.riderfusionmachine_block;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.display.rider_fusion_machine_item;
import software.bernie.geckolib.model.GeoModel;

import net.minecraft.resources.ResourceLocation;

public class riderfusionmachine_itemModel extends GeoModel<rider_fusion_machine_item> {
    @Override
    public ResourceLocation getAnimationResource(rider_fusion_machine_item animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "animations/item/rider_fusion_machine_item.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(rider_fusion_machine_item animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "geo/item/rider_fusion_machine_item.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(rider_fusion_machine_item entity) {
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/block/riderfusionmachine.png");
    }
}
