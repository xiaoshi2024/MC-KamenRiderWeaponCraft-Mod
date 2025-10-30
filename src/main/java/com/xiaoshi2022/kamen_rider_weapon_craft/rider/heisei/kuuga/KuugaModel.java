package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kuuga;


import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KuugaModel extends GeoModel<KuugaRiderEntity> {
    @Override
    public ResourceLocation getModelResource(KuugaRiderEntity animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "geo/rider/kuuga/kuuga_mighty.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KuugaRiderEntity animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "textures/rider/kuuga/kuuga_mighty.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KuugaRiderEntity animatable) {
        return new ResourceLocation("kamen_rider_weapon_craft", "animations/rider/kuuga/kuuga_mighty.animation.json");
    }
}