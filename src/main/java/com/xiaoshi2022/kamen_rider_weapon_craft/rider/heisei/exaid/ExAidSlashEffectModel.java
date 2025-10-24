package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid;

import com.xiaoshi2022.kamen_rider_weapon_craft.Kamen_Rider_Weapon_Craft;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ExAidSlashEffectModel extends GeoModel<ExAidSlashEffectEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState geoRenderState) {
        return Identifier.of(Kamen_Rider_Weapon_Craft.MOD_ID, "geckolib/models/rider/exaid/effect18.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState geoRenderState) {
        return Identifier.of(Kamen_Rider_Weapon_Craft.MOD_ID, "textures/rider/exaid/effect18.png");
    }

    @Override
    public Identifier getAnimationResource(ExAidSlashEffectEntity exaidRiderEntity) {
        return Identifier.of(Kamen_Rider_Weapon_Craft.MOD_ID, "geckolib/animations/rider/exaid/effect18.animation.json");
    }
}
