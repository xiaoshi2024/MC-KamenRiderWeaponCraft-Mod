package com.xiaoshi2022.kamen_rider_weapon_craft.key;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Y;

/**
 * 骑士武器模组的按键绑定定义
 */
public class KeyBindings {
    // 按键绑定实例 - 仅在客户端初始化
    private static KeyBinding riderSelectionKey;
    private static KeyBinding ultimateModeKey;
    
    /**
     * 获取骑士选择按键
     * @return 按键绑定实例
     */
    @Environment(EnvType.CLIENT)
    public static KeyBinding getRiderSelectionKey() {
        return riderSelectionKey;
    }
    
    /**
     * 获取超必杀模式按键
     * @return 按键绑定实例
     */
    @Environment(EnvType.CLIENT)
    public static KeyBinding getUltimateModeKey() {
        return ultimateModeKey;
    }
    
    /**
     * 注册所有按键绑定
     * 在Fabric的ClientModInitializer中调用
     * 仅在客户端环境中执行
     */
    @Environment(EnvType.CLIENT)
    public static void registerKeyBindings() {
        // 注册骑士选择键（Y键）
        riderSelectionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kamen_rider_weapon_craft.rider_selection",
                InputUtil.Type.KEYSYM,
                GLFW_KEY_Y,
                "category.kamen_rider_weapon_craft.main"
        ));
        
        // 注册超必杀模式键（X键）
        ultimateModeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kamen_rider_weapon_craft.ultimate_mode",
                InputUtil.Type.KEYSYM,
                GLFW_KEY_X,
                "category.kamen_rider_weapon_craft.main"
        ));
    }
}