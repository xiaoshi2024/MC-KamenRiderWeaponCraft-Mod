package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ooo;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

/**
 * Kamen Rider OOO Geo特效类
 * 用于生成和控制OOO骑士的细胞硬币斩特效
 */
public class OOOGeoEffect {
    
    /**
     * 生成OOO骑士的细胞硬币斩特效
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param direction 特效移动方向
     * @param attackDamage 攻击力
     * @param coinType 硬币类型（用于区分不同联组的特效）
     */
    public static void spawnCellMedalSlash(Level level, LivingEntity owner, Vec3 direction, float attackDamage, String coinType) {
        // 调用实体类中的静态方法生成特效（这里是示例，实际实现需要根据geo模型系统调整）
        OOOGeoEntity.trySpawnEffect(level, owner, direction, attackDamage, coinType);
    }
    
    /**
     * 生成OOO骑士的细胞硬币斩特效（使用方向向量的简化方法）
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param yRot 水平旋转角度
     * @param xRot 垂直旋转角度
     * @param attackDamage 攻击力
     * @param coinType 硬币类型
     */
    public static void spawnCellMedalSlashByRotation(Level level, LivingEntity owner, float yRot, float xRot, float attackDamage, String coinType) {
        // 将角度转换为方向向量
        Vec3 direction = calculateDirectionFromRotations(yRot, xRot);
        spawnCellMedalSlash(level, owner, direction, attackDamage, coinType);
    }
    
    /**
     * 生成OOO骑士的细胞硬币斩特效（以拥有者的朝向为方向）
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param attackDamage 攻击力
     * @param coinType 硬币类型
     */
    public static void spawnCellMedalSlashByOwnerDirection(Level level, LivingEntity owner, float attackDamage, String coinType) {
        // 获取拥有者的朝向
        Vec3 lookVector = owner.getViewVector(1.0F);
        spawnCellMedalSlash(level, owner, lookVector, attackDamage, coinType);
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
    
    // 恐龙联组（PUTOTYRA）常量定义
    public static final String PUTOTYRA = "putotyra";
    
    // 黄金螺旋参数
    private static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2;
    
    /**
     * 生成细胞硬币吞噬效果 - 恐龙联组专属
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param baseDirection 基础方向
     * @param attackDamage 攻击力
     * @param target 目标实体（可为null，此时会自动寻找附近敌对实体）
     */
    public static void spawnCellMedalSwallow(Level level, LivingEntity owner, Vec3 baseDirection, float attackDamage, LivingEntity target) {
        // 如果没有指定目标，查找攻击方向上的目标实体
        LivingEntity finalTarget = target;
        if (finalTarget == null) {
            finalTarget = findTargetInDirection(owner, baseDirection, 15.0);
        }
        
        // 生成12个细胞硬币，形成吞噬效果
        int count = 12;
        Vec3 playerPos = owner.getEyePosition(1.0F);
        
        // 生成追踪和包裹的细胞硬币
        spawnTrackingOrbPattern(level, owner, baseDirection, attackDamage, count, playerPos, finalTarget);
        
        // 在玩家周围生成爆发性粒子效果
        spawnExplosiveParticles(level, owner.position());
    }
    
    /**
     * 生成追踪球体模式的细胞硬币
     * 这些硬币会追踪并包裹目标实体
     */
    private static void spawnTrackingOrbPattern(Level level, LivingEntity owner, Vec3 baseDirection, float attackDamage, int count, Vec3 playerPos, LivingEntity target) {
        // 生成12个硬币，按黄金螺旋分布
        for (int i = 0; i < 12; i++) {
            // 黄金螺旋算法计算点分布
            double t = i * GOLDEN_RATIO;
            double inclination = Math.acos(1 - 2 * (i + 0.5) / 12);
            double azimuth = 2 * Math.PI * t;
            
            // 转换为方向向量
            double x = Math.sin(inclination) * Math.cos(azimuth);
            double y = Math.sin(inclination) * Math.sin(azimuth);
            double z = Math.cos(inclination);
            
            // 创建局部坐标系下的偏移向量
            Vec3 localOffset = new Vec3(x, y, z).normalize();
            
            // 计算带有偏移的方向向量
            Vec3 direction = calculateDirectionWithOffset(baseDirection, localOffset);
            
            // 添加微小的随机偏差，使硬币分布更自然
            direction = new Vec3(
                direction.x + (level.random.nextDouble() - 0.5) * 0.1,
                direction.y + (level.random.nextDouble() - 0.5) * 0.1,
                direction.z + (level.random.nextDouble() - 0.5) * 0.1
            ).normalize();
            
            // 创建一个特殊的追踪硬币实体
            OOOGeoEntity.trySpawnEffect(level, owner, direction, attackDamage, PUTOTYRA);
        }
    }
    
    /**
     * 在指定方向上查找敌对实体
     */
    private static LivingEntity findTargetInDirection(LivingEntity owner, Vec3 direction, double maxDistance) {
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;
        
        // 定义搜索区域
        Vec3 start = owner.getEyePosition();
        Vec3 end = start.add(direction.normalize().scale(maxDistance));
        double searchRadius = 2.0;
        
        // 获取区域内的所有实体
        AABB searchBox = new AABB(
            Math.min(start.x, end.x) - searchRadius,
            Math.min(start.y, end.y) - searchRadius,
            Math.min(start.z, end.z) - searchRadius,
            Math.max(start.x, end.x) + searchRadius,
            Math.max(start.y, end.y) + searchRadius,
            Math.max(start.z, end.z) + searchRadius
        );
        
        List<LivingEntity> entitiesInRange = owner.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        
        // 筛选敌对实体并找出最近的
        for (LivingEntity entity : entitiesInRange) {
            if (entity != owner && owner.canAttack(entity) && entity.isAlive()) {
                // 计算实体到视线的最短距离
                double distanceToLine = distanceToLineSegment(start, end, entity.position());
                if (distanceToLine <= searchRadius) {
                    // 如果实体在视线范围内，计算实际距离
                    double distance = entity.distanceToSqr(owner);
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestTarget = entity;
                    }
                }
            }
        }
        
        return nearestTarget;
    }
    
