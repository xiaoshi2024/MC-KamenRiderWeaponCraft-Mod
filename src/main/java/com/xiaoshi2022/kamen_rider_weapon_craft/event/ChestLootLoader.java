package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

@Mod.EventBusSubscriber
public class ChestLootLoader {

    // 用于加载战利品箱的静态方法
    public static void LootLoad(ResourceLocation id, Consumer<LootPool> addPool) {
        String prefix = "minecraft:chests/";
        String name = id.toString();
        if (name.startsWith(prefix)) {
            String file = name.substring(name.indexOf(prefix) + prefix.length());
            switch (file) {
                case "simple_dungeon","abandoned_mineshaft","desert_pyramid","spawn_bonus_chest","stronghold_corridor" -> addPool.accept(getInjectPool());
            }
        }
    }

    // 事件监听器，用于监听战利品表加载事件
    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        LootLoad(event.getName(), lootPool -> event.getTable().addPool(lootPool));
    }

    // 创建自定义战利品池
    public static LootPool getInjectPool() {
        return LootPool.lootPool()
                .add(getInjectEntry(1))
                .setBonusRolls(UniformGenerator.between(0.0F, 1.0F))
                .build();
    }

    // 创建自定义战利品条目
    private static LootPoolEntryContainer.Builder<?> getInjectEntry(int weight) {
        ResourceLocation customChestLocation = new ResourceLocation("kamen_rider_weapon_craft:chests/xs_loot_table");
        return LootTableReference.lootTableReference(customChestLocation)
                .setWeight(weight);
    }
}