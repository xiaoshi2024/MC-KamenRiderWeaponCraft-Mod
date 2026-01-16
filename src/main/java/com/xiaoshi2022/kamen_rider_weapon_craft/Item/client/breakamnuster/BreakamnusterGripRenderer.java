package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.breakamnuster;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.breakamnuster.BreakamnusterGrip;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BreakamnusterGripRenderer extends GeoItemRenderer<BreakamnusterGrip> {
    public BreakamnusterGripRenderer() {
        super(new BreakamnusterGripModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"breakam_buster_o")));
    }
}