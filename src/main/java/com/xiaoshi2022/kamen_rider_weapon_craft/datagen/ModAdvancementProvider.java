package com.xiaoshi2022.kamen_rider_weapon_craft.datagen;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends AdvancementProvider {
    
    public ModAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, List.of(new ModAdvancementsSubProvider(provider)));
    }
    
    // 内部类，实现AdvancementSubProvider接口
    private static class ModAdvancementsSubProvider implements AdvancementSubProvider {
        private final CompletableFuture<HolderLookup.Provider> provider;
        
        public ModAdvancementsSubProvider(CompletableFuture<HolderLookup.Provider> provider) {
            this.provider = provider;
        }
        
        @Override
        public void generate(HolderLookup.Provider provider, Consumer<Advancement> consumer) {
            // 创建"腐败的时空"成就，使用现有的kamen_rider_weapon_craft作为父成就
            Advancement.Builder.advancement()
                    .parent(new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "kamen_rider_weapon_craft"))
                    .display(
                            new ItemStack(com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems.HEISEISWORD.get()),
                            Component.literal("腐败的时空"),
                            Component.literal("第一次被平成剑僵尸攻击，遇到了DCD形态的武器僵尸"),
                            null,
                            FrameType.CHALLENGE,
                            true,
                            true,
                            false
                    )
                    .addCriterion("trigger", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STICK))
                    .save(consumer, kamen_rider_weapon_craft.MOD_ID + ":corrupted_time_space");
        }
    }
}