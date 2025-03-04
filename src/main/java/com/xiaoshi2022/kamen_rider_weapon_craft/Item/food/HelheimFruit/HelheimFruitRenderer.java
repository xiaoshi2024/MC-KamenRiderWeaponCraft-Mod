package com.xiaoshi2022.kamen_rider_weapon_craft.Item.food.HelheimFruit;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.food.HelheimFruit;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HelheimFruitRenderer extends GeoItemRenderer<HelheimFruit> {
    public HelheimFruitRenderer() {
        super(new HelheimFruitModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"helheimfruit")));
    }
}
