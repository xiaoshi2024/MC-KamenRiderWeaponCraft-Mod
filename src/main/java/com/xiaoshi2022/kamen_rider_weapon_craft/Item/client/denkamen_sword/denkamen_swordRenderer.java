package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.denkamen_sword;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.denkamen_sword;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class denkamen_swordRenderer extends GeoItemRenderer<denkamen_sword> {
    public denkamen_swordRenderer() {
        super(new denkamen_swordModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"denkamen_sword")));
    }
}
