package com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.HinawaDaidai_DJ_Ju;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.HinawaDaidai_DJ_Ju;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HinawaDaidai_DJ_JuRenderer extends GeoItemRenderer<HinawaDaidai_DJ_Ju> {
    public HinawaDaidai_DJ_JuRenderer() {
        super(new HinawaDaidai_DJ_JuModel<>(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"hinawadai_dai_dj_ju")));
    }
}

