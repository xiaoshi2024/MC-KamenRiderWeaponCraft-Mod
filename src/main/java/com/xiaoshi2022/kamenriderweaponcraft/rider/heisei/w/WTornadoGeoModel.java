package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.w;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WTornadoGeoModel extends GeoModel<WTornadoEntity> {
    
    @Override
    public ResourceLocation getModelResource(WTornadoEntity object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/w/tornado.geo.json");
    }
    
    @Override
    public ResourceLocation getTextureResource(WTornadoEntity object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/w/tornado.png");
    }
    
    @Override
    public ResourceLocation getAnimationResource(WTornadoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/w/tornado.animation.json");
    }
}