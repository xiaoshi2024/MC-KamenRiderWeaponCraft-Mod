package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static com.xiaoshi2022.kamen_rider_weapon_craft.Kamen_Rider_Weapon_Craft.MOD_ID;

public class ModItemGroups {

    public static ItemGroup KR_WEAPON_GROUP;

    public static void initialize() {
        // 在initialize方法中创建物品组，确保ModItems已经初始化
        KR_WEAPON_GROUP = Registry.register(
                Registries.ITEM_GROUP,
                Identifier.of(MOD_ID, "kamen_rider_weapons"),
                FabricItemGroup.builder()
                        .displayName(Text.translatable("itemGroup.kamen_rider_weapon_craft.kamen_rider_weapons"))
                        .icon(() -> new ItemStack(ModItems.HEISEISWORD))
                        .entries((displayContext, entries) -> {
                            // 添加所有要显示在物品组中的物品
                            entries.add(ModItems.HEISEISWORD);
                            // 未来可以添加更多物品
                            // entries.add(ModItems.OTHER_WEAPON);
                        })
                        .build()
        );
    }
}