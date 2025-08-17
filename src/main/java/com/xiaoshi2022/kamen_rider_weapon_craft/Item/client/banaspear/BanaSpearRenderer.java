package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.banaspear;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.BanaSpear;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BanaSpearRenderer extends GeoItemRenderer<BanaSpear> {

    public BanaSpearRenderer() {
        super(new BanaSpearModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"bana_spear")));
    }
}
