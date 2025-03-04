package com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.biome;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import terrablender.api.Regions;

public class ModTerrablender {
    public static void registerBiomes() {
        Regions.register(new ModOverworldRegion(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "overworld"), 5));
    }
}
