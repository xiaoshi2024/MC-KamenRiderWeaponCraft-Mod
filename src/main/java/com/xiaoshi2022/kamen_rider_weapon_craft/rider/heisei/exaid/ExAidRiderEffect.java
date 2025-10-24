package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid;

import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;

/**
 * Kamen Rider Ex-Aid 特效管理类
 * 用于生成和控制Ex-Aid骑士的geo特效
 */
public class ExAidRiderEffect {

    /**
     * 生成Ex-Aid骑士的砍击特效
     * @param world 世界对象
     * @param owner 拥有者实体
     * @param direction 特效移动方向
     */
    public static void spawnSlashEffect(World world, LivingEntity owner, Vec3d direction) {
        // 调用实体类中的静态方法生成特效
        ExAidSlashEffectEntity.spawnEffect(world, owner, direction);
    }

    /**
     * 生成Ex-Aid骑士的砍击特效（使用方向向量的简化方法）
     * @param world 世界对象
     * @param owner 拥有者实体
     * @param yRot 水平旋转角度
     * @param xRot 垂直旋转角度
     */
    public static void spawnSlashEffectByRotation(World world, LivingEntity owner, float yRot, float xRot) {
        // 将角度转换为方向向量
        Vec3d direction = calculateDirectionFromRotations(yRot, xRot);
        spawnSlashEffect(world, owner, direction);
    }

    /**
     * 生成Ex-Aid骑士的砍击特效（以拥有者的朝向为方向）
     * @param world 世界对象
     * @param owner 拥有者实体
     */
    public static void spawnSlashEffectByOwnerDirection(World world, LivingEntity owner) {
        // 获取拥有者的朝向
        Vec3d lookVector = owner.getRotationVec(1.0F);
        spawnSlashEffect(world, owner, lookVector);
    }

    /**
     * 计算指定旋转角度对应的方向向量
     * @param yRot 水平旋转角度
     * @param xRot 垂直旋转角度
     * @return 方向向量
     */
    private static Vec3d calculateDirectionFromRotations(float yRot, float xRot) {
        // 转换角度到弧度
        float yRadians = (float)Math.toRadians(yRot);
        float xRadians = (float)Math.toRadians(xRot);

        // 计算方向向量
        float xComponent = -MathHelper.sin(yRadians) * MathHelper.cos(xRadians);
        float zComponent = MathHelper.cos(yRadians) * MathHelper.cos(xRadians);
        float yComponent = -MathHelper.sin(xRadians);

        return new Vec3d(xComponent, yComponent, zComponent).normalize();
    }

    /**
     * 生成多个砍击特效，形成组合攻击
     * @param world 世界对象
     * @param owner 拥有者实体
     * @param baseDirection 基础方向
     * @param count 特效数量
     * @param spreadAngle 扩散角度（度）
     */
    public static void spawnMultiSlashEffect(World world, LivingEntity owner, Vec3d baseDirection, int count, float spreadAngle) {
        // 如果只生成一个特效，直接调用单个特效的方法
        if (count <= 1) {
            spawnSlashEffect(world, owner, baseDirection);
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
            Vec3d direction = rotateVectorAroundY(baseDirection, currentAngle);

            // 生成特效
            spawnSlashEffect(world, owner, direction);
        }
    }

    /**
     * 围绕Y轴旋转向量
     * @param vector 原始向量
     * @param angle 旋转角度（度）
     * @return 旋转后的向量
     */
    private static Vec3d rotateVectorAroundY(Vec3d vector, float angle) {
        // 转换角度到弧度
        double radians = Math.toRadians(angle);

        // 计算旋转矩阵的分量
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        // 应用旋转
        double x = vector.x * cos - vector.z * sin;
        double z = vector.x * sin + vector.z * cos;

        return new Vec3d(x, vector.y, z).normalize();
    }
}