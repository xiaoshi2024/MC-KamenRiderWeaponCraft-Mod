package com.xiaoshi2022.kamen_rider_weapon_craft.util;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.EffectInit;
import net.minecraft.world.entity.player.Player;

public class PlayerUtils {
    public static boolean hasCustomBuff(Player player, String effectName) {
        // 检查玩家是否拥有特定的效果
        if ("helmheim_power".equals(effectName)) {
            return player.hasEffect(EffectInit.HELMHEIM_POWER.get());
        }
        return false;
    }
}