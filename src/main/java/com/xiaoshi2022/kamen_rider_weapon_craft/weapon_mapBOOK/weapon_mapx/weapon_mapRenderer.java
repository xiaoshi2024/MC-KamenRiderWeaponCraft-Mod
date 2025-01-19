package com.xiaoshi2022.kamen_rider_weapon_craft.weapon_mapBOOK.weapon_mapx;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.weapon_mapBOOK.weapon_map;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class weapon_mapRenderer  extends GeoItemRenderer<weapon_map> {
    public weapon_mapRenderer() {
        super(new weapon_mapModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "weapon_map")));
    }
}