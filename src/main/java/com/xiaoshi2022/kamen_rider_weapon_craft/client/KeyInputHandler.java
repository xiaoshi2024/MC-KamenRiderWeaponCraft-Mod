package com.xiaoshi2022.kamen_rider_weapon_craft.client;

import com.xiaoshi2022.kamen_rider_weapon_craft.key.KeyBindings;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.NetworkHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import static com.xiaoshi2022.kamen_rider_weapon_craft.Kamen_Rider_Weapon_Craft.LOGGER;

/**
 * 客户端按键事件监听器，用于检测按键按下并发送数据包
 */
@Environment(EnvType.CLIENT)
public class KeyInputHandler {
    private static boolean riderSelectionKeyPressed = false;
    private static boolean ultimateModeKeyPressed = false;
    private static boolean bothKeysWerePressed = false;

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
     * 确保所有玩家的按键事件都能正确发送到服务器
     */
    private static void handleKeyPresses(MinecraftClient client) {
        if (client.player == null) return;

        // 检查骑士选择键（Y键）
        boolean currentRiderSelectionState = KeyBindings.getRiderSelectionKey() != null &&
                KeyBindings.getRiderSelectionKey().isPressed();

        // 检查超必杀键（X键）
        boolean currentUltimateModeState = KeyBindings.getUltimateModeKey() != null &&
                KeyBindings.getUltimateModeKey().isPressed();

        // 检测X键和Y键的组合按下 - 使用更宽松的检测
        boolean bothKeysPressed = currentRiderSelectionState && currentUltimateModeState;

        // 优先处理X+Y组合键
        if (bothKeysPressed && !bothKeysWerePressed) {
            LOGGER.info("Client: X+Y combination detected, sending packet");
            NetworkHandler.sendKeyPressPacket(NetworkHandler.ULTIMATE_MODE, true);
            bothKeysWerePressed = true;
        }
        // 当任一键释放时重置组合键状态
        else if ((!currentRiderSelectionState || !currentUltimateModeState) && bothKeysWerePressed) {
            bothKeysWerePressed = false;
        }

        // 只有当不是组合键按下时，才处理单独的Y键
        if (currentRiderSelectionState && !riderSelectionKeyPressed && !bothKeysPressed) {
            NetworkHandler.sendKeyPressPacket(NetworkHandler.RIDER_SELECTION, true);
        }

        riderSelectionKeyPressed = currentRiderSelectionState;
    }
}