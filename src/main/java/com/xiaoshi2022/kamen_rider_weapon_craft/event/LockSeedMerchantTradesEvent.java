package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.villagers.LockSeedMerchantProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LockSeedMerchantTradesEvent {

    @SubscribeEvent
    public static void registerTrades(VillagerTradesEvent event) {
        if (event.getType() == LockSeedMerchantProfession.LOCKSEED_MERCHANT.get()) {
            // 获取所有注册的物品
            List<Item> allItems = ForgeRegistries.ITEMS.getEntries().stream()
                    .map(entry -> entry.getValue())
                    .toList();

            // 遍历所有物品，检查是否带有锁种标签
            for (Item item : allItems) {
                ItemStack stack = new ItemStack(item);
                if (isLockSeedItem(stack)) {
                    // 如果带有锁种标签，添加到交易列表
                    addLockSeedTrade(event, stack);
                }
            }
        }
    }

    private static boolean isLockSeedItem(ItemStack stack) {
        // 检查物品是否带有锁种标签
        return stack.getOrCreateTag().contains("is_lockseed") && stack.getOrCreateTag().getInt("is_lockseed") == 1;
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