package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.sonicarrow;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class sonicarrowRenderer extends GeoItemRenderer<sonicarrow> {
    public sonicarrowRenderer() {
        super(new sonicarrowModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"sonicarrow")));
    }
}