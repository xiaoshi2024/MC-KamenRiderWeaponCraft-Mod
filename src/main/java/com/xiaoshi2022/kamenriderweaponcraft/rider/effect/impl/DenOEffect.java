package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DenOEffect extends AbstractHeiseiRiderEffect {

    private enum DenOForm {
        SwordForm,
        RodForm,
        AxForm,
        GunForm,
        ClimaxForm
    }

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 随机选择一个形态进行攻击
            DenOForm selectedForm = DenOForm.values()[level.random.nextInt(DenOForm.values().length)];
            
            switch (selectedForm) {
                case SwordForm:
                    executeSwordForm(level, shooter, direction);
                    break;
                case RodForm:
                    executeRodForm(level, shooter, direction);
                    break;
                case AxForm:
                    executeAxForm(level, shooter, direction);
                    break;
                case GunForm:
                    executeGunForm(level, shooter, direction);
                    break;
                case ClimaxForm:
                    executeClimaxForm(level, shooter, direction);
                    break;
            }
            
            // 给予时间相关的增益效果
            shooter.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
        }
    }
    
    private void executeSwordForm(Level level, LivingEntity shooter, Vec3 direction) {
        // 圣剑形态：斩击攻击
        Vec3 normalizedDirection = direction.normalize();
        double range = getEffectRange();
        
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(normalizedDirection.scale(range));
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(1.0);
        
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
            shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 1));
        });
    }
    
    private void executeRodForm(Level level, LivingEntity shooter, Vec3 direction) {
        // 圣竿形态：击退攻击
        Vec3 normalizedDirection = direction.normalize();
        double range = getEffectRange() * 0.8;
        
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(normalizedDirection.scale(range));
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(1.5);
        
        level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
            if (entity == shooter) return false;
            Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
            return toEntity.dot(normalizedDirection) > 0.5;
        }).forEach(livingEntity -> {
            float damage = getAttackDamage() * 0.7f;
            if (shooter instanceof Player) {
                livingEntity.hurt(level.damageSources().playerAttack((Player) shooter), damage);
            } else {
                livingEntity.hurt(level.damageSources().mobAttack(shooter), damage);
            }
            Vec3 knockback = normalizedDirection.scale(4.0);
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(knockback));
            shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1));
        });
    }
    
    private void executeAxForm(Level level, LivingEntity shooter, Vec3 direction) {
        // 圣斧形态：范围攻击
        Vec3 normalizedDirection = direction.normalize();
        double range = getEffectRange() * 0.6;
        
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(normalizedDirection.scale(range));
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(2.0);
        
        level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
            if (entity == shooter) return false;
            Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
            return toEntity.dot(normalizedDirection) > 0.3;
        }).forEach(livingEntity -> {
            float damage = getAttackDamage() * 0.8f;
            if (shooter instanceof Player) {
                livingEntity.hurt(level.damageSources().playerAttack((Player) shooter), damage);
            } else {
                livingEntity.hurt(level.damageSources().mobAttack(shooter), damage);
            }
            shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1));
        });
    }
    
    private void executeGunForm(Level level, LivingEntity shooter, Vec3 direction) {
        // 圣枪形态：远程攻击
        Vec3 normalizedDirection = direction.normalize();
        double range = getEffectRange() * 1.5;
        
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(normalizedDirection.scale(range));
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(0.5);
        
        level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
            if (entity == shooter) return false;
            Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
            return toEntity.dot(normalizedDirection) > 0.8;
        }).forEach(livingEntity -> {
            float damage = getAttackDamage() * 0.6f;
            if (shooter instanceof Player) {
                livingEntity.hurt(level.damageSources().playerAttack((Player) shooter), damage);
            } else {
                livingEntity.hurt(level.damageSources().mobAttack(shooter), damage);
            }
            livingEntity.setRemainingFireTicks(60);
        });
    }
    
    private void executeClimaxForm(Level level, LivingEntity shooter, Vec3 direction) {
        // 高潮形态：终极攻击
        Vec3 normalizedDirection = direction.normalize();
        double range = getEffectRange();
        
        // 创建大范围攻击区域
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(
            shooter.getX() - range, shooter.getY() - 2, shooter.getZ() - range,
            shooter.getX() + range, shooter.getY() + 3, shooter.getZ() + range
        );
        
        level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> entity != shooter).forEach(livingEntity -> {
            float damage = getAttackDamage() * 1.2f;
            if (shooter instanceof Player) {
                livingEntity.hurt(level.damageSources().playerAttack((Player) shooter), damage);
            } else {
                livingEntity.hurt(level.damageSources().mobAttack(shooter), damage);
            }
            Vec3 knockback = livingEntity.position().subtract(shooter.position()).normalize().scale(3.0);
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(knockback));
        });
        
        // 给予全能力量提升
        shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 3));
        shooter.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2));
        shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));
    }

    @Override
    public String getRiderName() {
        return "Den-O";
    }

    @Override
    public String getActivationSoundName() {
        return "Climax Time!";
    }

    @Override
    public float getAttackDamage() {
        return 46.0f;
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
}