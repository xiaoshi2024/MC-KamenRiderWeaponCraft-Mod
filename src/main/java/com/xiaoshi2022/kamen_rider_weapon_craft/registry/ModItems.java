package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.items.custom.Heiseisword;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import static com.xiaoshi2022.kamen_rider_weapon_craft.Kamen_Rider_Weapon_Craft.MOD_ID;

public class ModItems {
    public static final Item HEISEISWORD = registerItem("heiseisword",
            setting -> new Heiseisword(setting.component(DataComponentTypes.ATTRIBUTE_MODIFIERS, Heiseisword.createAttributeModifiers())));

    public static void initialize() {
        System.out.println("Heiseisword registered successfully");
    }

    private static Item registerItem(String name, java.util.function.Function<Item.Settings, Item> function) {
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name),
                function.apply(new Item.Settings()
                        .maxCount(1)
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name)))));
    }
}