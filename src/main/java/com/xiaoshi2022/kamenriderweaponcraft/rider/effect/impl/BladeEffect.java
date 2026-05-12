package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BladeEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 为使用者提供力量提升，模拟Blade的剑刃攻击
            shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
            
            // 使用方向向量创建锥形攻击区域
            Vec3 normalizedDirection = direction.normalize();
            double range = getEffectRange();
            
            Vec3 start = shooter.getEyePosition(1.0f);
            Vec3 end = start.add(normalizedDirection.scale(range));
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(range / 3, 1.5, range / 3);
            
            level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
                if (entity == shooter) return false;
                Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
                return toEntity.dot(normalizedDirection) > 0.6;
            }).forEach(livingEntity -> {
                if (shooter instanceof Player) {
                    livingEntity.hurt(level.damageSources().playerAttack((Player) shooter), getAttackDamage());
                } else {
                    livingEntity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage());
                }
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            });
            
            // 添加护盾效果，模拟卡片防御
            shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1));
        }
    }

    @Override
    public String getRiderName() {
        return "Blade";
    }

    @Override
    public String getActivationSoundName() {
        return "Blade Blade!";
    }

    @Override
    public float getAttackDamage() {
        return 48.0f;
    }

    @Override
    public float getEffectRange() {
        return 7.0f;
    }
}