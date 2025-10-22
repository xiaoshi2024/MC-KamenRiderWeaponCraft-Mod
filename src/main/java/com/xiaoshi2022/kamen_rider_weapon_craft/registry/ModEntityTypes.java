// registry/ModEntityTypes.java
package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.xiaoshi2022.kamen_rider_weapon_craft.Kamen_Rider_Weapon_Craft.MOD_ID;

public class ModEntityTypes {

    // 定义实体类型


    // 注册实体类型
    private static <T extends Entity> EntityType<T> register(String name, EntityType<T> entityType) {
        return Registry.register(Registries.ENTITY_TYPE,
                Identifier.of(MOD_ID, name),
                entityType);
    }

    public static void initialize() {
        // 实体类型会在类加载时自动注册
    }
}