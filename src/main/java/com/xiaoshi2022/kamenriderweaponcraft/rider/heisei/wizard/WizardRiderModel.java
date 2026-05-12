package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.wizard;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WizardRiderModel extends GeoModel<WizardRiderEntity> {
    private static final String BASE_PATH = "dragon_wizard";
    
    @Override
    public ResourceLocation getModelResource(WizardRiderEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/wizard/" + BASE_PATH + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WizardRiderEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/wizard/" + BASE_PATH + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(WizardRiderEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/wizard/" + BASE_PATH + ".animation.json");
    }
}