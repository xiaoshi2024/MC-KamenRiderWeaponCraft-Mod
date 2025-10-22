package com.xiaoshi2022.kamen_rider_weapon_craft.items.client.Heiseisword;

import com.xiaoshi2022.kamen_rider_weapon_craft.items.custom.Heiseisword;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import static com.xiaoshi2022.kamen_rider_weapon_craft.Kamen_Rider_Weapon_Craft.MOD_ID;

public class HeiseiswordRenderer extends GeoItemRenderer<Heiseisword> {
    public HeiseiswordRenderer() {
        super(new DefaultedItemGeoModel<>(Identifier.of(MOD_ID, "heiseisword")));
    }
}