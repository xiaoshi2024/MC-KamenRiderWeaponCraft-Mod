package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.gaim.GaimLockSeedEntity;
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
        // 无论客户端还是服务器端，都生成锁种特效实体
        // 调整锁种出现几率：橙子(Orange)出现几率大，菠萝(Pineapple)出现几率小
        LockSeed selectedSeed = getWeightedRandomLockSeed(level);
        
        // 创建并生成锁种特效实体
        spawnLockSeedEntity(level, player, direction, selectedSeed);
        
        if (!level.isClientSide) {
            // 服务器端：发动Kachidoki Arms攻击，使用不同的锁种能力
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
        }
    }
    
    /**
     * 生成锁种特效实体
     */
    private void spawnLockSeedEntity(Level level, Player player, Vec3 direction, LockSeed lockSeed) {
        // 计算生成位置（在玩家前方稍远处）
        Vec3 spawnPos = player.getEyePosition().add(direction.normalize().scale(1.0));
        
        // 根据锁种类型创建对应的实体
        // 对于菠萝锁种，在GaimEffect.executePineappleLockSeed中单独处理以实现流星雨效果
        // 这里只处理其他锁种
        if (lockSeed != LockSeed.Pineapple) {
            GaimLockSeedEntity lockSeedEntity = new GaimLockSeedEntity(
                    level,
                    player,
                    spawnPos,
                    direction,
                    lockSeed.name().toUpperCase(),
                    getAttackDamage()
            );
            
            // 将实体添加到世界中
            level.addFreshEntity(lockSeedEntity);
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
        
        // 为玩家添加速度2和力量2效果
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1)); // 速度2，持续15秒
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));  // 力量2，持续15秒
    }
    
    private void executePineappleLockSeed(Level level, Player player, Vec3 direction) {
        // 菠萝锁种：流星雨效果
        // 生成多个菠萝锁种实体，形成流星雨效果
        for (int i = 0; i < 5; i++) {
            // 计算略微不同的发射角度，形成扩散的流星雨效果
            float spreadAngle = (level.random.nextFloat() - 0.5f) * 0.4f; // -20°到20°的随机角度
            Vec3 spreadDirection = new Vec3(
                direction.x + (level.random.nextFloat() - 0.5f) * 0.3f,
                direction.y + spreadAngle,
                direction.z + (level.random.nextFloat() - 0.5f) * 0.3f
            ).normalize();
            
            // 计算生成位置
            Vec3 spawnPos = player.getEyePosition().add(spreadDirection.scale(1.0));
            
            // 创建并生成菠萝锁种实体
            GaimLockSeedEntity lockSeedEntity = new GaimLockSeedEntity(
                    level,
                    player,
                    spawnPos,
                    spreadDirection,
                    LockSeed.Pineapple.name().toUpperCase(),
                    getAttackDamage() * 0.6f // 调整伤害以适应多个实体
            );
            
            level.addFreshEntity(lockSeedEntity);
        }
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
    
    /**
     * 根据权重随机选择锁种
     * 增加Orange的出现几率，减少Pineapple的出现几率
     */
    private LockSeed getWeightedRandomLockSeed(Level level) {
        // 权重设置：Orange=50%, Banana=20%, Melon=20%, Pineapple=10%
        int randomValue = level.random.nextInt(100);
        
        if (randomValue < 50) {
            return LockSeed.Orange;
        } else if (randomValue < 70) {
            return LockSeed.Banana;
        } else if (randomValue < 90) {
            return LockSeed.Melon;
        } else {
            return LockSeed.Pineapple;
        }
    }
}
