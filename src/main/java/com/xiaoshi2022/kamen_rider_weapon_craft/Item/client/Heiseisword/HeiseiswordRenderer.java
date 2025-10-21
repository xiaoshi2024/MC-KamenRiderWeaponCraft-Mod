package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.Heiseisword;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HeiseiswordRenderer extends GeoItemRenderer<Heiseisword> {
    public HeiseiswordRenderer() {
        super(new HeiseiswordModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"heiseisword")));
    }
}
