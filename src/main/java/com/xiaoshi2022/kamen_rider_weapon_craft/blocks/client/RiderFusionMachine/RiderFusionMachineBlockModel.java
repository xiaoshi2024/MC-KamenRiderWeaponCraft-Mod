package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachine;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachineBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;


public class RiderFusionMachineBlockModel extends GeoModel<RiderFusionMachineBlockEntity> {
    @Override
    public ResourceLocation getAnimationResource(RiderFusionMachineBlockEntity animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "animations/block/riderfusionmachine.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(RiderFusionMachineBlockEntity animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "geo/block/riderfusionmachine.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RiderFusionMachineBlockEntity animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/block/riderfusionmachine.png");
    }
}
