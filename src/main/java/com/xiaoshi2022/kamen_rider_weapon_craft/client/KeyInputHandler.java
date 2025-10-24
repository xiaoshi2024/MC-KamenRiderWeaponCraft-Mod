package com.xiaoshi2022.kamen_rider_weapon_craft.client;

import com.xiaoshi2022.kamen_rider_weapon_craft.key.KeyBindings;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.NetworkHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

/**
 * 客户端按键事件监听器，用于检测按键按下并发送数据包
 */
@Environment(EnvType.CLIENT)
public class KeyInputHandler {
    private static boolean riderSelectionKeyPressed = false;
    private static boolean ultimateModeKeyPressed = false;
    
    /**
     * 注册按键事件监听器
     */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            handleKeyPresses(client);
        });
    }
    
    /**
     * 处理按键按下事件
     */
    private static void handleKeyPresses(MinecraftClient client) {
        if (client.player == null) return;
        
        // 检查骑士选择键
        boolean currentRiderSelectionState = KeyBindings.getRiderSelectionKey() != null && 
                KeyBindings.getRiderSelectionKey().isPressed();
        
        if (currentRiderSelectionState && !riderSelectionKeyPressed) {
            // 按键刚刚被按下
            NetworkHandler.sendKeyPressPacket(NetworkHandler.RIDER_SELECTION, true);
        }
        riderSelectionKeyPressed = currentRiderSelectionState;
        
        // 检查终极模式键
        boolean currentUltimateModeState = KeyBindings.getUltimateModeKey() != null && 
                KeyBindings.getUltimateModeKey().isPressed();
        
        if (currentUltimateModeState && !ultimateModeKeyPressed) {
            // 按键刚刚被按下
            NetworkHandler.sendKeyPressPacket(NetworkHandler.ULTIMATE_MODE, true);
        }
        ultimateModeKeyPressed = currentUltimateModeState;
    }
}