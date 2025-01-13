package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.SatanSabre;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.SatanSabre;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SatanSabreRenderer  extends GeoItemRenderer<SatanSabre> {
    public SatanSabreRenderer() {
        super(new SatanSabreModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"satan_sabre")));
    }
}
