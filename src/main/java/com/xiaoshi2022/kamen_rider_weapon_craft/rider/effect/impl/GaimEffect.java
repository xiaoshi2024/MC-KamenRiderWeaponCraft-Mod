package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GaimEffect extends AbstractHeiseiRiderEffect {

    // 铠武的不同锁种
    private enum LockSeed {
        Orange, 
        Pineapple, 
        Banana, 
        Melon
    }

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Kachidoki Arms攻击，使用不同的锁种能力
            // 随机选择一个锁种
            LockSeed selectedSeed = LockSeed.values()[level.random.nextInt(LockSeed.values().length)];
            
            switch (selectedSeed) {
                case Orange:
                    executeOrangeLockSeed(level, player, direction);
                    break;
                case Pineapple:
                    executePineappleLockSeed(level, player, direction);
                    break;
                case Banana:
                    executeBananaLockSeed(level, player, direction);
                    break;
                case Melon:
                    executeMelonLockSeed(level, player, direction);
                    break;
            }
            
            // 给予玩家相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    private void executeOrangeLockSeed(Level level, Player player, Vec3 direction) {
        // 橙子锁种：快速连击
        for (int i = 0; i < 5; i++) {
            double distance = 2.0 + (i * 0.5);
            Vec3 targetPos = player.getEyePosition(1.0f).add(direction.scale(distance));
            level.getEntities(player, new net.minecraft.world.phys.AABB(targetPos, targetPos).inflate(1.0))
                .forEach(entity -> {
                    if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                        ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                            level.damageSources().playerAttack(player), getAttackDamage() * 0.4f);
                    }
                });
        }
    }
    
    private void executePineappleLockSeed(Level level, Player player, Vec3 direction) {
        // 菠萝锁种：范围爆炸
        Vec3 targetPos = player.getEyePosition(1.0f).add(direction.scale(4.0));
        level.explode(player, targetPos.x, targetPos.y, targetPos.z, 
            getAttackDamage() / 3, Level.ExplosionInteraction.MOB);
    }
    
    private void executeBananaLockSeed(Level level, Player player, Vec3 direction) {
        // 香蕉锁种：滑动攻击 - 优化：移除Thread.sleep，使用更高效的实现
        Vec3 slideVelocity = direction.scale(3.0);
        player.setDeltaMovement(slideVelocity);
        
        // 直接检测并伤害前方路径上的敌人，避免使用Thread.sleep
        // 计算滑动路径上的多个点
        for (int i = 0; i < 5; i++) {
            double distance = 1.0 + (i * 0.75);
            Vec3 pathPos = player.getEyePosition(1.0f).add(direction.scale(distance));
            
            // 优化：使用更高效的实体查找方式
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                    new net.minecraft.world.phys.AABB(pathPos, pathPos).inflate(1.5),
                    entity -> entity != player) // 提前过滤掉玩家自己
                .forEach(entity -> {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.8f);
                });
        }
    }
    
    private void executeMelonLockSeed(Level level, Player player, Vec3 direction) {
        // 西瓜锁种：防御和反击
        // 给予玩家短暂的无敌效果
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 255));
        
        // 反弹周围敌人的攻击（简化实现为直接伤害）
        level.getEntities(player, player.getBoundingBox().inflate(getEffectRange()))
            .forEach(entity -> {
                if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.6f);
                }
            });
    }

    @Override
    public String getRiderName() {
        return "Gaim";
    }

    @Override
    public String getActivationSoundName() {
        return "Kachidoki Arms!";
    }

    @Override
    public float getAttackDamage() {
        return 50.0f; // 普通骑士 - Gaim拥有多种锁种和强大的铠甲形态，伤害略高于普通骑士
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
}
