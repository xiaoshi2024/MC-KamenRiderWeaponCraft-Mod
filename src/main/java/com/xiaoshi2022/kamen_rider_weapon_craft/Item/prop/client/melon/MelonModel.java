package com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.melon;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.model.DefaultedGeoModel;

public class MelonModel <T extends GeoAnimatable> extends DefaultedGeoModel<T> {
    public MelonModel(ResourceLocation assetSubpath) {
        super(assetSubpath);
    }

    @Override
    protected String subtype() {
        return "item";
    }
    @Override
    public MelonModel<T> withAltModel(ResourceLocation altPath) {
        return (MelonModel<T>)super.withAltModel(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"geo/item/melon.geo.json"));
    }

    /**
     * Changes the constructor-defined animations path for this model to an alternate.<br>
     * This is useful if your animatable shares an animations path with another animatable that differs in path to the model and texture for this model
     */
    @Override
    public MelonModel<T> withAltAnimations(ResourceLocation altPath) {
        return (MelonModel<T>)super.withAltAnimations(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"animations/item/melon.animation.json"));
    }

    /**
     * Changes the constructor-defined texture path for this model to an alternate.<br>
     * This is useful if your animatable shares a texture path with another animatable that differs in path to the model and animations for this model
     */
    @Override
    public MelonModel<T> withAltTexture(ResourceLocation altPath) {
        return (MelonModel<T>)super.withAltTexture(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"textures/item/melon.png"));
    }
}
