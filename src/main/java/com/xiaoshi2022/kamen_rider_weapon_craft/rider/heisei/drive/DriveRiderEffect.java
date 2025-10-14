package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.drive;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Kamen Rider Drive 特效类
 * 用于生成和控制Drive骑士的车轮特效
 */
public class DriveRiderEffect {
    
    /**
     * 生成Drive骑士的车轮特效
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param direction 特效移动方向
     * @param attackDamage 攻击力
     */
    public static void spawnWheelEffect(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        // 调用实体类中的静态方法生成特效
        DriveRiderEntity.trySpawnEffect(level, owner, direction, attackDamage);
    }
    
    /**
     * 生成Drive骑士的车轮特效（使用方向向量的简化方法）
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param yRot 水平旋转角度
     * @param xRot 垂直旋转角度
     * @param attackDamage 攻击力
     */
    public static void spawnWheelEffectByRotation(Level level, LivingEntity owner, float yRot, float xRot, float attackDamage) {
        // 将角度转换为方向向量
        Vec3 direction = calculateDirectionFromRotations(yRot, xRot);
        spawnWheelEffect(level, owner, direction, attackDamage);
    }
    
    /**
     * 生成Drive骑士的车轮特效（以拥有者的朝向为方向）
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param attackDamage 攻击力
     */
    public static void spawnWheelEffectByOwnerDirection(Level level, LivingEntity owner, float attackDamage) {
        // 获取拥有者的朝向
        Vec3 lookVector = owner.getViewVector(1.0F);
        spawnWheelEffect(level, owner, lookVector, attackDamage);
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
    
    /**
     * 生成多个车轮特效，形成组合攻击
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param baseDirection 基础方向
     * @param attackDamage 单个特效的攻击力
     * @param count 特效数量
     * @param spreadAngle 扩散角度（度）
     */
    public static void spawnMultiWheelEffect(Level level, LivingEntity owner, Vec3 baseDirection, float attackDamage, int count, float spreadAngle) {
        // 如果只生成一个特效，直接调用单个特效的方法
        if (count <= 1) {
            spawnWheelEffect(level, owner, baseDirection, attackDamage);
            return;
        }
        
        // 计算角度增量
        float angleIncrement = spreadAngle / (count - 1);
        float startAngle = -spreadAngle / 2;
        
        // 生成多个特效，每个特效朝向略微不同
        for (int i = 0; i < count; i++) {
            // 计算当前特效的角度
            float currentAngle = startAngle + i * angleIncrement;
            
            // 基于基础方向和角度生成新的方向向量
            Vec3 direction = rotateVectorAroundY(baseDirection, currentAngle);
            
            // 生成特效
            spawnWheelEffect(level, owner, direction, attackDamage);
        }
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
}