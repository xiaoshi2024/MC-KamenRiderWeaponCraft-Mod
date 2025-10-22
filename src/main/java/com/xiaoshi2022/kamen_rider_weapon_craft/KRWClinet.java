package com.xiaoshi2022.kamen_rider_weapon_craft;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy.HeiseiswordEnergyRenderer;
import net.fabricmc.api.ClientModInitializer;

public class KRWClinet implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        HeiseiswordEnergyRenderer.register();
        System.out.println("Kamen Rider Weapon Craft client initialized");
    }
}