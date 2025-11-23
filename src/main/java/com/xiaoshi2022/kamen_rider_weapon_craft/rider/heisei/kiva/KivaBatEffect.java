package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kiva;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Kamen Rider Kiva 蝙蝠群特效类
 * 用于生成和控制Kiva骑士的蝙蝠群攻击特效
 */
public class KivaBatEffect {
    
    // 黄金螺旋参数
    private static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2;
    
    /**
     * 生成Kiva骑士的蝙蝠群攻击特效
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param direction 特效移动方向
     * @param attackDamage 攻击力
     * @param count 蝙蝠数量
     */
    public static void spawnBatSwarm(Level level, LivingEntity owner, Vec3 direction, float attackDamage, int count) {
        if (!level.isClientSide) {
            Vec3 playerPos = owner.getEyePosition(1.0F);
            
            // 生成追踪和飞行的蝙蝠
            spawnTrackingBatPattern(level, owner, direction, attackDamage, count, playerPos);
        }
    }
    
    /**
     * 生成追踪模式的蝙蝠群 - 修改为聚集模式
     * 这些蝙蝠会聚集在一起并追踪攻击目标实体
     */
    private static void spawnTrackingBatPattern(Level level, LivingEntity owner, Vec3 baseDirection, float attackDamage, int count, Vec3 playerPos) {
        // 生成蝙蝠，按聚集模式分布
        for (int i = 0; i < count; i++) {
            // 聚集模式：使用较小的随机偏移，让蝙蝠集中在一个区域
            // 为了让蝙蝠聚集在一起，我们使用更小的随机范围
            double xOffset = (level.random.nextDouble() - 0.5) * 0.6; // 减小随机范围，让蝙蝠更集中
            double yOffset = (level.random.nextDouble() - 0.5) * 0.6;
            double zOffset = (level.random.nextDouble() - 0.5) * 0.6;
            
            // 创建集中分布的偏移向量
            Vec3 localOffset = new Vec3(xOffset, yOffset, zOffset).normalize();
            
            // 计算带有微小偏移的方向向量，保持蝙蝠群的整体方向性
            Vec3 direction = calculateDirectionWithOffset(baseDirection, localOffset);
            
            // 添加微小的随机偏差，使蝙蝠分布更自然但仍保持聚集
            direction = new Vec3(
                direction.x + (level.random.nextDouble() - 0.5) * 0.1, // 进一步减小随机偏差
                direction.y + (level.random.nextDouble() - 0.5) * 0.1,
                direction.z + (level.random.nextDouble() - 0.5) * 0.1
            ).normalize();
            
            // 创建一个追踪蝙蝠实体
            KivaBatEntity.trySpawnEffect(level, owner, direction, attackDamage);
        }
    }
    
    /**
     * 计算带有偏移的方向向量
     * 这确保蝙蝠群会从不同角度发起攻击
     */
    private static Vec3 calculateDirectionWithOffset(Vec3 baseDirection, Vec3 localOffset) {
        // 将局部坐标系的偏移转换为全局坐标系
        // 这里使用简化的旋转，实际项目中可能需要更复杂的旋转矩阵
        
        // 假设baseDirection是前方向
        Vec3 forward = baseDirection.normalize();
        
        // 计算右侧方向（简化版）
        Vec3 right = new Vec3(-forward.z, 0, forward.x).normalize();
        
        // 计算上方向
        Vec3 up = forward.cross(right).normalize();
        
        // 使用正交基构建全局坐标系中的偏移向量
        double globalX = localOffset.x * right.x + localOffset.y * forward.x + localOffset.z * up.x;
        double globalY = localOffset.x * right.y + localOffset.y * forward.y + localOffset.z * up.y;
        double globalZ = localOffset.x * right.z + localOffset.y * forward.z + localOffset.z * up.z;
        
        // 返回合成后的方向向量
        return new Vec3(globalX, globalY, globalZ).normalize();
    }
    
    /**
     * 生成Kiva骑士的蝙蝠群攻击特效（简化方法，使用拥有者的朝向）
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param attackDamage 攻击力
     */
    public static void spawnBatSwarmByOwnerDirection(Level level, LivingEntity owner, float attackDamage) {
        // 获取拥有者的朝向
        Vec3 lookVector = owner.getViewVector(1.0F);
        // 增加蝙蝠数量到24只，让聚集效果更明显
        spawnBatSwarm(level, owner, lookVector, attackDamage, 24);
    }
    
    /**
     * 生成Kiva骑士的蝙蝠群攻击特效（使用旋转角度）
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param yRot 水平旋转角度
     * @param xRot 垂直旋转角度
     * @param attackDamage 攻击力
     * @param count 蝙蝠数量
     */
    public static void spawnBatSwarmByRotation(Level level, LivingEntity owner, float yRot, float xRot, float attackDamage, int count) {
        // 将角度转换为方向向量
        Vec3 direction = calculateDirectionFromRotations(yRot, xRot);
        spawnBatSwarm(level, owner, direction, attackDamage, count);
    }
    
    /**
     * 计算指定旋转角度对应的方向向量
     * @param yRot 水平旋转角度
     * @param xRot 垂直旋转角度
     * @return 方向向量
     */
    private static Vec3 calculateDirectionFromRotations(float yRot, float xRot) {
        // 转换角度到弧度
        float yRadians = (float)Math.toRadians(yRot);
        float xRadians = (float)Math.toRadians(xRot);
        
        // 计算方向向量
        float xComponent = -Mth.sin(yRadians) * Mth.cos(xRadians);
        float zComponent = Mth.cos(yRadians) * Mth.cos(xRadians);
        float yComponent = -Mth.sin(xRadians);
        
        return new Vec3(xComponent, yComponent, zComponent).normalize();
    }
}