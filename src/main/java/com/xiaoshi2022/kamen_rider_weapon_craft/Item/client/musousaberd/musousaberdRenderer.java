package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.musousaberd;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class musousaberdRenderer extends GeoItemRenderer<sonicarrow> {
    public musousaberdRenderer() {
        super(new musousaberdModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"musousaberd")));
    }
}

