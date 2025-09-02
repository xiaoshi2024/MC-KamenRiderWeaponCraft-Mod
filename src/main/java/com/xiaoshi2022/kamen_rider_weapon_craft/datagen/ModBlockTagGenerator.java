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
        // 明确设置 replace: false 对于所有标签
        this.tag(BlockTags.LEAVES).add(ModBlocks.PINE_LEAVES.get());
        this.tag(BlockTags.LOGS).add(ModBlocks.PINE_LOG.get());
        this.tag(BlockTags.PLANKS).add(ModBlocks.PINE_PLANKS.get());
        this.tag(BlockTags.LOGS_THAT_BURN).add(ModBlocks.PINE_LOG.get());


        this.tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.PINE_PLANKS.get());
        this.tag(BlockTags.MINEABLE_WITH_HOE).add(ModBlocks.PINE_LEAVES.get());

        // ✅ 添加到自定义标签（可选）
        this.tag(ModTags.Blocks.PINE_LOGS)
                .add(ModBlocks.PINE_LOG.get())
                .add(ModBlocks.PINE_WOOD.get())
                .add(ModBlocks.STRIPPED_PINE_LOG.get())
                .add(ModBlocks.STRIPPED_PINE_WOOD.get());

        this.tag(ModTags.Blocks.PINE_LEAVES).add(ModBlocks.PINE_LEAVES.get());
        this.tag(ModTags.Blocks.LOGS_THAT_CAN_SUSTAIN_PINE_LEAVES).add(ModBlocks.PINE_LOG.get());
    }
}