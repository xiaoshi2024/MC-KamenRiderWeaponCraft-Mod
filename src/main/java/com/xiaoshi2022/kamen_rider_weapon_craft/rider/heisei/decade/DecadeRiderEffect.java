package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.decade;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Kamen Rider Decade 特效类
 * 用于生成和控制Decade骑士的次元踢特效
 */
public class DecadeRiderEffect {
    
    /**
     * 生成Decade骑士的次元踢特效
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param direction 特效移动方向
     * @param attackDamage 攻击力
     */
    public static void spawnDimensionKickEffect(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        // 调用实体类中的静态方法生成特效
        DecadeRiderEntity.trySpawnEffect(level, owner, direction, attackDamage);
    }
    
    /**
     * 生成Decade骑士的次元踢特效（以拥有者的朝向为方向）
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param attackDamage 攻击力
     */
    public static void spawnDimensionKickEffectByOwnerDirection(Level level, LivingEntity owner, float attackDamage) {
        // 获取拥有者的朝向
        Vec3 lookVector = owner.getViewVector(1.0F);
        spawnDimensionKickEffect(level, owner, lookVector, attackDamage);
    }
    
    /**
     * 生成Decade骑士的招牌技能 - Complete Form Dimension Kick
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param direction 特效移动方向
     * @param attackDamage 攻击力
     */
    public static void spawnCompleteFormDimensionKick(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        // 增强版次元踢，可以生成多个特效实体形成连击
        for (int i = 0; i < 3; i++) {
            // 延迟生成，形成连击效果
            final int delay = i * 5;
            final float damage = attackDamage * (1.0f + i * 0.1f); // 每一击伤害递增
            
            // 这里可以添加延迟逻辑，或者在实际实现中使用计时器
            DecadeRiderEntity.trySpawnEffect(level, owner, direction.add(0, i * 0.1, 0), damage);
        }
    }
}