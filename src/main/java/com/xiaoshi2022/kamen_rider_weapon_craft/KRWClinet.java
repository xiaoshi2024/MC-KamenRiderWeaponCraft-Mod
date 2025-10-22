package com.xiaoshi2022.kamen_rider_weapon_craft;

import com.xiaoshi2022.kamen_rider_weapon_craft.items.client.Heiseisword.HeiseiswordItemEntityRenderer;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy.HeiseiswordEnergyRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.entity.EntityType;

public class KRWClinet implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 注册自定义掉落物渲染器
        EntityRendererRegistry.register(EntityType.ITEM, HeiseiswordItemEntityRenderer::new);

        HeiseiswordEnergyRenderer.register();
        System.out.println("Kamen Rider Weapon Craft client initialized");
    }
}