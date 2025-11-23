package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect;

import net.minecraftforge.fml.common.Mod;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

/**
 * 骑士效果事件监听器
 * 用于处理各种骑士能力相关的事件
 */
@Mod.EventBusSubscriber(modid = MOD_ID)
public class RiderEffectEventListener {
    
    /**
     * 我们将不再使用这个事件监听器来处理龙骑的火焰效果
     * 而是直接在RyukiEffect类中处理，这样可以更精确地控制
     * 当玩家使用龙骑技能时才会触发火焰攻击
     */
}