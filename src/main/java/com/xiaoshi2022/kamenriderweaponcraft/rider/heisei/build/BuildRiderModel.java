package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.build;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BuildRiderModel extends GeoModel<BuildRiderEntity> {

    @Override
    public ResourceLocation getModelResource(BuildRiderEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/build/effect19.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BuildRiderEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/build/effect19.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BuildRiderEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/build/effect19.animation.json");
    }
}
