package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID)
public class CommonEvents {

    public static void handleFruitConversion(Player player, InteractionHand hand) {
        // 1. 快速空检查
        if (player == null) return;

        // 2. 获取物品并检查
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty()) return;

        // 3. 标签匹配转换
        Item targetItem = getConvertedItem(heldItem);
        if (targetItem != null) {
            convertItem(player, hand, heldItem, targetItem);
        }
    }

    private static Item getConvertedItem(ItemStack stack) {
        if (stack.is(ItemTags.create(new ResourceLocation("forge", "fruits/cantaloupe")))) {
            return ModItems.MELON.get();
        }
        else if (stack.is(ItemTags.create(new ResourceLocation("forge", "fruits/cherry")))) {
            return ModItems.CHERYY.get();
        }
        else if (stack.is(ItemTags.create(new ResourceLocation("forge", "fruits/banana")))) {
            return ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation("kamen_rider_boss_you_and_me", "bananafruit"));
        }
        //可继续添加如下例子
//        else if (stack.is(ItemTags.create(new ResourceLocation("forge", "fruits/peach")))) {
//            return ForgeRegistries.ITEMS.getValue(
//                    new ResourceLocation("other_mod", "peach_lockseed"));
//        }
        return null;
    }

    private static void convertItem(Player player, InteractionHand hand,
                                    ItemStack original, Item newItem) {
        original.shrink(1);
        ItemStack result = new ItemStack(newItem);

        if (original.isEmpty()) {
            player.setItemInHand(hand, result);
        } else {
            player.getInventory().add(result);
        }
    }
}