package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.den_o;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DenOTrainModel extends GeoModel<DenOTrainEntity> {
    private static final String SWORD_MODEL = "geo/rider/den_o/den_o_sword.geo.json";
    private static final String SWORD_ANIMATION = "animations/rider/den_o/den_o_sword.animation.json";

    @Override
    public ResourceLocation getModelResource(DenOTrainEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, SWORD_MODEL);
    }

    @Override
    public ResourceLocation getTextureResource(DenOTrainEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/rider/den_o/den_o_sword.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DenOTrainEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, SWORD_ANIMATION);
    }
    
    public void setCurrentWeaponType(String weaponType) {
    }
    
    public ResourceLocation getSwordModelLocation() {
        return getModelResource(null);
    }
    
    public ResourceLocation getSwordTextureLocation() {
        return getTextureResource(null);
    }
    
    public ResourceLocation getSwordAnimationFileLocation() {
        return getAnimationResource(null);
    }
}