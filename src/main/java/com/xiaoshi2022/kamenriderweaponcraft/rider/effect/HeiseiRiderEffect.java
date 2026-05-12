package com.xiaoshi2022.kamenriderweaponcraft.rider.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface HeiseiRiderEffect {
    // 注意：第二个参数是 LivingEntity，不是 Player
    void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction);

    String getRiderName();
    String getActivationSoundName();
    float getAttackDamage();
    float getEffectRange();
    double getEnergyCost();
}