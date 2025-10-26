package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid.ExAidSlashEffectEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class ExAidEffect extends AbstractHeiseiRiderEffect {

    // 存储需要受到持续伤害的实体和对应的剩余时间（游戏刻）
    private static final Map<LivingEntity, Integer> DAMAGE_TARGETS = new HashMap<>();
    private static final int DURATION = 60;
    private static final int DAMAGE_INTERVAL = 10;

    // 存储延迟生成的特效信息
    private static final Map<Integer, DelayedEffectInfo> DELAYED_EFFECTS = new HashMap<>();
    private static int nextEffectId = 0;

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        // 统一在服务器端处理攻击逻辑和特效生成，确保同步一致性
        if (!level.isClientSide) {
            // 为玩家添加增益效果
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 400, 1));

            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 400, 1));

            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.REGENERATION, 200, 2));

            // 1. 对前方敌人造成伤害
            double reach = 8.0;
            Vec3 start = player.getEyePosition(1.0f);
            Vec3 end = start.add(direction.scale(reach));

            // 使用改进的实体检测方法
            Entity hitEntity = getTargetEntity(level, player, start, end, reach);

            if (hitEntity instanceof LivingEntity && hitEntity != player) {
                LivingEntity livingEntity = (LivingEntity) hitEntity;

                // 降低单次伤害，改为短时间持续伤害
                float initialDamage = getAttackDamage() * 0.5f;

                // 确保伤害能够造成，即使实体有一定的抗性
                // 再次确认不是释放者本人，防止误判造成自伤
                if (livingEntity != player) {
                    boolean hurt = livingEntity.hurt(
                            level.damageSources().playerAttack(player), initialDamage);
                }

                // 无论是否成功造成伤害，都添加到持续伤害列表
                addToDamageTargets(livingEntity, player);

                // 生成特效，确保玩家有视觉反馈
                spawnSlashEffectOnEntity(hitEntity, level, player, direction);

                // 立即生成一个初始特效
                ExAidSlashEffectEntity.spawnEffectOnTarget(level, player, hitEntity);
            } else {
                // 即使没有击中实体，也生成基础特效
                ExAidSlashEffectEntity.spawnEffect(level, player, direction);
            }
        }
        // 客户端只生成预览特效
        else {
            // 确保有对应的客户端生成方法
            try {
                Class<?> effectClass = Class.forName("com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid.ExAidRiderEffect");
                java.lang.reflect.Method method = effectClass.getMethod("spawnSlashEffectByOwnerDirection", Level.class, Player.class);
                method.invoke(null, level, player);
            } catch (Exception e) {
                // 如果反射失败，使用备用方法
                ExAidSlashEffectEntity.spawnEffect(level, player, direction);
            }
        }
    }
    
    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 为非玩家实体（如僵尸）生成ExAid特效实体
            double reach = 8.0;
            Vec3 start = shooter.getEyePosition(1.0f);
            Vec3 end = start.add(direction.scale(reach));
            
            // 使用改进的实体检测方法
            Entity hitEntity = getTargetEntityForMob(level, shooter, start, end, reach);
            
            if (hitEntity instanceof LivingEntity && hitEntity != shooter) {
                LivingEntity livingEntity = (LivingEntity) hitEntity;
                
                // 降低单次伤害，改为短时间持续伤害
                float initialDamage = getAttackDamage() * 0.5f;
                
                // 确保伤害能够造成，即使实体有一定的抗性
                if (livingEntity != shooter) {
                    livingEntity.hurt(level.damageSources().mobAttack(shooter), initialDamage);
                }
                
                // 生成特效实体
                ExAidSlashEffectEntity.spawnEffectOnTarget(level, shooter, hitEntity);
            } else {
                // 即使没有击中实体，也生成基础特效
                ExAidSlashEffectEntity.spawnEffect(level, shooter, direction);
            }
        } else {
            // 客户端只生成预览特效
            ExAidSlashEffectEntity.spawnEffect(level, shooter, direction);
        }
    }
    
    /**
     * 为非玩家实体查找目标实体
     */
    private Entity getTargetEntityForMob(Level level, LivingEntity shooter, Vec3 start, Vec3 end, double reach) {
        // 简化的实体检测实现
        net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(start, end).inflate(1.0);
        
        // 查找最近的敌对实体
        return level.getEntitiesOfClass(LivingEntity.class, searchBox, 
                entity -> entity != shooter && entity.isAlive())
                .stream()
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 在指定方向上查找目标实体
     */
    private Entity getTargetEntity(Level level, Player player, Vec3 start, Vec3 end, double reach) {
        // 简化的实体检测实现
        net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(start, end).inflate(1.0);
        
        // 查找最近的敌对实体
        return level.getEntitiesOfClass(LivingEntity.class, searchBox, 
                entity -> entity != player && entity.isAlive())
                .stream()
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 在目标实体上生成剑砍特效
     */
    private void spawnSlashEffectOnEntity(Entity target, Level level, Player player, Vec3 direction) {
        // 生成特效
        ExAidSlashEffectEntity.spawnEffectOnTarget(level, player, target);
    }
    
    /**
     * 将目标实体添加到持续伤害列表
     */
    private void addToDamageTargets(LivingEntity target, Player source) {
        DAMAGE_TARGETS.put(target, DURATION);
    }

    @Override
    public String getRiderName() {
        return "Ex-Aid";
    }

    @Override
    public String getActivationSoundName() {
        return "Hyper Critical Sparking!";
    }

    @Override
    public float getAttackDamage() {
        return 55.0f; // 提升伤害值
    }

    @Override
    public float getEffectRange() {
        return 10.0f; // 扩大效果范围
    }
    
    /**
     * 延迟特效信息类
     */
    private static class DelayedEffectInfo {
        final Level level;
        final LivingEntity shooter;
        final Vec3 direction;
        final int delay;

        DelayedEffectInfo(Level level, LivingEntity shooter, Vec3 direction, int delay) {
            this.level = level;
            this.shooter = shooter;
            this.direction = direction;
            this.delay = delay;
        }
    }
}