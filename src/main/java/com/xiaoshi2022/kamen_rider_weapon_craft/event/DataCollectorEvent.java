package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.villagers.TimeTravelerProfessionPoiTypeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataCollectorEvent {
    @SubscribeEvent
    public static void DataCollect(GatherDataEvent gatherDataEvent){
        DataGenerator dataGenerator = gatherDataEvent.getGenerator();
        PackOutput packOutput = gatherDataEvent.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> providerCompletableFuture = gatherDataEvent.getLookupProvider();
        ExistingFileHelper existingFileHelper = gatherDataEvent.getExistingFileHelper();
        dataGenerator.addProvider(gatherDataEvent.includeServer(), new TimeTravelerProfessionPoiTypeTags(packOutput,providerCompletableFuture,kamen_rider_weapon_craft.MOD_ID,existingFileHelper));
    }
}
