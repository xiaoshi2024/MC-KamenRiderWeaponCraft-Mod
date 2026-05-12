package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.drive;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DriveRiderModel extends GeoModel<DriveRiderEntity> {

    @Override
    public ResourceLocation getModelResource(DriveRiderEntity animatable) {
        if (animatable == null) {
            return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/drive/effect16_engineer.geo.json");
        }
        
        DriveRiderEntity.WheelType type = animatable.getWheelType();
        switch (type) {
            case FIRE:
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/drive/effect16_fire.geo.json");
            case NINJA:
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/drive/effect16_ninja.geo.json");
            case ENGINEER:
            default:
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/drive/effect16_engineer.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(DriveRiderEntity animatable) {
        if (animatable == null) {
            return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/drive/effect16_engineer.png");
        }
        
        DriveRiderEntity.WheelType type = animatable.getWheelType();
        switch (type) {
            case FIRE:
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/drive/effect16_fire.png");
            case NINJA:
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/drive/effect16_ninja.png");
            case ENGINEER:
            default:
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/drive/effect16_engineer.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(DriveRiderEntity animatable) {
        if (animatable == null) {
            return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/drive/effect16_engineer.animation.json");
        }
        
        DriveRiderEntity.WheelType type = animatable.getWheelType();
        switch (type) {
            case FIRE:
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/drive/effect16_fire.animation.json");
            case NINJA:
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/drive/effect16_ninja.animation.json");
            case ENGINEER:
            default:
                return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/drive/effect16_engineer.animation.json");
        }
    }
}