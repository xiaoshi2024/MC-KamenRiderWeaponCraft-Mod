package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.Faiz.FaizEmptySetEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Random;

public class FaizEffect extends AbstractHeiseiRiderEffect {

    private static final Random random = new Random();
    // Faiz主题色：红色 (RGB: 255, 0, 0)
    private static final Vector3f FAIZ_RED_COLOR = new Vector3f(1.0F, 0.0F, 0.0F);
    private static final DustParticleOptions RED_DUST = new DustParticleOptions(FAIZ_RED_COLOR, 2.5F); // 增大粒子尺寸

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        // 客户端和服务器端都执行粒子效果
        spawnAttackParticles(level, player, direction);

        if (!level.isClientSide) {
            // 服务器端：发动Axel Form攻击，高速移动并对敌人造成连续伤害
            // 1. 给予玩家极高的速度和抗性效果
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 3));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));

            // 2. 对前方敌人造成连击伤害 - 添加方向限制，只攻击玩家面前的目标
            double attackRange = getEffectRange();
            double width = attackRange / 2; // 攻击宽度，使其成为一个锥形区域
            Vec3 start = player.getEyePosition(1.0f);
            Vec3 end = start.add(direction.scale(attackRange));

            // 创建攻击范围AABB
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(
                    Math.min(start.x, end.x) - width,
                    Math.min(start.y, end.y) - 1,
                    Math.min(start.z, end.z) - width,
                    Math.max(start.x, end.x) + width,
                    Math.max(start.y, end.y) + 1,
                    Math.max(start.z, end.z) + width
            );

            // 查找范围内的实体，并添加方向检查
            level.getEntitiesOfClass(LivingEntity.class, attackBox, 
                    entity -> entity != player && entity.isAlive())
                .forEach(entity -> {
                    // 计算目标相对于起始点的向量
                    Vec3 targetRelative = entity.position().subtract(start);
                    
                    // 使用点积检查目标是否在玩家面前（角度限制）
                    // 0.7 约等于 45度角的余弦值，确保只攻击前方约45度范围内的目标
                    if (targetRelative.normalize().dot(direction.normalize()) > 0.7) {
                        // 记录伤害前的生命值
                        float healthBefore = entity.getHealth();

                        // 连续攻击，每次伤害较低但快速连续
                        for (int i = 0; i < 3; i++) {
                            entity.hurt(level.damageSources().playerAttack(player), getAttackDamage() * 0.4f);
                        }

                        // 检查敌人是否被击杀
                        if (entity.isDeadOrDying()) {
                            // 在敌人死亡位置生成空集符号实体
                            spawnEmptySetParticleEffect(level, entity.position());
                        }
                    }
                });
        }
    }

    /**
     * 在指定位置生成空集符号效果
     * 现在使用实体来显示，而不是直接的粒子效果
     */
    private void spawnEmptySetParticleEffect(Level level, Vec3 position) {
        // 只有在服务器端才会生成实体
        if (!level.isClientSide) {
            // 创建空集符号实体
            FaizEmptySetEntity emptySetEntity = new FaizEmptySetEntity(level, null);
            emptySetEntity.setPos(position.x, position.y + 0.5, position.z); // 稍微抬高一点位置
            level.addFreshEntity(emptySetEntity);
        }
    }

    /**
     * 生成竖立的空集符号粒子效果
     * 符号在XZ平面显示（竖立），确保符号垂直于地面
     */
    private void spawnVerticalEmptySetParticles(ServerLevel level, DustParticleOptions particle, Vec3 position) {
        double centerX = position.x;
        double centerY = position.y + 2.0; // 抬得更高，更显眼
        double centerZ = position.z;

        // 1. 生成圆形部分 - 在XZ平面上的圆（立起来）
        int circleParticles = 50; // 增加粒子数量
        double radius = 1.2; // 增大半径

        for (int i = 0; i < circleParticles; i++) {
            double angle = 2 * Math.PI * i / circleParticles;
            // 在XZ平面上生成圆形，Y保持不变
            double x = centerX + radius * Math.cos(angle);
            double z = centerZ + radius * Math.sin(angle);
            double y = centerY;

            // 轻微随机偏移，保持在XZ平面
            double offsetX = (random.nextDouble() - 0.5) * 0.1;
            double offsetZ = (random.nextDouble() - 0.5) * 0.1;

            // 轻微向外扩散的运动，沿XZ平面
            double motionX = (x - centerX) * 0.08;
            double motionZ = (z - centerZ) * 0.08;
            double motionY = 0.02;

            // 发送粒子到客户端
            level.sendParticles(
                    particle,
                    x + offsetX, y, z + offsetZ,
                    1, // 每个位置1个粒子
                    motionX, motionY, motionZ,
                    0.05 // 速度
            );
        }

        // 2. 生成斜线部分 - 在XZ平面上的45度斜线（立起来）
        int lineParticles = 25;
        double lineLength = 1.8; // 加长斜线

        for (int i = 0; i < lineParticles; i++) {
            double progress = (double) i / (lineParticles - 1);
            double offset = (progress - 0.5) * lineLength;

            // 确保斜线在XZ平面上，形成45度角（从左上到右下）
            double x = centerX + offset * Math.cos(Math.PI / 4); // 45度在XZ平面
            double z = centerZ + offset * Math.sin(Math.PI / 4); // 45度在XZ平面
            double y = centerY;

            // 轻微随机运动，保持在XZ平面
            double motionX = (random.nextDouble() - 0.5) * 0.03;
            double motionZ = (random.nextDouble() - 0.5) * 0.03;
            double motionY = 0.03 + random.nextDouble() * 0.03;

            // 发送粒子到客户端
            level.sendParticles(
                    particle,
                    x, y, z,
                    1,
                    motionX, motionY, motionZ,
                    0.05
            );
        }

        // 3. 添加中心闪烁效果 - 更多粒子
        for (int i = 0; i < 20; i++) {
            double sparkX = centerX + (random.nextDouble() - 0.5) * 0.5;
            double sparkY = centerY + (random.nextDouble() - 0.5) * 0.5;
            double sparkZ = centerZ + (random.nextDouble() - 0.5) * 0.5;

            double motionX = (random.nextDouble() - 0.5) * 0.1;
            double motionY = (random.nextDouble() - 0.5) * 0.1;
            double motionZ = (random.nextDouble() - 0.5) * 0.1;

            // 发送粒子到客户端
            level.sendParticles(
                    particle,
                    sparkX, sparkY, sparkZ,
                    1,
                    motionX, motionY, motionZ,
                    0.05
            );
        }

        // 4. 添加脉冲效果 - 向外扩散的圆环
        for (int ring = 0; ring < 3; ring++) {
            double ringRadius = 0.5 + ring * 0.4;
            for (int i = 0; i < 20; i++) {
                double angle = 2 * Math.PI * i / 20;
                double x = centerX + ringRadius * Math.cos(angle);
                double z = centerZ + ringRadius * Math.sin(angle);
                double y = centerY;

                // 向外扩散的运动
                double motionX = (x - centerX) * 0.15;
                double motionZ = (z - centerZ) * 0.15;
                double motionY = 0.05;

                level.sendParticles(
                        particle,
                        x, y, z,
                        1,
                        motionX, motionY, motionZ,
                        0.05
                );
            }
        }
    }

    /**
     * 客户端版本 - 生成竖立的空集符号粒子效果
     * 符号在XZ平面显示（竖立），确保符号垂直于地面
     */
    private void spawnVerticalEmptySetParticles(Level level, DustParticleOptions particle, Vec3 position) {
        double centerX = position.x;
        double centerY = position.y + 2.0;
        double centerZ = position.z;

        // 1. 生成圆形部分 - 在XZ平面上的圆（立起来）
        int circleParticles = 50;
        double radius = 1.2;

        for (int i = 0; i < circleParticles; i++) {
            double angle = 2 * Math.PI * i / circleParticles;
            // 在XZ平面上生成圆形，Y保持不变
            double x = centerX + radius * Math.cos(angle);
            double z = centerZ + radius * Math.sin(angle);
            double y = centerY;

            // 轻微随机偏移，保持在XZ平面
            double offsetX = (random.nextDouble() - 0.5) * 0.1;
            double offsetZ = (random.nextDouble() - 0.5) * 0.1;

            // 轻微向外扩散的运动，沿XZ平面
            double motionX = (x - centerX) * 0.08;
            double motionZ = (z - centerZ) * 0.08;
            double motionY = 0.02;

            level.addParticle(particle, x + offsetX, y, z + offsetZ, motionX, motionY, motionZ);
        }

        // 2. 生成斜线部分 - 在XZ平面上的45度斜线（立起来）
        int lineParticles = 25;
        double lineLength = 1.8;

        for (int i = 0; i < lineParticles; i++) {
            double progress = (double) i / (lineParticles - 1);
            double offset = (progress - 0.5) * lineLength;

            // 确保斜线在XZ平面上，形成45度角（从左上到右下）
            double x = centerX + offset * Math.cos(Math.PI / 4); // 45度在XZ平面
            double z = centerZ + offset * Math.sin(Math.PI / 4); // 45度在XZ平面
            double y = centerY;

            // 轻微随机运动，保持在XZ平面
            double motionX = (random.nextDouble() - 0.5) * 0.03;
            double motionZ = (random.nextDouble() - 0.5) * 0.03;
            double motionY = 0.03 + random.nextDouble() * 0.03;

            level.addParticle(particle, x, y, z, motionX, motionY, motionZ);
        }

        // 3. 添加中心闪烁效果
        for (int i = 0; i < 20; i++) {
            double sparkX = centerX + (random.nextDouble() - 0.5) * 0.5;
            double sparkY = centerY + (random.nextDouble() - 0.5) * 0.5;
            double sparkZ = centerZ + (random.nextDouble() - 0.5) * 0.5;

            double motionX = (random.nextDouble() - 0.5) * 0.1;
            double motionY = (random.nextDouble() - 0.5) * 0.1;
            double motionZ = (random.nextDouble() - 0.5) * 0.1;

            level.addParticle(particle, sparkX, sparkY, sparkZ, motionX, motionY, motionZ);
        }

        // 4. 添加脉冲效果
        for (int ring = 0; ring < 3; ring++) {
            double ringRadius = 0.5 + ring * 0.4;
            for (int i = 0; i < 20; i++) {
                double angle = 2 * Math.PI * i / 20;
                double x = centerX + ringRadius * Math.cos(angle);
                double z = centerZ + ringRadius * Math.sin(angle);
                double y = centerY;

                double motionX = (x - centerX) * 0.15;
                double motionZ = (z - centerZ) * 0.15;
                double motionY = 0.05;

                level.addParticle(particle, x, y, z, motionX, motionY, motionZ);
            }
        }
    }

    /**
     * 生成攻击时的粒子效果
     */
    private void spawnAttackParticles(Level level, Player player, Vec3 direction) {
        // 生成红色轨迹粒子
        for (int i = 0; i < 25; i++) {
            double progress = (double)i / 25;
            Vec3 pos = player.getEyePosition(1.0f).add(direction.scale(progress * 5.0));

            double offsetX = (random.nextDouble() - 0.5) * 0.3;
            double offsetY = (random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (random.nextDouble() - 0.5) * 0.3;

            double motionX = direction.x * 0.2 + (random.nextDouble() - 0.5) * 0.1;
            double motionY = direction.y * 0.2 + (random.nextDouble() - 0.5) * 0.1;
            double motionZ = direction.z * 0.2 + (random.nextDouble() - 0.5) * 0.1;

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        RED_DUST,
                        pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                        1,
                        motionX, motionY, motionZ,
                        0.05
                );
            } else if (level.isClientSide) {
                level.addParticle(RED_DUST,
                        pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                        motionX, motionY, motionZ);
            }
        }
    }

    @Override
    public String getRiderName() {
        return "Faiz";
    }

    @Override
    public String getActivationSoundName() {
        return "Axel Form!";
    }

    @Override
    public float getAttackDamage() {
        return 48.0f;
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }

    /**
     * 为非玩家实体执行特殊攻击效果
     */
    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 播放音效
            com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager.playSelectionSound(level, shooter, getRiderName());
            com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager.playAttackSound(level, shooter, getRiderName());

            Vec3 attackDirection = direction != null && direction.lengthSqr() > 0 ?
                    direction.normalize() : shooter.getLookAngle();

            float attackRange = getEffectRange();
            float width = attackRange / 2;

            Vec3 start = shooter.position().add(0, shooter.getEyeHeight() * 0.5, 0);
            Vec3 end = start.add(attackDirection.scale(attackRange));

            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(
                    Math.min(start.x, end.x) - width,
                    Math.min(start.y, end.y) - 1,
                    Math.min(start.z, end.z) - width,
                    Math.max(start.x, end.x) + width,
                    Math.max(start.y, end.y) + 1,
                    Math.max(start.z, end.z) + width
            );

            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, attackBox,
                    entity -> entity != shooter && entity != null && entity.isAlive())) {
                Vec3 targetRelative = target.position().subtract(start);
                if (targetRelative.dot(attackDirection) > 0) {
                    float healthBefore = target.getHealth();

                    for (int i = 0; i < 3; i++) {
                        DamageSource damageSource = level.damageSources().mobAttack(shooter);
                        target.hurt(damageSource, getAttackDamage() * 0.4f);

                        if (!target.isAlive()) {
                            break;
                        }
                    }

                    if (!target.isAlive()) {
                        spawnEmptySetParticleEffect(level, target.position());
                    }
                }
            }
        }
    }
}