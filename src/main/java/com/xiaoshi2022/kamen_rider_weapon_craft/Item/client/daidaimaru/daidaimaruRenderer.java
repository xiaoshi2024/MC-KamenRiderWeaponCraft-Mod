package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.daidaimaru;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.daidaimaru;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class daidaimaruRenderer extends GeoItemRenderer<daidaimaru> {
    public daidaimaruRenderer() {
        super(new daidaimaruModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"daidaimaru")));
    }
}
