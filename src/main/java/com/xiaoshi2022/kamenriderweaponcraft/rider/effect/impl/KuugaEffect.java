package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.kuuga.KuugaRiderEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class KuugaEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 生成Kuuga特效实体，扑向敌人并爆炸
            KuugaRiderEntity.trySpawnEffect(level, shooter, direction, getAttackDamage());
            
            // 给予全能形态相关的增益效果
            shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
        }
    }

    @Override
    public String getRiderName() {
        return "Kuuga";
    }

    @Override
    public String getActivationSoundName() {
        return "Rising Mighty Kick!";
    }

    @Override
    public float getAttackDamage() {
        return 45.0f;
    }

    @Override
    public float getEffectRange() {
        return 15.0f;
    }
}