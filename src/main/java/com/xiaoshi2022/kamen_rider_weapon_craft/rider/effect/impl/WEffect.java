package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.w.WTornadoEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 只保留CycloneJoker形态的攻击
            executeCycloneJoker(level, player, direction);
            
            // 给予玩家速度加成效果
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
            
            // 服务器端：生成龙卷风特效实体
            WTornadoEntity.trySpawnTornado(level, player, direction);
        }
    }
    
    private void executeCycloneJoker(Level level, Player player, Vec3 direction) {
        // 飓风王牌形态：高速移动和剑攻击
        // 给予玩家高速移动效果
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 3));
        
        // 向前方进行快速斩击
        double reach = 8.0;
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(reach));
        
        net.minecraft.world.phys.HitResult result = player.pick(reach, 0.0f, false);
        
        if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage());
                // 给予敌人短暂的缓速效果
                ((net.minecraft.world.entity.LivingEntity) entity).addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
            }
        }
    }

    @Override
    public String getRiderName() {
        return "W";
    }

    @Override
    public String getActivationSoundName() {
        return "Cyclone Joker!";
    }

    @Override
    public float getAttackDamage() {
        return 51.0f; // 高级骑士 - W是双人一体骑士，拥有强大力量
    }

    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 为非玩家实体执行CycloneJoker形态的攻击逻辑
            executeNonPlayerCycloneJoker(level, shooter, direction);
            
            // 生成龙卷风特效实体 - 这是关键，让非玩家实体也能生成特效
            WTornadoEntity.trySpawnTornado(level, shooter, direction);
        }
    }
    
    private void executeNonPlayerCycloneJoker(Level level, LivingEntity shooter, Vec3 direction) {
        // 飓风王牌形态：非玩家实体版本
        // 给予实体速度加成效果
        shooter.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 2));
        
        // 向前方进行范围攻击
        double reach = 6.0;
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(reach));
        
        // 攻击范围内的敌人
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(start, end).inflate(2.0),
                entity -> entity != shooter && entity.isAlive())) {
            // 造成伤害
            target.hurt(level.damageSources().mobAttack(shooter), getAttackDamage() * 0.8f); // 稍微降低伤害
            // 给予敌人缓速效果
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
        }
    }
    
    @Override
    public float getEffectRange() {
        return 7.0f;
    }
}
