package com.xiaoshi2022.kamen_rider_weapon_craft.util;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.tags.FruitTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class FruitConversionRegistry {
    private static final Map<TagKey<Item>, Supplier<Item>> CONVERSIONS = new HashMap<>();

    public static void init() {
        // 当前已实现的转换
        register(FruitTags.CANTALOUPE, () -> ModItems.MELON.get());
        register(FruitTags.CHERRY, () -> ModItems.CHERYY.get());
        register(FruitTags.BANANA, () -> crossModItem("kamen_rider_boss_you_and_me", "bananafruit"));
        register(FruitTags.LEMON, () -> crossModItem("kamen_rider_boss_you_and_me", "lemon_energy"));

        // 预留的未来水果注册位置
        // register(FruitTags.PEACH, () -> crossModItem(...));
        // register(FruitTags.LEMON, () -> crossModItem(...));
    }

    private static void register(TagKey<Item> tag, Supplier<Item> resultSupplier) {
        CONVERSIONS.put(tag, resultSupplier);
    }

    private static Item crossModItem(String modId, String itemId) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(modId, itemId));
    }

    public static Optional<Item> getConversion(ItemStack stack) {
        return CONVERSIONS.entrySet().stream()
                .filter(e -> stack.is(e.getKey()))
                .findFirst()
                .map(e -> e.getValue().get());
    }

    public static boolean isConvertibleFruit(ItemStack stack) {
        return CONVERSIONS.keySet().stream()
                .anyMatch(tag -> stack.is(tag));
    }
}