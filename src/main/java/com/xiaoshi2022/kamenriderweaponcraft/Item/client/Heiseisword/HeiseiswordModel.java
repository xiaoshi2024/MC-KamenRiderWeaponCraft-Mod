package com.xiaoshi2022.kamenriderweaponcraft.Item.client.Heiseisword;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HeiseiswordModel extends GeoModel<Heiseisword> {
    @Override
    public ResourceLocation getModelResource(Heiseisword object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "geo/item/heiseisword.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Heiseisword object) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/item/heiseisword.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Heiseisword animatable) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "animations/item/heiseisword.animation.json");
    }
}