package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xiaoshi2022.kamen_rider_weapon_craft.villagers.LockSeedMerchantProfession;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LockSeedMerchantTradesEvent {

    @SubscribeEvent
    public static void registerTrades(VillagerTradesEvent event) {
        if (event.getType() == LockSeedMerchantProfession.LOCKSEED_MERCHANT.get()) {
            // 读取 nbt.json 文件
            JsonObject nbtJson = readNbtJson();

            // 获取所有注册的物品
            List<Item> allItems = ForgeRegistries.ITEMS.getEntries().stream()
                    .map(entry -> entry.getValue())
                    .toList();

            // 遍历所有物品，检查是否在 nbt.json 中定义
            for (Item item : allItems) {
                ItemStack stack = new ItemStack(item);
                if (isLockSeedItem(stack, nbtJson)) {
                    // 如果带有锁种标签，添加到交易列表
                    addLockSeedTrade(event, stack);
                    ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(item);
                    System.out.println("Added trade for item: " + registryName + " with rarity: " + stack.getRarity().name());
                }
            }
        }
    }

    private static JsonObject readNbtJson() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                LockSeedMerchantTradesEvent.class.getResourceAsStream("/nbt.json"), "UTF-8"))) {
            return new Gson().fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonObject();
        }
    }

    private static boolean isLockSeedItem(ItemStack stack, JsonObject nbtJson) {
        // 检查物品是否在 nbt.json 中定义
        JsonArray lockseeds = nbtJson.has("lockseeds") ? nbtJson.get("lockseeds").getAsJsonArray() : new JsonArray();
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(stack.getItem());
        for (int i = 0; i < lockseeds.size(); i++) {
            String itemName = lockseeds.get(i).getAsString();
            if (registryName.toString().equals(itemName)) {
                // 设置自定义名字
                CompoundTag tag = stack.getOrCreateTag();
                tag.putString("CustomName", "lockseeds");
                return true;
            }
        }
        return false;
    }

    private static void addLockSeedTrade(VillagerTradesEvent event, ItemStack itemStack) {
        // 添加交易：玩家用绿宝石换取锁种物品
        event.getTrades().get(1).add(new BasicItemListing(
                new ItemStack(Items.EMERALD, 1), // 1 绿宝石
                itemStack,                       // 锁种物品
                8, 4, 0.05f));                   // 最大供应次数、经验、价格乘数

        // 添加交易：玩家用锁种物品换取绿宝石
        event.getTrades().get(2).add(new BasicItemListing(
                itemStack,                       // 锁种物品
                new ItemStack(Items.EMERALD, 1), // 1 绿宝石
                8, 4, 0.05f));                   // 最大供应次数、经验、价格乘数
    }
}