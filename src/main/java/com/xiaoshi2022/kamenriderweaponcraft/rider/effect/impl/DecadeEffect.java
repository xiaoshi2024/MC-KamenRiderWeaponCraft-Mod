package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.decade.DecadeRiderEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DecadeEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (level.isClientSide) return;
        // 调用 Decade 特效
        DecadeRiderEffect.spawnDimensionKickEffect(level, shooter, direction, getAttackDamage());
    }

    @Override
    public String getRiderName() {
        return "Decade";
    }

    @Override
    public String getActivationSoundName() {
        return "name_decade";
    }

    @Override
    public float getAttackDamage() {
        return 13.0F;
    }

    @Override
    public float getEffectRange() {
        return 9.0F;
    }
}