// rider/effect/HeiseiRiderEffect.java
package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// HeiseiRiderEffect.java
public interface HeiseiRiderEffect {
    // 执行特殊攻击效果
    void executeSpecialAttack(Level level, Player player, Vec3 direction);

    // 返回骑士名称
    String getRiderName();

    // 获取激活音效名称
    String getActivationSoundName();

    // 攻击伤害值
    float getAttackDamage();

    // 特效范围
    float getEffectRange();
    
    // 获取骑士能量消耗
    double getEnergyCost();
}