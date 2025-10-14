package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.destroy_fifty_swords;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.destroy_fifty_swords;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class destroy_fifty_swordsRenderer extends GeoItemRenderer<destroy_fifty_swords> {
    public destroy_fifty_swordsRenderer() {
        super(new destroy_fifty_swordsModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"destroy_fifty_swords")));
    }
}
