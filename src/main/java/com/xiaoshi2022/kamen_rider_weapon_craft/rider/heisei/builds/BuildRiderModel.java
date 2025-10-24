package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.builds;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import static com.xiaoshi2022.kamen_rider_weapon_craft.Kamen_Rider_Weapon_Craft.MOD_ID;

public class BuildRiderModel extends GeoModel<BuildRiderEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState geoRenderState) {
        return Identifier.of(MOD_ID, "geckolib/models/rider/builds/effect19.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState geoRenderState) {
        return Identifier.of(MOD_ID, "textures/rider/builds/effect19.png");
    }

    @Override
    public Identifier getAnimationResource(BuildRiderEntity buildsRiderEntity) {
        return Identifier.of(MOD_ID, "geckolib/animations/rider/builds/effect19.animation.json");
    }
}
