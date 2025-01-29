package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.Time_traveler_studio_block;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.Time_traveler_studio_blockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.example.block.entity.GeckoHabitatBlockEntity;
import software.bernie.geckolib.model.GeoModel;

public class Time_traveler_studio_blockModel extends GeoModel<Time_traveler_studio_blockEntity> {
    @Override
    public ResourceLocation getModelResource(Time_traveler_studio_blockEntity animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "geo/block/time_traveler_studio_block.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Time_traveler_studio_blockEntity animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/block/time_traveler_studio_block.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Time_traveler_studio_blockEntity animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "animations/block/time_traveler_studio_block.animation.json");
    }

    @Override
    public RenderType getRenderType(Time_traveler_studio_blockEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
