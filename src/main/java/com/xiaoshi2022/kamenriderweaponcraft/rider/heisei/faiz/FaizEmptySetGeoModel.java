package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.faiz;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FaizEmptySetGeoModel extends GeoModel<FaizEmptySetEntity> {
    @Override
    public ResourceLocation getModelResource(FaizEmptySetEntity object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/faiz/faiz_empty_set.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FaizEmptySetEntity object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/faiz/faiz_empty_set.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FaizEmptySetEntity object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/faiz/faiz_empty_set.animation.json");
    }
}