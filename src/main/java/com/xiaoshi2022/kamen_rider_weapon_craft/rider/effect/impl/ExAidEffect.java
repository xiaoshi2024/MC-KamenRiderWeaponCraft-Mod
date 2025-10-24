package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid.ExAidSlashEffectEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExAidEffect extends AbstractHeiseiRiderEffect {

    // 存储需要受到持续伤害的实体和对应的剩余时间（游戏刻）
    private static final Map<LivingEntity, Integer> DAMAGE_TARGETS = new HashMap<>();
    private static final int DURATION = 60;
    private static final int DAMAGE_INTERVAL = 10;

    // 存储延迟生成的特效信息
    private static final Map<Integer, DelayedEffectInfo> DELAYED_EFFECTS = new HashMap<>();
    private static int nextEffectId = 0;

    public ExAidEffect() {
        // 注册服务器tick事件
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            onServerTick();
        });
    }

    @Override
    public void executeSpecialAttack(World world, PlayerEntity player, Vec3d direction) {
        // 统一在服务器端处理攻击逻辑和特效生成，确保同步一致性
        if (!world.isClient()) {
            // 为玩家添加增益效果
            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED, 400, 1));

            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.STRENGTH, 400, 1));

            player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.REGENERATION, 200, 2));

            // 1. 对前方敌人造成伤害
            double reach = 8.0;
            Vec3d start = player.getEyePos();
            Vec3d end = start.add(direction.multiply(reach));

            // 使用改进的实体检测方法
            Entity hitEntity = getTargetEntity(world, player, start, end, reach);

            if (hitEntity instanceof LivingEntity && hitEntity != player) {
                LivingEntity livingEntity = (LivingEntity) hitEntity;

                // 降低单次伤害，改为短时间持续伤害
                float initialDamage = getAttackDamage() * 0.5f;

                // 确保伤害能够造成，即使实体有一定的抗性
                // 再次确认不是释放者本人，防止误判造成自伤
                if (livingEntity != player) {
                    boolean hurt = livingEntity.damage(
                            (ServerWorld)world, world.getDamageSources().playerAttack(player), initialDamage);
                }

                // 无论是否成功造成伤害，都添加到持续伤害列表
                addToDamageTargets(livingEntity, player);

                // 生成特效，确保玩家有视觉反馈
                spawnSlashEffectOnEntity(hitEntity, world, player, direction);

                // 立即生成一个初始特效
                ExAidSlashEffectEntity.spawnEffectOnTarget(world, player, hitEntity);
            } else {
                // 即使没有击中实体，也生成基础特效
                ExAidSlashEffectEntity.spawnEffect(world, player, direction);
            }
        }
        // 客户端只生成预览特效
        else {
            // 使用备用方法生成客户端特效
            ExAidSlashEffectEntity.spawnEffect(world, player, direction);
        }
    }

    // 添加实体到持续伤害列表
    private void addToDamageTargets(LivingEntity target, PlayerEntity player) {
        // 确保不会将释放者添加到持续伤害列表
        if (target != player) {
            DAMAGE_TARGETS.put(target, DURATION);
        }
    }

    // 服务器tick处理
    private static void onServerTick() {
        // 处理持续伤害
        handleDamageTargets();

        // 处理延迟特效生成
        handleDelayedEffects();
    }

    // 处理持续伤害目标
    private static void handleDamageTargets() {
        Iterator<Map.Entry<LivingEntity, Integer>> damageIterator = DAMAGE_TARGETS.entrySet().iterator();
        while (damageIterator.hasNext()) {
            Map.Entry<LivingEntity, Integer> entry = damageIterator.next();
            LivingEntity target = entry.getKey();
            int remainingTicks = entry.getValue() - 1;

            // 如果目标已经死亡或不再有效，从列表中移除
            // 允许对其他玩家（包括敌对玩家）造成持续伤害，但确保不会伤害释放者
            if (target == null || !target.isAlive() || target.isRemoved()) {
                damageIterator.remove();
                continue;
            }

            // 在指定间隔造成伤害
            if (remainingTicks % DAMAGE_INTERVAL == 0) {
                float dotDamage = 5.0f;
                target.damage(
                            (ServerWorld)target.getWorld(), target.getWorld().getDamageSources().magic(), dotDamage);

                // 在持续伤害期间生成小型特效
                spawnDotEffect(target);
            }

            // 更新剩余时间或移除目标
            if (remainingTicks <= 0) {
                damageIterator.remove();
            } else {
                DAMAGE_TARGETS.put(target, remainingTicks);
            }
        }
    }

    // 处理延迟特效
    private static void handleDelayedEffects() {
        Iterator<Map.Entry<Integer, DelayedEffectInfo>> effectIterator = DELAYED_EFFECTS.entrySet().iterator();
        while (effectIterator.hasNext()) {
            Map.Entry<Integer, DelayedEffectInfo> entry = effectIterator.next();
            DelayedEffectInfo info = entry.getValue();

            // 检查目标实体是否仍然有效
            if (info.targetEntity == null || !info.targetEntity.isAlive() || info.targetEntity.isRemoved()) {
                effectIterator.remove();
                continue;
            }

            // 减少剩余延迟时间
            info.remainingTicks--;

            // 如果到了生成时机且还有特效未生成
            if (info.remainingTicks <= 0 && info.spawnedEffects < info.totalEffects) {
                // 生成一个特效实体
                spawnSingleDelayedEffect(info);

                // 设置下一个特效的延迟时间
                info.remainingTicks = 3;
            }

            // 如果所有特效都已生成，移除该条目
            if (info.spawnedEffects >= info.totalEffects) {
                effectIterator.remove();
            }
        }
    }

    // 生成持续伤害的小型特效
    private static void spawnDotEffect(LivingEntity target) {
        World world = target.getWorld();
        if (!world.isClient() && target != null) {
            double x = target.getX() + (world.random.nextDouble() - 0.5) * 0.8;
            double y = target.getY() + target.getHeight() * 0.5 + (world.random.nextDouble() - 0.5) * 0.5;
            double z = target.getZ() + (world.random.nextDouble() - 0.5) * 0.8;

            ExAidSlashEffectEntity effect = new ExAidSlashEffectEntity(
                                ModEntityTypes.EXAID_SLASH_EFFECT,
                                world
                        );

            effect.setPosition(x, y, z);
            effect.setYaw(world.random.nextFloat() * 360.0f);
            effect.setPitch(world.random.nextFloat() * 360.0f);
            effect.setNoGravity(true);
            effect.setInvulnerable(true);

            // 重要：不要设置owner，防止这些小特效造成误伤
            // 注意：直接不设置owner，而不是设置为null

            world.spawnEntity(effect);
        }
    }

    // 改进的实体检测方法
    private Entity getTargetEntity(World world, PlayerEntity player, Vec3d start, Vec3d end, double reach) {
        // 方法1：创建一个较大的碰撞盒来检测实体
        Box box = new Box(
                Math.min(start.x, end.x) - 1.0,
                Math.min(start.y, end.y) - 1.0,
                Math.min(start.z, end.z) - 1.0,
                Math.max(start.x, end.x) + 1.0,
                Math.max(start.y, end.y) + 1.0,
                Math.max(start.z, end.z) + 1.0
        );

        // 获取碰撞盒内的所有实体
        List<Entity> entities = world.getEntitiesByClass(Entity.class, box,
                entity -> entity instanceof LivingEntity &&
                        !entity.isSpectator() &&
                        entity.isAlive() &&
                        entity != player);

        // 找出最接近视线的实体
        double closestDistance = Double.MAX_VALUE;
        Entity closestEntity = null;

        for (Entity entity : entities) {
            // 计算实体与视线的最近点
            Vec3d closestPoint = entity.getBoundingBox().raycast(start, end).orElse(null);
            if (closestPoint != null) {
                double distance = start.distanceTo(closestPoint);
                if (distance <= reach && distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }

        // 如果没有找到实体，使用原来的pick方法作为备用
        if (closestEntity == null) {
            net.minecraft.util.hit.HitResult result = player.raycast(reach, 0.0f, false);
            if (result instanceof net.minecraft.util.hit.EntityHitResult entityHitResult) {
                return entityHitResult.getEntity();
            }
        }

        return closestEntity;
    }

    // 延迟生成特效的信息类
    private static class DelayedEffectInfo {
        Entity targetEntity;
        World world;
        PlayerEntity owner;
        Vec3d direction;
        int remainingTicks;
        int totalEffects;
        int spawnedEffects;

        DelayedEffectInfo(Entity targetEntity, World world, PlayerEntity owner, Vec3d direction, int delayTicks, int totalEffects) {
            this.targetEntity = targetEntity;
            this.world = world;
            this.owner = owner;
            this.direction = direction;
            this.remainingTicks = delayTicks;
            this.totalEffects = totalEffects;
            this.spawnedEffects = 0;
        }
    }

    // 生成单个延迟特效
    private static void spawnSingleDelayedEffect(DelayedEffectInfo info) {
        if (!info.world.isClient() && info.targetEntity != null && info.targetEntity.isAlive()) {
            // 获取实体的当前位置
            double x = info.targetEntity.getX();
            double y = info.targetEntity.getY() + info.targetEntity.getHeight() * 0.5;
            double z = info.targetEntity.getZ();

            Vec3d lookVector = info.direction.normalize();

            // 基于实体形状生成更自然的偏移
            double radius = Math.max(info.targetEntity.getWidth(), info.targetEntity.getHeight()) * 0.6;
            double angle = info.world.random.nextDouble() * Math.PI * 2;
            double heightVariation = info.world.random.nextDouble() * 0.8 - 0.4;

            double offsetX = Math.cos(angle) * radius * (info.world.random.nextDouble() * 0.7 + 0.3);
            double offsetY = heightVariation * info.targetEntity.getHeight() * 0.5 + 0.2;
            double offsetZ = Math.sin(angle) * radius * (info.world.random.nextDouble() * 0.7 + 0.3);

            // 创建特效实体
            ExAidSlashEffectEntity effect = new ExAidSlashEffectEntity(
                    ModEntityTypes.EXAID_SLASH_EFFECT,
                    info.world
            );

            // 设置特效位置，添加随机偏移
            effect.setPosition(x + offsetX, y + offsetY, z + offsetZ);

            // 设置随机旋转角度
            float yRot = (float)Math.toDegrees(Math.atan2(-lookVector.x, lookVector.z)) +
                    (info.world.random.nextFloat() - 0.5f) * 180;
            float xRot = (float)Math.toDegrees(Math.atan2(lookVector.y, Math.sqrt(lookVector.x * lookVector.x + lookVector.z * lookVector.z))) +
                    (info.world.random.nextFloat() - 0.5f) * 90;
            effect.setYaw(yRot);
            effect.setPitch(xRot);

            // 设置特效属性
            effect.setNoGravity(true);
            effect.setInvulnerable(true);

            // 重要：设置owner为当前玩家
            effect.setOwner(info.owner);

            // 设置特效的目标实体，确保特效跟踪目标而不是释放者
            effect.setTargetEntity(info.targetEntity);

            // 添加实体到世界中
            info.world.spawnEntity(effect);
            info.spawnedEffects++;

            // 在生成特效时直接对目标实体造成伤害
            if (info.targetEntity instanceof LivingEntity && info.targetEntity != info.owner) {
                LivingEntity targetLiving = (LivingEntity) info.targetEntity;
                // 确保在服务器端才应用伤害
                if (!info.world.isClient()) {
                    // 对目标造成伤害
                    float damage = 8.0f;
                    targetLiving.damage(
                            (ServerWorld)info.world, info.world.getDamageSources().playerAttack(info.owner), damage);
                }
            }
        }
    }

    // 改进的特效生成方法
    private void spawnSlashEffectOnEntity(Entity entity, World world, PlayerEntity player, Vec3d direction) {
        // 只在服务器端生成，确保所有客户端都能看到
        if (!world.isClient() && entity != null) {
            // 添加延迟生成特效的信息
            DELAYED_EFFECTS.put(nextEffectId++,
                    new DelayedEffectInfo(entity, world, player, direction, 0, 7)
            );

            // 立即播放音效，给玩家即时反馈
            playHitSound(world, entity.getX(), entity.getY() + entity.getHeight() * 0.5, entity.getZ());
        }
    }

    // 播放击中音效
    private void playHitSound(World world, double x, double y, double z) {
        // 在服务器端使用广播音效，确保所有附近玩家都能听到
        float volume = 0.8F + world.random.nextFloat() * 0.2F;
        float pitch = 0.9F + world.random.nextFloat() * 0.2F;

        // 播放多个音效来增强反馈
        world.playSound(null, x, y, z,
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                net.minecraft.sound.SoundCategory.PLAYERS,
                volume, pitch);

        // 添加额外的电子音效，增强Ex-Aid的游戏风格
        world.playSound(null, x, y, z,
                SoundEvents.ENTITY_BLAZE_SHOOT,
                net.minecraft.sound.SoundCategory.PLAYERS,
                volume * 0.7F, pitch * 1.2F);
    }

    @Override
    public String getRiderName() {
        return "Ex-Aid";
    }

    @Override
    public String getActivationSoundName() {
        return "Critical Strike!";
    }

    @Override
    public float getAttackDamage() {
        return 52.0f;
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
}