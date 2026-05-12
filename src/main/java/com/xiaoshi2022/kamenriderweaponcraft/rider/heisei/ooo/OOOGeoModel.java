package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.ooo;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class OOOGeoModel extends GeoModel<OOOGeoEntity> {
    
    @Override
    public ResourceLocation getModelResource(OOOGeoEntity object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/ooo/ooo_geo.geo.json");
    }
    
    @Override
    public ResourceLocation getTextureResource(OOOGeoEntity object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/ooo/ooo_geo.png");
    }
    
    @Override
    public ResourceLocation getAnimationResource(OOOGeoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/ooo/ooo_geo.animation.json");
    }
}