package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RyukiEffect extends AbstractHeiseiRiderEffect {

    private enum RyukiCard {
        SwordVent, 
        ShootVent, 
        GuardVent, 
        StrikeVent, 
        FinalVent
    }

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 为使用者提供火焰抗性，避免在战斗中被火焰伤害
            shooter.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
            
            // 随机选择一个卡片能力
            RyukiCard selectedCard = RyukiCard.values()[level.random.nextInt(RyukiCard.values().length)];
            
            switch (selectedCard) {
                case SwordVent:
                    executeSwordVent(level, shooter, direction);
                    break;
                case ShootVent:
                    executeShootVent(level, shooter, direction);
                    break;
                case GuardVent:
                    executeGuardVent(level, shooter, direction);
                    break;
                case StrikeVent:
                    executeStrikeVent(level, shooter, direction);
                    break;
                case FinalVent:
                    executeFinalVent(level, shooter, direction);
                    break;
            }
            
            // 给予镜世界相关的增益效果
            shooter.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
        }
    }
    
    private void executeSwordVent(Level level, LivingEntity shooter, Vec3 direction) {
        // 剑降临：使用龙剑攻击
        double reach = getEffectRange();
        Vec3 normalizedDirection = direction.normalize();
        
        // 创建基于前方的攻击区域
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(normalizedDirection.scale(reach));
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(1.0);
        
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
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            livingEntity.setRemainingFireTicks(100);
        });
    }
    
    private void executeShootVent(Level level, LivingEntity shooter, Vec3 direction) {
        // 射击降临：发射龙弹
        Vec3 normalizedDirection = direction.normalize();
        double maxReach = 15.0;
        
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(normalizedDirection.scale(maxReach));
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(2.0);
        
        level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
            if (entity == shooter) return false;
            Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
            return toEntity.dot(normalizedDirection) > 0.5;
        }).forEach(livingEntity -> {
            float damage = getAttackDamage() * 0.3f;
            if (shooter instanceof Player) {
                livingEntity.hurt(level.damageSources().playerAttack((Player) shooter), damage);
            } else {
                livingEntity.hurt(level.damageSources().mobAttack(shooter), damage);
            }
            livingEntity.setRemainingFireTicks(140);
        });
    }
    
    private void executeGuardVent(Level level, LivingEntity shooter, Vec3 direction) {
        // 防御降临：使用龙盾防御
        shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2));
        
        Vec3 normalizedDirection = direction.normalize();
        double range = getEffectRange();
        
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(normalizedDirection.scale(range));
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(range / 2, 2.0, range / 2);
        
        level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
            if (entity == shooter) return false;
            Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
            return toEntity.dot(normalizedDirection) > 0.5;
        }).forEach(entity -> {
            if (shooter instanceof Player) {
                entity.hurt(level.damageSources().thorns((Player) shooter), getAttackDamage() * 0.3f);
            }
        });
    }
    
    private void executeStrikeVent(Level level, LivingEntity shooter, Vec3 direction) {
        // 突击降临：使用龙爪攻击
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
            float damage = getAttackDamage() * 0.6f;
            if (shooter instanceof Player) {
                livingEntity.hurt(level.damageSources().playerAttack((Player) shooter), damage);
            } else {
                livingEntity.hurt(level.damageSources().mobAttack(shooter), damage);
            }
            shooter.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));
            livingEntity.setRemainingFireTicks(100);
        });
    }
    
    private void executeFinalVent(Level level, LivingEntity shooter, Vec3 direction) {
        // 最终降临：召唤契约兽无双龙，发动Dragon Rider Kick
        Vec3 normalizedDirection = direction.normalize();
        double range = getEffectRange();
        Vec3 targetPos = shooter.getEyePosition(1.0f).add(normalizedDirection.scale(range));
        
        // 制造爆炸效果
        level.explode(shooter, targetPos.x, targetPos.y, targetPos.z,
                (float) (range / 2), Level.ExplosionInteraction.NONE);
        
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(targetPos, targetPos).inflate(4.0);
        
        level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
            if (entity == shooter) return false;
            Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
            return toEntity.dot(normalizedDirection) > 0.5;
        }).forEach(livingEntity -> {
            float damage = getAttackDamage() * 1.5f;
            if (shooter instanceof Player) {
                livingEntity.hurt(level.damageSources().playerAttack((Player) shooter), damage);
            } else {
                livingEntity.hurt(level.damageSources().mobAttack(shooter), damage);
            }
            Vec3 knockback = normalizedDirection.scale(2.0);
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(knockback));
            livingEntity.setRemainingFireTicks(200);
        });
        
        shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 5));
    }

    @Override
    public String getRiderName() {
        return "Ryuki";
    }

    @Override
    public String getActivationSoundName() {
        return "Dragon Rider Kick!";
    }

    @Override
    public float getAttackDamage() {
        return 47.0f;
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
    
    /**
     * 发射火球技能
     */
    public void executeFireballAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            Vec3 fireballDirection = direction != null && direction.lengthSqr() > 0 ? 
                                     direction.normalize() : shooter.getLookAngle();
            
            Vec3 velocity = fireballDirection.scale(1.5);
            SmallFireball fireball = new SmallFireball(level, 
                shooter.getX() + fireballDirection.x * 0.5,
                shooter.getEyeY() - 0.3 + fireballDirection.y * 0.5,
                shooter.getZ() + fireballDirection.z * 0.5,
                velocity
            );
            
            level.addFreshEntity(fireball);
        }
    }
}