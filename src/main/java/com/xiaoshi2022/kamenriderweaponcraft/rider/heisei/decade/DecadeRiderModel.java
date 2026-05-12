package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.decade;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DecadeRiderModel extends GeoModel<DecadeRiderEntity> {

    @Override
    public ResourceLocation getModelResource(DecadeRiderEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/decade/dcd.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DecadeRiderEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/decade/dcd.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DecadeRiderEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/decade/dcd.animation.json");
    }
}