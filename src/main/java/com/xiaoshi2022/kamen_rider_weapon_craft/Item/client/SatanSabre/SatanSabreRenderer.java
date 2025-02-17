package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.SatanSabre;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.satan_sabre;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class SatanSabreRenderer  extends GeoItemRenderer<satan_sabre> {
    public SatanSabreRenderer() {
        super(new SatanSabreModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"satan_sabre")));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
