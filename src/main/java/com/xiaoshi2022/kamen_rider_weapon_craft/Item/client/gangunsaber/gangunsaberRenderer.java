package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.gangunsaber;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.gangunsaber;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class gangunsaberRenderer  extends GeoItemRenderer<gangunsaber> {
    public gangunsaberRenderer() {
        super(new gangunsaberModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"gangunsaber")));
    }
}