package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.build.BuildRiderEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Kamen Rider Build 骑士特效实现
 * 气泡兔坦形态的公式踢特效
 */
public class BuildEffect implements HeiseiRiderEffect {
    // Build骑士特有的常量 - 气泡兔坦形态
    private static final String RIDER_NAME = "Build";
    private static final String ACTIVATION_SOUND = "activation_build";
    private static final float BASE_DAMAGE = 15.0f;
    private static final float ATTACK_MULTIPLIER = 1.2f;
    private static final float EFFECT_RANGE = 5.0f;
    private static final double ENERGY_COST = 20.0;

    @Override
    public void executeSpecialAttack(World world, PlayerEntity player, Vec3d direction) {
        if (world.isClient()) return;

        // 生成Build骑士实体而不是直接伤害
        try {
            if (world instanceof ServerWorld serverWorld) {
                // 规范化方向向量
                Vec3d normalizedDirection = direction.normalize();
                
                // 创建Build骑士实体
                BuildRiderEntity buildEntity = new BuildRiderEntity(
                    world,
                    player,
                    normalizedDirection,
                    getAttackDamage()
                );
                
                // 在世界中生成实体
                world.spawnEntity(buildEntity);
                
                // 创建额外的视觉效果
                createVisualEffects(world, player);
            }
        } catch (Exception e) {
            // 防止崩溃，记录错误但不中断游戏
            System.err.println("创建Build骑士实体失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getRiderName() {
        return RIDER_NAME;
    }

    @Override
    public String getActivationSoundName() {
        return ACTIVATION_SOUND;
    }

    @Override
    public float getAttackDamage() {
        return BASE_DAMAGE * ATTACK_MULTIPLIER;
    }

    @Override
    public float getEffectRange() {
        return EFFECT_RANGE;
    }

    @Override
    public double getEnergyCost() {
        return ENERGY_COST;
    }

    /**
     * 创建视觉效果
     */
    private void createVisualEffects(World world, PlayerEntity player) {
        if (world.isClient()) return;

        // 在用户周围生成气泡兔坦相关的粒子效果
        for (int i = 0; i < 15; i++) {
            double offsetX = (world.random.nextDouble() - 0.5) * 2.0;
            double offsetY = world.random.nextDouble() * 1.5;
            double offsetZ = (world.random.nextDouble() - 0.5) * 2.0;

            // 使用getX、getY、getZ方法代替getPos()
            double x = player.getX() + offsetX;
            double y = player.getY() + offsetY;
            double z = player.getZ() + offsetZ;

            // 在服务器端生成粒子
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld) world;
                // 交替生成气泡和心形粒子（代表气泡和兔子）
                if (i % 2 == 0) {
                    // 气泡粒子效果
                    serverWorld.spawnParticles(
                            ParticleTypes.BUBBLE,
                            x, y, z,
                            1, 0, 0, 0, 0.1
                    );
                } else {
                    // 心形粒子效果
                    serverWorld.spawnParticles(
                            ParticleTypes.HEART,
                            x, y, z,
                            1, 0, 0, 0, 0.1
                    );
                }
            }
        }
    }
}