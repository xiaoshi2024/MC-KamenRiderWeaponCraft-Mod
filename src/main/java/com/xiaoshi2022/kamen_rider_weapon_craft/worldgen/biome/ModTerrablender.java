package com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.biome;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import terrablender.api.Regions;

@Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModTerrablender {

    @Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class SetupHandler {
        @SubscribeEvent
        public static void onCommonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                // 在这里注册生物群系
                Regions.register(new ModOverworldRegion(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "overworld"), 5));
            });
        }
    }
}