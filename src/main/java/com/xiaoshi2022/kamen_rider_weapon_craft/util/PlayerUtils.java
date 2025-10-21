package com.xiaoshi2022.kamen_rider_weapon_craft.util;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.EffectInit;
import net.minecraft.world.entity.player.Player;

public class PlayerUtils {
    /**
     * 检查玩家是否拥有Helmheim Power效果
     * @param player 要检查的玩家
     * @return 当player为null或没有效果时返回false
     */
    public static boolean hasCustomBuff(Player player, String effectName) {
        // 安全保护：检查null和正确的效果名称
        if (player == null || !"helmheim_power".equals(effectName)) {
            return false;
        }

        // 直接返回效果检查结果
        return player.hasEffect(EffectInit.HELMHEIM_POWER.get());
    }
}