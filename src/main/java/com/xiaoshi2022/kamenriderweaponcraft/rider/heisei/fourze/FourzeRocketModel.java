package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.fourze;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FourzeRocketModel extends GeoModel<FourzeRocketEntity> {
    private static final ResourceLocation MODEL_RESOURCE = ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/fourze/fourze_rocket.geo.json");
    private static final ResourceLocation TEXTURE_RESOURCE = ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/fourze/fourze_rocket.png");
    private static final ResourceLocation ANIMATION_RESOURCE = ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/fourze/fourze_rocket.animation.json");

    @Override
    public ResourceLocation getModelResource(FourzeRocketEntity animatable) {
        return MODEL_RESOURCE;
    }

    @Override
    public ResourceLocation getTextureResource(FourzeRocketEntity animatable) {
        return TEXTURE_RESOURCE;
    }

    @Override
    public ResourceLocation getAnimationResource(FourzeRocketEntity animatable) {
        return ANIMATION_RESOURCE;
    }
}