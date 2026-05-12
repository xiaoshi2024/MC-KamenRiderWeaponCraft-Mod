package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 给予速度提升，模拟W的高速移动
            shooter.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2));
            
            // 使用方向向量创建锥形攻击区域
            Vec3 normalizedDirection = direction.normalize();
            double range = getEffectRange();
            
            Vec3 start = shooter.getEyePosition(1.0f);
            Vec3 end = start.add(normalizedDirection.scale(range));
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(range / 2, 2.0, range / 2);
            
            level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
                if (entity == shooter) return false;
                Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
                return toEntity.dot(normalizedDirection) > 0.5;
            }).forEach(livingEntity -> {
                if (shooter instanceof Player) {
                    livingEntity.hurt(level.damageSources().playerAttack((Player) shooter), getAttackDamage());
                } else {
                    livingEntity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage());
                }
                livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            });
            
            // 给予护盾效果，模拟W的双重驱动器保护
            shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1));
        }
    }

    @Override
    public String getRiderName() {
        return "W";
    }

    @Override
    public String getActivationSoundName() {
        return "Double Extreme!";
    }

    @Override
    public float getAttackDamage() {
        return 47.0f;
    }

    @Override
    public float getEffectRange() {
        return 9.0f;
    }
}