package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.hibiki;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class HibikiRiderEffect {
    
    /**
     * 生成响鬼鼓特效
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param target 目标实体
     */
    public static void spawnDrumEffect(Level level, Entity owner, LivingEntity target) {
        HibikiDrumEffectEntity.spawnEffect(level, owner, target);
    }
    
    /**
     * 生成响鬼鼓特效（简化方法，自动寻找最近目标）
     * @param level 世界对象
     * @param owner 拥有者实体
     */
    public static void spawnDrumEffectWithTarget(Level level, Entity owner) {
        if (owner instanceof LivingEntity livingOwner) {
            // 寻找最近的敌对实体作为目标
            double searchRange = 10.0;
            LivingEntity nearestTarget = level.getEntitiesOfClass(LivingEntity.class,
                    livingOwner.getBoundingBox().inflate(searchRange),
                    entity -> entity != owner && entity.isAlive())
                    .stream()
                    .min(java.util.Comparator.comparingDouble(livingOwner::distanceToSqr))
                    .orElse(null);
            
            if (nearestTarget != null) {
                spawnDrumEffect(level, owner, nearestTarget);
            }
        }
    }
}