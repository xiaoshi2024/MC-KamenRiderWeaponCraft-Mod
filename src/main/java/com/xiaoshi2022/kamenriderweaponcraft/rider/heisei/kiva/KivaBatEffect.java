package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.kiva;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class KivaBatEffect {
    
    /**
     * 生成蝙蝠群特效
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param direction 方向向量
     * @param attackDamage 攻击力
     */
    public static void spawnBatSwarm(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        // 生成多只蝙蝠，形成蝙蝠群效果
        for (int i = 0; i < 5; i++) {
            // 为每只蝙蝠添加随机偏移
            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetY = (level.random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
            
            Vec3 offsetDirection = new Vec3(
                direction.x + offsetX * 0.3,
                direction.y + offsetY * 0.3,
                direction.z + offsetZ * 0.3
            ).normalize();
            
            KivaBatEntity.trySpawnEffect(level, owner, offsetDirection, attackDamage);
        }
    }
    
    /**
     * 生成蝙蝠群特效（使用拥有者的朝向）
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param attackDamage 攻击力
     */
    public static void spawnBatSwarmByOwnerDirection(Level level, LivingEntity owner, float attackDamage) {
        Vec3 lookVector = owner.getViewVector(1.0F);
        spawnBatSwarm(level, owner, lookVector, attackDamage);
    }
}