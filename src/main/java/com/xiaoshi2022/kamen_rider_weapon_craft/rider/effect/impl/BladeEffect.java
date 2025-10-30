package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BladeEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Lightning Slash攻击，使用Blade的光刃
            // 获取标准化的方向向量
            Vec3 normalizedDirection = direction.normalize();
            double reach = getEffectRange();
            Vec3 start = player.getEyePosition(1.0f);
            Vec3 end = start.add(normalizedDirection.scale(reach));
            
            // 创建基于玩家前方的攻击区域
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(1.0);
            
            // 优化：使用更高效的实体查找方式，并添加方向检查
            level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
                if (entity == player) return false;
                
                // 计算实体相对于玩家的方向向量
                Vec3 toEntity = entity.position().subtract(start).normalize();
                // 确保目标在玩家面前的角度范围内（约60度，余弦值>0.5）
                return toEntity.dot(normalizedDirection) > 0.5;
            }).forEach(entity -> {
                // 造成伤害
                entity.hurt(
                    level.damageSources().playerAttack(player), getAttackDamage());
                
                // 为被击中的实体添加酥麻/电击效果
                addElectricalShockEffect(entity);
                
                // 播放电击音效
                level.playSound(null, entity.blockPosition(), 
                        net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER, 
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);
            });
            
            // 2. 给予玩家速度提升效果
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    /**
     * 为实体添加酥麻/电击效果
     * 模拟被电击后的状态，包括减速、发光和其他相关效果
     */
    private void addElectricalShockEffect(LivingEntity entity) {
        // 添加减速效果（模拟肌肉麻痹）
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        
        // 添加发光效果（表示带电状态）
        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        
        // 添加挖掘/攻击速度减慢（模拟反应迟缓）
        entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 1));
        
        // 可以选择添加短暂的失明效果，增加电击的真实感
        if (!(entity instanceof Player)) { // 不对玩家添加失明，保持游戏体验
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
        }
    }

    @Override
    public String getRiderName() {
        return "Blade";
    }

    @Override
    public String getActivationSoundName() {
        return "Lightning Slash!";
    }

    @Override
    public float getAttackDamage() {
        return 49.0f; // 普通骑士 - Blade拥有醒剑力量，伤害略高于基础值
    }

    @Override
    public float getEffectRange() {
        return 10.0f;
    }
    
    /**
     * 为非玩家实体（如僵尸）执行Blade的特殊攻击效果
     * 确保僵尸等生物使用该武器时也能触发相同的Lightning Slash和电击效果
     */
    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 播放骑士选择音效
            com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager.playSelectionSound(level, shooter, getRiderName());
            
            // 播放攻击音效
            com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager.playAttackSound(level, shooter, getRiderName());
            
            // 获取标准化的攻击方向
            Vec3 normalizedDirection = (direction != null && direction.lengthSqr() > 0) ? 
                                      direction.normalize() : shooter.getLookAngle().normalize();
            
            // 服务器端：发动Lightning Slash攻击，使用Blade的光刃
            double reach = getEffectRange();
            Vec3 start = shooter.getEyePosition(1.0f);
            Vec3 end = start.add(normalizedDirection.scale(reach));
            
            // 创建基于射手前方的攻击区域
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(1.0);
            
            // 优化：使用更高效的实体查找方式，并添加方向检查
            level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
                if (entity == shooter) return false;
                
                // 计算实体相对于射手的方向向量
                Vec3 toEntity = entity.position().subtract(start).normalize();
                // 确保目标在射手面前的角度范围内（约60度，余弦值>0.5）
                return toEntity.dot(normalizedDirection) > 0.5;
            }).forEach(entity -> {
                // 造成伤害
                entity.hurt(
                    level.damageSources().mobAttack(shooter), getAttackDamage());
                
                // 为被击中的实体添加酥麻/电击效果
                addElectricalShockEffect(entity);
                
                // 播放电击音效
                level.playSound(null, entity.blockPosition(), 
                        net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER, 
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);
            });
            
            // 给予使用武器的非玩家实体（如僵尸）速度提升效果
            shooter.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
        }
    }
}
