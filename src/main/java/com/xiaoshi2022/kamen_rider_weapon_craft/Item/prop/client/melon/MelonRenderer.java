package com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.melon;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.custom.Melon;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class MelonRenderer extends GeoItemRenderer<Melon> {
    public MelonRenderer() {
        super(new MelonModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"melon")));
    }
}
