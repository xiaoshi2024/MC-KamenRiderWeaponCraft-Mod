package com.xiaoshi2022.kamen_rider_weapon_craft.util.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class FruitTags {
    // 当前已实现的水果
    public static final TagKey<Item> CANTALOUPE = create("fruits/cantaloupe");
    public static final TagKey<Item> CHERRY = create("fruits/cherry");
    public static final TagKey<Item> BANANA = create("fruits/banana");

    // 预留的未来水果标签（尚未实现）
    public static final TagKey<Item> PEACH = create("fruits/peach");
    public static final TagKey<Item> LEMON = create("fruits/lemon");
    public static final TagKey<Item> DRAGON_FRUIT = create("fruits/dragon_fruit");
    public static final TagKey<Item> MANGO = create("fruits/mango");
    public static final TagKey<Item> GRAPE = create("fruits/grape");

    private static TagKey<Item> create(String path) {
        return ItemTags.create(new ResourceLocation("forge", path));
    }
}