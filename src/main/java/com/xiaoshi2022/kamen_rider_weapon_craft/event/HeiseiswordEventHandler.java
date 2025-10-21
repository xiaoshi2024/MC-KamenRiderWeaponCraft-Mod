package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy.HeiseiswordEnergyManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 平成嘿嘿剑事件处理器
 * 用于处理能量恢复和其他相关事件
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HeiseiswordEventHandler {
    // 用于跟踪tick计数，每20个tick（1秒）恢复一次能量
    private static int tickCounter = 0;
    
    /**
     * 监听玩家tick事件，处理能量恢复
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // 只在服务器端处理
        if (event.player.level().isClientSide) {
            return;
        }
        
        // 每20个tick（1秒）恢复一次能量
        if (tickCounter++ >= 20) {
            tickCounter = 0;
            // 更新玩家能量恢复
            HeiseiswordEnergyManager.updateEnergyRegen(event.player);
        }
    }
    
    /**
     * 监听服务器tick事件，用于全局能量系统管理
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        // 这里可以添加全局能量系统管理逻辑
        // 例如重置tick计数器等
    }
}