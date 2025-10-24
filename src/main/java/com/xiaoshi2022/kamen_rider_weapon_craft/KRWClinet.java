package com.xiaoshi2022.kamen_rider_weapon_craft;

import com.xiaoshi2022.kamen_rider_weapon_craft.client.KeyInputHandler;
import com.xiaoshi2022.kamen_rider_weapon_craft.key.KeyBindings;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.RiderEffectEntityRegistry;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy.HeiseiswordEnergyRenderer;
import net.fabricmc.api.ClientModInitializer;

public class KRWClinet implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 注册按键绑定
        KeyBindings.registerKeyBindings();
        
        // 注册按键输入监听器
        KeyInputHandler.register();
        
        // 注册骑士特效实体渲染器（重要！）
        RiderEffectEntityRegistry.registerRiderEffectEntityRenderers();
        
        HeiseiswordEnergyRenderer.register();
        System.out.println("Kamen Rider Weapon Craft client initialized");
    }
}