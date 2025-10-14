package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.helheim_crack;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.helheim_crackBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class helheim_crackModel extends GeoModel<helheim_crackBlockEntity> {
    @Override
    public ResourceLocation getModelResource(helheim_crackBlockEntity animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "geo/block/helheim_crack.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(helheim_crackBlockEntity animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/block/helheim_crack.png");
    }

    @Override
    public ResourceLocation getAnimationResource(helheim_crackBlockEntity animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "animations/block/helheim_crack.animation.json");
    }

    @Override
    public RenderType getRenderType(helheim_crackBlockEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
