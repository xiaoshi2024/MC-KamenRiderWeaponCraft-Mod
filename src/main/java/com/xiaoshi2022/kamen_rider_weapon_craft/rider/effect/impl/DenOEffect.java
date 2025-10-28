package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.den_o.DenOTrainEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class DenOEffect extends AbstractHeiseiRiderEffect {

    // 电王的不同形态
    private enum DenOForm {
        Sword, 
        FishingRod, 
        Ax, 
        Gun
    }

    private final Random random = new Random();

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 随机选择一个电王形态
            DenOForm selectedForm = DenOForm.values()[random.nextInt(DenOForm.values().length)];
            
            switch (selectedForm) {
                case Sword:
                    executeSwordFormAttack(level, player, direction);
                    // 剑形态buff：增加伤害和速度
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                    break;
                case FishingRod:
                    executeFishingRodFormAttack(level, player, direction);
                    // 鱼竿形态buff：增强生命恢复和幸运
                    player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.LUCK, 200, 3));
                    break;
                case Ax:
                    executeAxFormAttack(level, player, direction);
                    // 斧形态buff：增加力量和抗性
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 3));
                    break;
                case Gun:
                    executeGunFormAttack(level, player, direction);
                    // 枪形态buff：增加敏捷和穿透能力
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2));
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 0, 0)); // 清除虚弱效果
                    break;
            }
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    @Override
    public float getEffectRange() {
        return 12.0f;
    }
    
    private void executeSwordFormAttack(Level level, Player player, Vec3 direction) {
        // 剑形态：生成电王列车剑武器
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            DenOTrainEntity.spawn(serverLevel, player, direction, getAttackDamage(), "Sword");
        }
        
        // 检查玩家手持的是否是Heiseisword武器并附加电王实体到武器尖端
        if (player.getMainHandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword) {
            com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.Heiseisword.HeiseiswordRenderer.attachDenOEntityToWeapon(player, player.getMainHandItem(), "Sword");
        }
    }
    
    private void executeFishingRodFormAttack(Level level, Player player, Vec3 direction) {
        // 鱼竿形态：抛出鱼线并拉回敌人
        if (!level.isClientSide && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // 创建鱼线实体
            DenOTrainEntity fishline = DenOTrainEntity.spawn(serverLevel, player, direction, getAttackDamage(), "FishingRod");
            
            // 应用特殊鱼线效果
            applyFishingRodEffect(level, player, direction);
        }
        
        // 检查玩家手持的是否是Heiseisword武器并附加电王实体到武器尖端
        if (player.getMainHandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword) {
            com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.Heiseisword.HeiseiswordRenderer.attachDenOEntityToWeapon(player, player.getMainHandItem(), "FishingRod");
        }
    }
    
    private void applyFishingRodEffect(Level level, Player player, Vec3 direction) {
        // 鱼线效果：拉回敌人
        double reach = 15.0;
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(reach));
        
        // 查找最远的敌人
        net.minecraft.world.entity.LivingEntity target = null;
        double maxDistance = reach;
        
        for (net.minecraft.world.entity.LivingEntity entity : level.getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class, 
                new net.minecraft.world.phys.AABB(start, end).inflate(2.0),
                e -> e != player)) {
            
            double distance = player.distanceTo(entity);
            if (distance < maxDistance && player.hasLineOfSight(entity)) {
                maxDistance = distance;
                target = entity;
            }
        }
        
        // 如果找到目标，拉回敌人并造成伤害
        if (target != null) {
            // 造成伤害
            target.hurt(level.damageSources().playerAttack(player), getAttackDamage() * 0.5f);
            
            // 拉回敌人到玩家身边
            Vec3 pullDir = player.position().subtract(target.position()).normalize();
            target.setDeltaMovement(pullDir.scale(1.5));
            
            // 添加粒子效果表示鱼线
            for (int i = 0; i < 10; i++) {
                double t = (double)i / 10.0;
                double x = start.x + (target.position().x - start.x) * t;
                double y = start.y + (target.position().y - start.y) * t;
                double z = start.z + (target.position().z - start.z) * t;
                
                level.addParticle(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, 
                        x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }
    
    private void executeAxFormAttack(Level level, Player player, Vec3 direction) {
        // 斧形态：生成电王列车斧武器
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            DenOTrainEntity.spawn(serverLevel, player, direction, getAttackDamage(), "Ax");
        }
        
        // 检查玩家手持的是否是Heiseisword武器并附加电王实体到武器尖端
        if (player.getMainHandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword) {
            com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.Heiseisword.HeiseiswordRenderer.attachDenOEntityToWeapon(player, player.getMainHandItem(), "Ax");
        }
    }
    
    private void executeGunFormAttack(Level level, Player player, Vec3 direction) {
        // 枪形态：生成3个电王列车枪武器（快速发射）
        // 确保只在服务器端生成实体
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (int i = 0; i < 3; i++) {
                double spreadX = (level.random.nextDouble() - 0.5) * 0.2;
                double spreadY = (level.random.nextDouble() - 0.5) * 0.2;
                double spreadZ = (level.random.nextDouble() - 0.5) * 0.2;
                Vec3 bulletDir = new Vec3(
                    direction.x + spreadX,
                    direction.y + spreadY,
                    direction.z + spreadZ
                ).normalize();
                
                DenOTrainEntity.spawn(serverLevel, player, bulletDir, getAttackDamage(), "Gun");
            }
        }
        
        // 检查玩家手持的是否是Heiseisword武器并附加电王实体到武器尖端
        if (player.getMainHandItem().getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword) {
            com.xiaoshi2022.kamen_rider_weapon_craft.Item.client.Heiseisword.HeiseiswordRenderer.attachDenOEntityToWeapon(player, player.getMainHandItem(), "Gun");
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

    /**
     * 为非玩家实体实现鱼线效果
     */
    private void applyFishingRodEffectForNonPlayer(Level level, net.minecraft.world.entity.LivingEntity shooter, Vec3 direction) {
        // 鱼线效果：拉回敌人
        double reach = 15.0;
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(reach));
        
        // 查找最远的敌人
        net.minecraft.world.entity.LivingEntity target = null;
        double maxDistance = reach;
        
        for (net.minecraft.world.entity.LivingEntity entity : level.getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class, 
                new net.minecraft.world.phys.AABB(start, end).inflate(2.0),
                e -> e != shooter && shooter.canAttack(e))) {
            
            double distance = shooter.distanceTo(entity);
            // 简化的视线检测
            if (distance < maxDistance) {
                maxDistance = distance;
                target = entity;
            }
        }
        
        // 如果找到目标，拉回敌人并造成伤害
        if (target != null) {
            // 造成伤害 - 使用更合适的伤害源方法
            if (shooter instanceof net.minecraft.world.entity.Mob mob) {
                target.hurt(level.damageSources().mobAttack(mob), getAttackDamage() * 0.5f);
            } else {
                // 使用泛用伤害源
                target.hurt(level.damageSources().generic(), getAttackDamage() * 0.5f);
            }
            
            // 拉回敌人到射击者身边
            Vec3 pullDir = shooter.position().subtract(target.position()).normalize();
            target.setDeltaMovement(pullDir.scale(1.5));
            
            // 添加粒子效果表示鱼线
            for (int i = 0; i < 10; i++) {
                double t = (double)i / 10.0;
                double x = start.x + (target.position().x - start.x) * t;
                double y = start.y + (target.position().y - start.y) * t;
                double z = start.z + (target.position().z - start.z) * t;
                
                level.addParticle(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, 
                        x, y, z, 0.0, 0.0, 0.0);
            }
        }
    }
    
    /**
     * 为非玩家实体（如僵尸）执行电王的特殊攻击效果
     * 允许非玩家实体也能使用电王的各种形态技能
     */
    @Override
    public void executeNonPlayerSpecialAttack(Level level, net.minecraft.world.entity.LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 播放骑士选择音效
            com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager.playSelectionSound(level, shooter, getRiderName());
            
            // 播放攻击音效
            com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager.playAttackSound(level, shooter, getRiderName());
            
            // 随机选择一个电王形态
            DenOForm selectedForm = DenOForm.values()[level.random.nextInt(DenOForm.values().length)];
            
            switch (selectedForm) {
                case Sword:
                    // 剑形态 - 确保只在服务器端生成实体
                    if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        DenOTrainEntity.spawn(serverLevel, shooter, direction, getAttackDamage(), "Sword");
                    }
                    break;
                case FishingRod:
                    // 鱼竿形态：抛出鱼线并拉回敌人
                    if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        DenOTrainEntity.spawn(serverLevel, shooter, direction, getAttackDamage(), "FishingRod");
                    }
                    
                    // 应用鱼线效果
                    if (shooter instanceof net.minecraft.world.entity.Mob mob) {
                        applyFishingRodEffectForNonPlayer(level, shooter, direction);
                    }
                    break;
                case Ax:
                    // 斧形态 - 确保只在服务器端生成实体
                    if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        DenOTrainEntity.spawn(serverLevel, shooter, direction, getAttackDamage(), "Ax");
                    }
                    break;
                case Gun:
                    // 枪形态 - 确保只在服务器端生成实体
                    if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        for (int i = 0; i < 3; i++) {
                            double spreadX = (level.random.nextDouble() - 0.5) * 0.2;
                            double spreadY = (level.random.nextDouble() - 0.5) * 0.2;
                            double spreadZ = (level.random.nextDouble() - 0.5) * 0.2;
                            Vec3 bulletDir = new Vec3(
                                direction.x + spreadX,
                                direction.y + spreadY,
                                direction.z + spreadZ
                            ).normalize();
                            
                            DenOTrainEntity.spawn(serverLevel, shooter, bulletDir, getAttackDamage(), "Gun");
                        }
                    }
                    break;
            }
            
            // 应用视觉效果
            applyVisualEffects(level, shooter, direction);
        }
    }
}
