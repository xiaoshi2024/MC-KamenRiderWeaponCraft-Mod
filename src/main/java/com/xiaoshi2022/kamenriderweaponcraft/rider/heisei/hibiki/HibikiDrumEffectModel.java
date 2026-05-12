package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.hibiki;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HibikiDrumEffectModel extends GeoModel<HibikiDrumEffectEntity> {
    @Override
    public ResourceLocation getModelResource(HibikiDrumEffectEntity object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/hibiki/hibiki_drum.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HibikiDrumEffectEntity object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/hibiki/hibiki_drum.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HibikiDrumEffectEntity object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/hibiki/hibiki_drum.animation.json");
    }
}