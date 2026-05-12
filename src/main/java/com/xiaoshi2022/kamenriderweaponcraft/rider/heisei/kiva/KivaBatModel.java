package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.kiva;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KivaBatModel extends GeoModel<KivaBatEntity> {
    
    @Override
    public ResourceLocation getModelResource(KivaBatEntity object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/kiva/kiva_bat.geo.json");
    }
    
    @Override
    public ResourceLocation getTextureResource(KivaBatEntity object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/kiva/kiva_bat.png");
    }
    
    @Override
    public ResourceLocation getAnimationResource(KivaBatEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/kiva/kiva_bat.animation.json");
    }
}