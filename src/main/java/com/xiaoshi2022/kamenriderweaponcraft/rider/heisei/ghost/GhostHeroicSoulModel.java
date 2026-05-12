package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.ghost;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GhostHeroicSoulModel extends GeoModel<GhostHeroicSoulEntity> {

    @Override
    public ResourceLocation getModelResource(GhostHeroicSoulEntity animatable) {
        if (animatable == null) {
            return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/ghost/musashi.geo.json");
        }

        String soulType = animatable.getSoulType();
        switch (soulType) {
            case "MUSASHI":
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/ghost/musashi.geo.json");
            case "EDISON":
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/ghost/edison.geo.json");
            case "NEWTON":
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/ghost/newton.geo.json");
            default:
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/ghost/musashi.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(GhostHeroicSoulEntity animatable) {
        if (animatable == null) {
            return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/ghost/musashi.png");
        }

        String soulType = animatable.getSoulType();
        switch (soulType) {
            case "MUSASHI":
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/ghost/musashi.png");
            case "EDISON":
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/ghost/edison.png");
            case "NEWTON":
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/ghost/newton.png");
            default:
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/ghost/musashi.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(GhostHeroicSoulEntity animatable) {
        if (animatable == null) {
            return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/ghost/musashi.animation.json");
        }

        String soulType = animatable.getSoulType();
        switch (soulType) {
            case "MUSASHI":
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/ghost/musashi.animation.json");
            case "EDISON":
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/ghost/edison.animation.json");
            case "NEWTON":
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/ghost/newton.animation.json");
            default:
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/ghost/musashi.animation.json");
        }
    }
}
