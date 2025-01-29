package com.xiaoshi2022.kamen_rider_weapon_craft.villagers;

import com.google.common.collect.ImmutableSet;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import forge.net.mca.mixin.MixinVillagerProfession;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TimeTravelerProfession {
    public static final DeferredRegister<PoiType> POI_TYPE = DeferredRegister.create(ForgeRegistries.POI_TYPES, kamen_rider_weapon_craft.MOD_ID);
    public static final DeferredRegister<VillagerProfession> PROFESSION = DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, kamen_rider_weapon_craft.MOD_ID);

    public static final RegistryObject<PoiType> TRAVELER_STUDIO_POI_TYPE = POI_TYPE.register("traveler_studio_poi_type", () -> new PoiType(
            ImmutableSet.copyOf(ModBlocks.TIME_TRAVELER_STUDIO_BLOCK.get()
                    .getStateDefinition()
                    .getPossibleStates()),
            1,
            1));

    public static final RegistryObject<VillagerProfession> TIME_TRAVELER_PROFESSION = PROFESSION.register("time_traveler_profession",
            () -> new VillagerProfession("time_traveler_profession",
                    poiTypeHolder -> poiTypeHolder.get() == TRAVELER_STUDIO_POI_TYPE.get(),
                    poiTypeHolder -> poiTypeHolder.get() == TRAVELER_STUDIO_POI_TYPE.get(),
                    ImmutableSet.of(ModItems.WEAPON_MAP.get()),
                    ImmutableSet.of(Block.byItem(ModItems.WEAPON_MAP.get())),
                    SoundEvents.VILLAGER_WORK_FLETCHER
                    ));
}
