package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.gaim;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GaimLockSeedModel extends GeoModel<GaimLockSeedEntity> {
    @Override
    public ResourceLocation getModelResource(GaimLockSeedEntity object) {
        String lockSeedType = object.getLockSeedType().toLowerCase();
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/gaim/" + lockSeedType + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GaimLockSeedEntity object) {
        String lockSeedType = object.getLockSeedType().toLowerCase();
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/gaim/" + lockSeedType + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(GaimLockSeedEntity object) {
        String lockSeedType = object.getLockSeedType().toLowerCase();
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/gaim/" + lockSeedType + ".animation.json");
    }
}