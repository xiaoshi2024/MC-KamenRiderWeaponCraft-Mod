// rider/effect/HeiseiRiderEffect.java
package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface HeiseiRiderEffect {
    // 执行特殊攻击效果
    void executeSpecialAttack(World world, PlayerEntity player, Vec3d direction);

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