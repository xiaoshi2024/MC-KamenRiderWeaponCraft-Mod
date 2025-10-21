package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.ZanvatSword;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.ZanvatSword;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ZanvatSwordRenderer extends GeoItemRenderer<ZanvatSword> {
    public ZanvatSwordRenderer() {
        super(new ZanvatSwordModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"zanvat_sword")));
    }
}
