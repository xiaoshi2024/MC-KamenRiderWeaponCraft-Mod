package com.xiaoshi2022.kamen_rider_weapon_craft.datagen;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModItemTagGenerator extends ItemTagsProvider {

    // 手动创建指向 minecraft:compostables 标签的 TagKey
    private static final TagKey<Item> COMPOSTABLES = TagKey.create(net.minecraft.core.registries.Registries.ITEM, new ResourceLocation("minecraft", "compostables"));

    public ModItemTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, BlockTagsProvider blockTags, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags.contentsGetter(), kamen_rider_weapon_craft.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 使用手动创建的 COMPOSTABLES TagKey
        this.tag(COMPOSTABLES).add(ModItems.PINE_LEAVES_ITEM.get());

        // 添加其他物品标签...
    }
}