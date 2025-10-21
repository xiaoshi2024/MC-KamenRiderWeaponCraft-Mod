package com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.cheryy;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.custom.cheryy;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class cheryyRenderer  extends GeoItemRenderer<cheryy> {
    public cheryyRenderer() {
        super(new cheryyModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"cheryy")));
    }
}