    /**
     * 计算点到线段的最短距离
     */
    private static double distanceToLineSegment(Vec3 lineStart, Vec3 lineEnd, Vec3 point) {
        Vec3 lineVector = lineEnd.subtract(lineStart);
        Vec3 pointVector = point.subtract(lineStart);
        double lineLengthSq = lineVector.lengthSqr();
        
        if (lineLengthSq == 0.0) {
            return point.distanceTo(lineStart);
        }
        
        double t = Math.max(0, Math.min(1, pointVector.dot(lineVector) / lineLengthSq));
        Vec3 projection = lineStart.add(lineVector.scale(t));
        return point.distanceTo(projection);
    }
    
    /**
     * 生成爆发性粒子效果
     */
    private static void spawnExplosiveParticles(Level level, Vec3 position) {
        // 移除了DRAGON_BREATH和SMOKE粒子效果，避免火效果
        // 可以保留方法但不生成粒子，或者完全注释掉调用此方法的代码
    }
    
    /**
     * 根据基础方向和偏移向量计算新的方向
     * 这个方法将偏移向量转换为相对于基础方向的方向变化
     */
    private static Vec3 calculateDirectionWithOffset(Vec3 baseDirection, Vec3 offset) {
        // 创建一个局部坐标系
        // 基础方向作为Z轴
        Vec3 normalizedBase = baseDirection.normalize();
        
        // 计算X轴（与基础方向垂直）
        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(normalizedBase.y) > 0.99) {
            // 如果基础方向接近垂直，使用替代的上向量
            up = new Vec3(1, 0, 0);
        }
        Vec3 right = normalizedBase.cross(up).normalize();
        up = right.cross(normalizedBase).normalize();
        
        // 使用局部坐标系计算偏移后的方向
        return right.scale(offset.x)
            .add(up.scale(offset.y))
            .add(normalizedBase.scale(offset.z))
            .normalize();
    }
    
    /**
     * 围绕Y轴旋转向量
     * @param vector 原始向量
     * @param angle 旋转角度（度）
     * @return 旋转后的向量
     */
    private static Vec3 rotateVectorAroundY(Vec3 vector, float angle) {
        // 转换角度到弧度
        double radians = Math.toRadians(angle);
        
        // 计算旋转矩阵的分量
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        
        // 应用旋转
        double x = vector.x * cos - vector.z * sin;
        double z = vector.x * sin + vector.z * cos;
        
        return new Vec3(x, vector.y, z).normalize();
    }
    
    /**
     * 生成恐龙联组的细胞硬币吞噬特效
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param attackDamage 攻击力
     */
    public static void spawnPutotyraCellMedalSwallow(Level level, LivingEntity owner, float attackDamage) {
        // 生成紫色的细胞硬币吞噬特效
        Vec3 lookVector = owner.getViewVector(1.0F);
        spawnCellMedalSwallow(level, owner, lookVector, attackDamage, null);
        
        // 给玩家添加短暂的力量提升效果
         if (owner instanceof Player) {
             owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
             // 移除抗性效果，使用正确的效果名称或暂时注释掉
         }
    }
}