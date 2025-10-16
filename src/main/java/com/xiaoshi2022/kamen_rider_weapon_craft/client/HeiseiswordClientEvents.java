package com.xiaoshi2022.kamen_rider_weapon_craft.client;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy.HeiseiswordEnergyRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 平成嘿嘿剑客户端事件处理器
 * 用于注册客户端相关的事件，如GUI渲染等
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HeiseiswordClientEvents {
    
    /**
     * 注册GUI覆盖层
     */
    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        // 注册能量条渲染器到游戏GUI中
        event.registerAboveAll("heiseisword_energy", HeiseiswordEnergyRenderer.RENDER_ENERGY_BAR);
    }
}