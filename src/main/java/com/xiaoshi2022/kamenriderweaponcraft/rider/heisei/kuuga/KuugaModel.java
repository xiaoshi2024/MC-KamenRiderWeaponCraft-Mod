package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.kuuga;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KuugaModel extends GeoModel<KuugaRiderEntity> {
    
    @Override
    public ResourceLocation getModelResource(KuugaRiderEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/rider/kuuga/kuuga_mighty.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KuugaRiderEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/kuuga/kuuga_mighty.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KuugaRiderEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/rider/kuuga/kuuga_mighty.animation.json");
    }
}