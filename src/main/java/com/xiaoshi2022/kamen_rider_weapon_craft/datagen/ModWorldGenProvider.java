package com.xiaoshi2022.kamen_rider_weapon_craft.datagen;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.ModBiomeModifiers;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.ModConfiguredFeatures;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.ModPlacedFeatures;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.biome.ModBiomes;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.dimension.ModDimensions;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, ModDimensions::bootstrapType)
            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap)
            // 保留生物群系注册，确保维度可以正常使用赫尔海姆生物群系
            // 但移除了主世界的生物群系生成（在ModTerrablender.java中）以避免与Biomes O' Plenty冲突
            .add(Registries.BIOME, ModBiomes::boostrap)
            .add(Registries.LEVEL_STEM, ModDimensions::bootstrapStem);

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(kamen_rider_weapon_craft.MOD_ID));
    }
}