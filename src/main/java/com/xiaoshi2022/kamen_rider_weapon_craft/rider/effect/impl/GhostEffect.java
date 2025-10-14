package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GhostEffect implements HeiseiRiderEffect {

    // Ghost的不同眼魂
    private enum GhostEyecon {
        Ore, 
        TouconBoost, 
        Grateful, 
        Mugen, 
        OreDamashii
    }

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Omega Drive攻击，使用不同的眼魂
            // 随机选择一个眼魂
            GhostEyecon selectedEyecon = GhostEyecon.values()[level.random.nextInt(GhostEyecon.values().length)];
            
            switch (selectedEyecon) {
                case Ore:
                    executeOreMode(level, player, direction);
                    break;
                case TouconBoost:
                    executeTouconBoostMode(level, player, direction);
                    break;
                case Grateful:
                    executeGratefulMode(level, player, direction);
                    break;
                case Mugen:
                    executeMugenMode(level, player, direction);
                    break;
                case OreDamashii:
                    executeOreDamashiiMode(level, player, direction);
                    break;
            }
            
            // 给予玩家幽灵相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    private void executeOreMode(Level level, Player player, Vec3 direction) {
        // 俺魂模式：基本形态的攻击
        // 向前方发射灵魂能量
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(15.0));
        
        net.minecraft.world.phys.HitResult result = player.pick(10.0, 0.0f, false);
        
        if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage());
                
                // 击退敌人
                Vec3 knockback = direction.scale(1.5);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            }
        }
    }
    
    private void executeTouconBoostMode(Level level, Player player, Vec3 direction) {
        // 斗魂鼓舞模式：火焰灵魂攻击
        // 给予玩家再生效果
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        
        // 前方制造火焰路径 - 优化：减少遍历次数，使用更高效的实体查找
        for (int i = 0; i < 8; i++) { // 从10减少到8
            Vec3 firePos = player.getEyePosition(1.0f).add(direction.scale(i + 1));
            
            // 优化：使用更高效的实体查找方式
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                    new net.minecraft.world.phys.AABB(firePos, firePos).inflate(1.2),
                    entity -> entity != player) // 提前过滤掉玩家自己
                .forEach(entity -> {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.4f);
                    ((net.minecraft.world.entity.LivingEntity) entity).setSecondsOnFire(5);
                });
        }
    }
    
    private void executeGratefulMode(Level level, Player player, Vec3 direction) {
        // 感恩魂模式：召唤15位平成骑士的力量
        // 对周围敌人造成伤害并给予玩家防御增益
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2));
        
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                player.getBoundingBox().inflate(8.0),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 0.8f);
            });
    }
    
    private void executeMugenMode(Level level, Player player, Vec3 direction) {
        // 无限魂模式：大范围的灵魂爆发
        // 对周围所有敌人造成伤害并给予玩家强大增益
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 3));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 2));
        
        // 优化：使用更高效的实体查找方式和限制范围
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                player.getBoundingBox().inflate(10.0),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 1.2f);
            });
    }
    
    private void executeOreDamashiiMode(Level level, Player player, Vec3 direction) {
        // 俺魂基本模式：简单但有效的攻击
        // 向前方发射强大的灵魂能量
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(12.0));
        
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                new net.minecraft.world.phys.AABB(start, end),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 1.0f);
            });
    }

    @Override
    public String getRiderName() {
        return "Ghost";
    }

    @Override
    public String getActivationSoundName() {
        return "Omega Drive!";
    }

    @Override
    public float getAttackDamage() {
        return 49.0f; // 普通骑士 - Ghost拥有多种眼魂和强大的无限形态，伤害略高于普通骑士
    }

    @Override
    public float getEffectRange() {
        return 10.0f;
    }
}
