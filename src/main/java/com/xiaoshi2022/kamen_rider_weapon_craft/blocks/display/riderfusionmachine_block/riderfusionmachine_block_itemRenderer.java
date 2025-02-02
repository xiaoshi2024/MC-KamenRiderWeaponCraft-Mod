package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.display.riderfusionmachine_block;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.display.rider_fusion_machine_item;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class riderfusionmachine_block_itemRenderer extends GeoItemRenderer<rider_fusion_machine_item> {
    public riderfusionmachine_block_itemRenderer() {
        super(new riderfusionmachine_itemModel());
    }
}

