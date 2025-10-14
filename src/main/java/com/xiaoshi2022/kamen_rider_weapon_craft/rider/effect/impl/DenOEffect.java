package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class DenOEffect implements HeiseiRiderEffect {

    // 电王的不同形态
    private enum DenOForm {
        Sword, 
        Rod, 
        Ax, 
        Gun
    }

    private final Random random = new Random();

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 随机选择一个电王形态
            DenOForm selectedForm = DenOForm.values()[random.nextInt(DenOForm.values().length)];
            
            switch (selectedForm) {
                case Sword:
                    executeSwordFormAttack(level, player, direction);
                    break;
                case Rod:
                    executeRodFormAttack(level, player, direction);
                    break;
                case Ax:
                    executeAxFormAttack(level, player, direction);
                    break;
                case Gun:
                    executeGunFormAttack(level, player, direction);
                    break;
            }
            
            // 给予玩家时间相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    private void executeSwordFormAttack(Level level, Player player, Vec3 direction) {
        // 剑形态：连续的剑击
        double reach = 8.0;
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(reach));
        
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                new net.minecraft.world.phys.AABB(start, end).inflate(1.0),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                // 连续两次攻击
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 0.6f);
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 0.6f);
            });
    }
    
    private void executeRodFormAttack(Level level, Player player, Vec3 direction) {
        // 棍形态：范围连击并击退敌人
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                player.getBoundingBox().inflate(5.0),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 0.5f);
                
                // 击退敌人
                Vec3 knockback = entity.position().subtract(player.position()).normalize().scale(1.5);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            });
    }
    
    private void executeAxFormAttack(Level level, Player player, Vec3 direction) {
        // 斧形态：强大的单体攻击
        double reach = 6.0;
        Vec3 targetPos = player.getEyePosition(1.0f).add(direction.scale(reach));
        
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                new net.minecraft.world.phys.AABB(targetPos, targetPos).inflate(2.0),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 1.2f);
                entity.setSecondsOnFire(3);
            });
    }
    
    private void executeGunFormAttack(Level level, Player player, Vec3 direction) {
        // 枪形态：远程射击
        for (int i = 0; i < 3; i++) {
            double spreadX = (level.random.nextDouble() - 0.5) * 0.2;
            double spreadY = (level.random.nextDouble() - 0.5) * 0.2;
            double spreadZ = (level.random.nextDouble() - 0.5) * 0.2;
            Vec3 bulletDir = new Vec3(
                direction.x + spreadX,
                direction.y + spreadY,
                direction.z + spreadZ
            ).normalize();
            
            // 发射子弹
            double bulletReach = 15.0;
            Vec3 bulletStart = player.getEyePosition(1.0f);
            Vec3 bulletEnd = bulletStart.add(bulletDir.scale(bulletReach));
            
            // 优化：使用更高效的实体查找方式
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                    new net.minecraft.world.phys.AABB(bulletStart, bulletEnd).inflate(0.5),
                    entity -> entity != player) // 提前过滤掉玩家自己
                .forEach(entity -> {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.4f);
                });
        }
    }

    @Override
    public String getRiderName() {
        return "Den-O";
    }

    @Override
    public String getActivationSoundName() {
        return "Full Charge!";
    }

    @Override
    public float getAttackDamage() {
        return 48.0f; // 普通骑士 - Den-O拥有多种形态和时间力量，伤害略高于普通骑士
    }

    @Override
    public float getEffectRange() {
        return 12.0f;
    }
}
