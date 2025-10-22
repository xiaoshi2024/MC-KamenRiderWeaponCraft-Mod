package com.xiaoshi2022.kamen_rider_weapon_craft.component;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.ArrayList;

public class ModComponents {
    // 声明组件字段，但不立即注册
    public static ComponentType<String> SELECTED_RIDER;
    public static ComponentType<List<String>> SCRAMBLE_RIDERS;
    public static ComponentType<Boolean> IS_FINISH_TIME_MODE;
    public static ComponentType<Boolean> IS_ULTIMATE_MODE;
    public static ComponentType<Integer> CURRENT_ROTATION_POSITION;
    public static ComponentType<Long> LAST_FINISH_TIME_ENTER;
    public static ComponentType<Long> LAST_ATTACK_TIME;
    public static ComponentType<Long> LAST_RIDER_SELECTION_TIME;
    public static ComponentType<NbtCompound> HEISEISWORD_ENERGY;

    public static void initialize() {
        // 移至initialize方法中注册所有组件
        SELECTED_RIDER = Registry.register(Registries.DATA_COMPONENT_TYPE,
                Identifier.of("kamen_rider_weapon_craft", "selected_rider"),
                ComponentType.<String>builder().codec(Codec.STRING).build()
        );

        SCRAMBLE_RIDERS = Registry.register(Registries.DATA_COMPONENT_TYPE,
                Identifier.of("kamen_rider_weapon_craft", "scramble_riders"),
                ComponentType.<List<String>>builder().codec(Codec.STRING.listOf()).build()
        );

        IS_FINISH_TIME_MODE = Registry.register(Registries.DATA_COMPONENT_TYPE,
                Identifier.of("kamen_rider_weapon_craft", "is_finish_time_mode"),
                ComponentType.<Boolean>builder().codec(Codec.BOOL).build()
        );

        IS_ULTIMATE_MODE = Registry.register(Registries.DATA_COMPONENT_TYPE,
                Identifier.of("kamen_rider_weapon_craft", "is_ultimate_mode"),
                ComponentType.<Boolean>builder().codec(Codec.BOOL).build()
        );

        CURRENT_ROTATION_POSITION = Registry.register(Registries.DATA_COMPONENT_TYPE,
                Identifier.of("kamen_rider_weapon_craft", "current_rotation_position"),
                ComponentType.<Integer>builder().codec(Codec.INT).build()
        );

        LAST_FINISH_TIME_ENTER = Registry.register(Registries.DATA_COMPONENT_TYPE,
                Identifier.of("kamen_rider_weapon_craft", "last_finish_time_enter"),
                ComponentType.<Long>builder().codec(Codec.LONG).build()
        );

        LAST_ATTACK_TIME = Registry.register(Registries.DATA_COMPONENT_TYPE,
                Identifier.of("kamen_rider_weapon_craft", "last_attack_time"),
                ComponentType.<Long>builder().codec(Codec.LONG).build()
        );

        LAST_RIDER_SELECTION_TIME = Registry.register(Registries.DATA_COMPONENT_TYPE,
                Identifier.of("kamen_rider_weapon_craft", "last_rider_selection_time"),
                ComponentType.<Long>builder().codec(Codec.LONG).build()
        );

        HEISEISWORD_ENERGY = Registry.register(Registries.DATA_COMPONENT_TYPE,
                Identifier.of("kamen_rider_weapon_craft", "heiseisword_energy"),
                ComponentType.<NbtCompound>builder().codec(NbtCompound.CODEC).build()
        );
    }
}