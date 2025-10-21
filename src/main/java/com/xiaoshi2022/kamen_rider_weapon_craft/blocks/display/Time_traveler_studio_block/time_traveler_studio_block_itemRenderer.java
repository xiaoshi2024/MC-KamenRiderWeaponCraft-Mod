package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.display.Time_traveler_studio_block;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.display.time_traveler_studio_item;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class time_traveler_studio_block_itemRenderer extends GeoItemRenderer<time_traveler_studio_item> {
    public time_traveler_studio_block_itemRenderer() {
        super(new time_traveler_studio_block_itemModel());
    }
}
