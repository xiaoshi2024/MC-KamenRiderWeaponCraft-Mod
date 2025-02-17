package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.gavvwhipir;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.gangunsaber.gangunsaberModel;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.gangunsaber;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.gavvwhipir;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class gavvwhipirRenderer  extends GeoItemRenderer<gavvwhipir> {
    public gavvwhipirRenderer() {
        super(new gavvwhipirModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"gavvwhipir")));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}