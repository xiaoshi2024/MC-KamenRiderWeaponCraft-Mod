package com.xiaoshi2022.kamen_rider_weapon_craft.datagen;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, kamen_rider_weapon_craft.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 添加到 logs_that_burn 标签
        this.tag(BlockTags.LOGS_THAT_BURN)
                .add(ModBlocks.PINE_LOG.get(), ModBlocks.PINE_WOOD.get(),
                        ModBlocks.STRIPPED_PINE_LOG.get(), ModBlocks.STRIPPED_PINE_WOOD.get());

        // 添加到 stripped_logs 和 stripped_wood 标签
        this.tag(BlockTags.LOGS)
                .add(ModBlocks.STRIPPED_PINE_LOG.get());
        this.tag(BlockTags.LOGS)
                .add(ModBlocks.STRIPPED_PINE_WOOD.get());
        // 添加到 planks 标签
        this.tag(BlockTags.PLANKS)
                .add(ModBlocks.PINE_PLANKS.get());
        this.tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.PINE_PLANKS.get());
        // 添加自定义树干方块到 minecraft:logs 标签
        this.tag(ModTags.Blocks.LOGS)
                .add(ModBlocks.PINE_LOG.get());
        // 添加自定义树干方块到 minecraft:logs_that_sustain_leaves 标签
        this.tag(ModTags.Blocks.LOGS_THAT_CAN_SUSTAIN_LEAVES)
                .add(ModBlocks.PINE_LOG.get());
        // 添加自定义树叶到自定义标签
        this.tag(ModTags.Blocks.LEAVES)
                .add(ModBlocks.PINE_LEAVES.get());
        // 添加自定义树叶到 minecraft:leaves 标签
        this.tag(BlockTags.LEAVES)
                .add(ModBlocks.PINE_LEAVES.get());
    }
}