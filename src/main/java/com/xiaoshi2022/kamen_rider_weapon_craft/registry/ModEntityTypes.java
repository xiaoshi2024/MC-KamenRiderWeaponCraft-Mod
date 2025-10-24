package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.build.BuildRiderEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import static com.xiaoshi2022.kamen_rider_weapon_craft.Kamen_Rider_Weapon_Craft.MOD_ID;

public class ModEntityTypes {

    // 定义实体类型
    // Build骑士特效实体类型
    public static final EntityType<Entity> BUILD_RIDER_EFFECT = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(MOD_ID, "build_rider_effect"),
            EntityType.Builder.create(BuildRiderEntity::new, SpawnGroup.MISC)
                    .dimensions(1.5f, 3.0f)
                    .maxTrackingRange(64)
                    .trackingTickInterval(1)
                    .build(RegistryKey.of(Registries.ENTITY_TYPE.getKey(), Identifier.of(MOD_ID, "build_rider_effect")))
    );

    public static void initialize() {
        System.out.println("注册实体类型: " + BUILD_RIDER_EFFECT);
    }
}