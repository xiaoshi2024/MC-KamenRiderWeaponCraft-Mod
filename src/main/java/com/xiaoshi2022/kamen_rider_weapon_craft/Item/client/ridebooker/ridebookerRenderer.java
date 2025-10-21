package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.ridebooker;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.ridebooker;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ridebookerRenderer extends GeoItemRenderer<ridebooker> {
    public ridebookerRenderer() {
        super(new ridebookerModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"ridebooker")));
    }
}

