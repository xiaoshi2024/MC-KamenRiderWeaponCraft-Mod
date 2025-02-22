package com.xiaoshi2022.kamen_rider_weapon_craft.datagen.loot;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.add(ModBlocks.PINE_LEAVES.get(), block ->
                createLeavesDrops(block, ModBlocks.PINE_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));

        this.dropSelf(ModBlocks.PINE_SAPLING.get());

    }
}
