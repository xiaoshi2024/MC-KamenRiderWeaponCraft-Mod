package com.xiaoshi2022.kamen_rider_weapon_craft.component;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;

import static com.xiaoshi2022.kamen_rider_weapon_craft.Kamen_Rider_Weapon_Craft.MOD_ID;

/**
 * 骑士特效组件类
 * 管理所有骑士特效相关的数据状态
 */
public class ridermodComponents {

    // ========== 数据组件定义 ==========

    // 当前旋转位置
    public static final ComponentType<Integer> CURRENT_ROTATION_POSITION =
            register("current_rotation_position", ComponentType.<Integer>builder().codec(Codec.INT).build());

    // 选择的骑士
    public static final ComponentType<String> SELECTED_RIDER =
            register("selected_rider", ComponentType.<String>builder().codec(Codec.STRING).build());

    // Scramble骑士列表
    public static final ComponentType<List<String>> SCRAMBLE_RIDERS =
            register("scramble_riders", ComponentType.<List<String>>builder()
                    .codec(Codec.list(Codec.STRING))
                    .build());

    // 是否在Finish Time模式
    public static final ComponentType<Boolean> IS_FINISH_TIME_MODE =
            register("is_finish_time_mode", ComponentType.<Boolean>builder().codec(Codec.BOOL).build());

    // 是否在Ultimate模式
    public static final ComponentType<Boolean> IS_ULTIMATE_MODE =
            register("is_ultimate_mode", ComponentType.<Boolean>builder().codec(Codec.BOOL).build());

    // 最后进入Finish Time的时间
    public static final ComponentType<Long> LAST_FINISH_TIME_ENTER =
            register("last_finish_time_enter", ComponentType.<Long>builder().codec(Codec.LONG).build());

    // 最后攻击时间
    public static final ComponentType<Long> LAST_ATTACK_TIME =
            register("last_attack_time", ComponentType.<Long>builder().codec(Codec.LONG).build());

    // 最后骑士选择时间
    public static final ComponentType<Long> LAST_RIDER_SELECTION_TIME =
            register("last_rider_selection_time", ComponentType.<Long>builder().codec(Codec.LONG).build());

    // 激活的骑士特效
    public static final ComponentType<String> ACTIVE_RIDER_EFFECT =
            register("active_rider_effect", ComponentType.<String>builder().codec(Codec.STRING).build());

    // 特效冷却时间
    public static final ComponentType<Integer> EFFECT_COOLDOWN =
            register("effect_cooldown", ComponentType.<Integer>builder().codec(Codec.INT).build());

    // 特效伤害加成
    public static final ComponentType<Float> EFFECT_DAMAGE_BONUS =
            register("effect_damage_bonus", ComponentType.<Float>builder().codec(Codec.FLOAT).build());

    // 特效是否激活
    public static final ComponentType<Boolean> IS_EFFECT_ACTIVE =
            register("is_effect_active", ComponentType.<Boolean>builder().codec(Codec.BOOL).build());

    // 最后使用特效的时间
    public static final ComponentType<Long> LAST_EFFECT_USE_TIME =
            register("last_effect_use_time", ComponentType.<Long>builder().codec(Codec.LONG).build());

    // 注册组件类型
    private static <T> ComponentType<T> register(String name, ComponentType<T> componentType) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, name), componentType);
    }

    // 注册所有自定义组件
    public static void initialize() {
        // 组件会在注册时自动创建，这里不需要额外代码
        System.out.println("注册骑士特效组件");
    }

    /**
     * 检查特效是否在冷却中
     */
    public static boolean isEffectOnCooldown(long lastUseTime, int cooldownTicks, long currentTime) {
        return currentTime - lastUseTime < cooldownTicks;
    }

    /**
     * 获取冷却剩余时间
     */
    public static int getRemainingCooldown(long lastUseTime, int cooldownTicks, long currentTime) {
        long elapsed = currentTime - lastUseTime;
        return Math.max(0, (int)(cooldownTicks - elapsed));
    }
}