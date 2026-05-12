package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.faiz.FaizEmptySetEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FaizEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 给予加速效果，模拟Faiz的高速移动能力
            shooter.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2));
            
            // 发射光束攻击
            Vec3 normalizedDirection = direction.normalize();
            double range = getEffectRange();
            
            Vec3 start = shooter.getEyePosition(1.0f);
            Vec3 end = start.add(normalizedDirection.scale(range));
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(1.0);
            
            level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
                if (entity == shooter) return false;
                Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
                return toEntity.dot(normalizedDirection) > 0.7;
            }).forEach(livingEntity -> {
                float healthBefore = livingEntity.getHealth();
                
                if (shooter instanceof Player) {
                    livingEntity.hurt(level.damageSources().playerAttack((Player) shooter), getAttackDamage());
                } else {
                    livingEntity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage());
                }
                livingEntity.setRemainingFireTicks(100);
                
                // 如果敌人被击杀，生成空集符号实体
                if (!livingEntity.isAlive() && healthBefore > 0) {
                    spawnEmptySetParticleEffect(level, livingEntity.position());
                }
            });
            
            // 给予夜视效果，模拟Faiz的眼部扫描功能
            shooter.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0));
        }
    }
    
    /**
     * 在指定位置生成空集符号效果实体
     */
    private void spawnEmptySetParticleEffect(Level level, Vec3 position) {
        if (!level.isClientSide) {
            FaizEmptySetEntity emptySetEntity = new FaizEmptySetEntity(level, null);
            emptySetEntity.setPos(position.x, position.y + 0.5, position.z);
            level.addFreshEntity(emptySetEntity);
        }
    }

    @Override
    public String getRiderName() {
        return "Faiz";
    }

    @Override
    public String getActivationSoundName() {
        return "Axel Form!";
    }

    @Override
    public float getAttackDamage() {
        return 44.0f;
    }

    @Override
    public float getEffectRange() {
        return 12.0f;
    }
}