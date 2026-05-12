package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.exaid;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft.MODID;


public class ExAidSlashEffectModel extends GeoModel<ExAidSlashEffectEntity> {

    @Override
    public ResourceLocation getModelResource(ExAidSlashEffectEntity object) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "geo/rider/exaid/effect18.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ExAidSlashEffectEntity object) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/rider/exaid/effect18.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ExAidSlashEffectEntity object) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "animations/rider/exaid/effect18.animation.json");
    }
}
