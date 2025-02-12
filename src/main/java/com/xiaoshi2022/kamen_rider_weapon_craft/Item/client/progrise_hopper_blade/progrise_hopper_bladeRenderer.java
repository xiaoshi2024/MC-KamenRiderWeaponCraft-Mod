package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.progrise_hopper_blade;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.progrise_hopper_blade;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class progrise_hopper_bladeRenderer extends GeoItemRenderer<progrise_hopper_blade> {
    public progrise_hopper_bladeRenderer() {
        super(new progrise_hopper_bladeModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"progrise_hopper_blade")));
    }
}
