package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.authorize_buster;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.authorize_buster;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class authorize_busterRenderer extends GeoItemRenderer<authorize_buster> {
    public authorize_busterRenderer() {
        super(new authorize_busterModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"authorize_buster")));
    }
}
