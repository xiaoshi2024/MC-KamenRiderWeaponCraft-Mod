package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid.ExAidSlashEffectEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class ExAidEffect extends AbstractHeiseiRiderEffect {

    // 存储需要受到持续伤害的实体和对应的剩余时间（游戏刻）
    private static final Map<LivingEntity, Integer> DAMAGE_TARGETS = new HashMap<>();
    private static final int DURATION = 60;
    private static final int DAMAGE_INTERVAL = 10;

    // 存储延迟生成的特效信息
    private static final Map<Integer, DelayedEffectInfo> DELAYED_EFFECTS = new HashMap<>();
    private static int nextEffectId = 0;

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        // 统一在服务器端处理攻击逻辑和特效生成，确保同步一致性
        if (!level.isClientSide) {
            // 为玩家添加增益效果
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 400, 1));

            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 400, 1));

            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.REGENERATION, 200, 2));

            // 1. 对前方敌人造成伤害
            double reach = 8.0;
            Vec3 start = player.getEyePosition(1.0f);
            Vec3 end = start.add(direction.scale(reach));

            // 使用改进的实体检测方法
            Entity hitEntity = getTargetEntity(level, player, start, end, reach);

            if (hitEntity instanceof LivingEntity && hitEntity != player) {
                LivingEntity livingEntity = (LivingEntity) hitEntity;

                // 降低单次伤害，改为短时间持续伤害
                float initialDamage = getAttackDamage() * 0.5f;

                // 确保伤害能够造成，即使实体有一定的抗性
                // 再次确认不是释放者本人，防止误判造成自伤
                if (livingEntity != player) {
                    boolean hurt = livingEntity.hurt(
                            level.damageSources().playerAttack(player), initialDamage);
                }

                // 无论是否成功造成伤害，都添加到持续伤害列表
                addToDamageTargets(livingEntity, player);

                // 生成特效，确保玩家有视觉反馈
                spawnSlashEffectOnEntity(hitEntity, level, player, direction);

                // 立即生成一个初始特效
                ExAidSlashEffectEntity.spawnEffectOnTarget(level, player, hitEntity);
            } else {
                // 即使没有击中实体，也生成基础特效
                ExAidSlashEffectEntity.spawnEffect(level, player, direction);
            }
        }
        // 客户端只生成预览特效
        else {
            // 确保有对应的客户端生成方法
            try {
                Class<?> effectClass = Class.forName("com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid.ExAidRiderEffect");
                java.lang.reflect.Method method = effectClass.getMethod("spawnSlashEffectByOwnerDirection", Level.class, Player.class);
                method.invoke(null, level, player);
            } catch (Exception e) {
                // 如果反射失败，使用备用方法
                ExAidSlashEffectEntity.spawnEffect(level, player, direction);
            }
        }
    }

    // 添加实体到持续伤害列表
    private void addToDamageTargets(LivingEntity target, Player player) {
        // 确保不会将释放者添加到持续伤害列表
        if (target != player) {
            DAMAGE_TARGETS.put(target, DURATION);
        }
    }

    // 修复：事件订阅器需要是静态的
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

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
                target.hurt(target.level().damageSources().magic(), dotDamage);

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
        Level level = target.level();
        if (!level.isClientSide && target != null) {
            double x = target.getX() + (level.random.nextDouble() - 0.5) * 0.8;
            double y = target.getY() + target.getBbHeight() * 0.5 + (level.random.nextDouble() - 0.5) * 0.5;
            double z = target.getZ() + (level.random.nextDouble() - 0.5) * 0.8;

            ExAidSlashEffectEntity effect = new ExAidSlashEffectEntity(
                    ModEntityTypes.EXAID_SLASH_EFFECT.get(),
                    level
            );

            effect.setPos(x, y, z);
            effect.setYRot(level.random.nextFloat() * 360.0f);
            effect.setXRot(level.random.nextFloat() * 360.0f);
            effect.noPhysics = true;
            effect.setInvulnerable(true);

            // 重要：不要设置owner，防止这些小特效造成误伤
            // 注意：直接不设置owner，而不是设置为null

            level.addFreshEntity(effect);
        }
    }

    // 改进的实体检测方法
    private Entity getTargetEntity(Level level, Player player, Vec3 start, Vec3 end, double reach) {
        // 方法1：创建一个较大的碰撞盒来检测实体
        AABB aabb = new AABB(
                Math.min(start.x, end.x) - 1.0,
                Math.min(start.y, end.y) - 1.0,
                Math.min(start.z, end.z) - 1.0,
                Math.max(start.x, end.x) + 1.0,
                Math.max(start.y, end.y) + 1.0,
                Math.max(start.z, end.z) + 1.0
        );

        // 获取碰撞盒内的所有实体
        List<Entity> entities = level.getEntities(player, aabb,
                entity -> entity instanceof LivingEntity &&
                        !entity.isSpectator() &&
                        entity.isAlive() &&
                        entity != player);

        // 找出最接近视线的实体
        double closestDistance = Double.MAX_VALUE;
        Entity closestEntity = null;

        for (Entity entity : entities) {
            // 计算实体与视线的最近点
            Vec3 closestPoint = entity.getBoundingBox().clip(start, end).orElse(null);
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
            net.minecraft.world.phys.HitResult result = player.pick(reach, 0.0f, false);
            if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
                return entityHitResult.getEntity();
            }
        }

        return closestEntity;
    }

    // 延迟生成特效的信息类
    private static class DelayedEffectInfo {
        Entity targetEntity;
        Level level;
        Player owner;
        Vec3 direction;
        int remainingTicks;
        int totalEffects;
        int spawnedEffects;

        DelayedEffectInfo(Entity targetEntity, Level level, Player owner, Vec3 direction, int delayTicks, int totalEffects) {
            this.targetEntity = targetEntity;
            this.level = level;
            this.owner = owner;
            this.direction = direction;
            this.remainingTicks = delayTicks;
            this.totalEffects = totalEffects;
            this.spawnedEffects = 0;
        }
    }

    // 生成单个延迟特效
    private static void spawnSingleDelayedEffect(DelayedEffectInfo info) {
        if (!info.level.isClientSide && info.targetEntity != null && info.targetEntity.isAlive()) {
            // 获取实体的当前位置
            double x = info.targetEntity.getX();
            double y = info.targetEntity.getY() + info.targetEntity.getBbHeight() * 0.5;
            double z = info.targetEntity.getZ();

            Vec3 lookVector = info.direction.normalize();

            // 基于实体形状生成更自然的偏移
            double radius = Math.max(info.targetEntity.getBbWidth(), info.targetEntity.getBbHeight()) * 0.6;
            double angle = info.level.random.nextDouble() * Math.PI * 2;
            double heightVariation = info.level.random.nextDouble() * 0.8 - 0.4;

            double offsetX = Math.cos(angle) * radius * (info.level.random.nextDouble() * 0.7 + 0.3);
            double offsetY = heightVariation * info.targetEntity.getBbHeight() * 0.5 + 0.2;
            double offsetZ = Math.sin(angle) * radius * (info.level.random.nextDouble() * 0.7 + 0.3);

            // 创建特效实体
            ExAidSlashEffectEntity effect = new ExAidSlashEffectEntity(
                    ModEntityTypes.EXAID_SLASH_EFFECT.get(),
                    info.level
            );

            // 设置特效位置，添加随机偏移
            effect.setPos(x + offsetX, y + offsetY, z + offsetZ);

            // 设置随机旋转角度
            float yRot = (float)Math.toDegrees(Math.atan2(-lookVector.x, lookVector.z)) +
                    (info.level.random.nextFloat() - 0.5f) * 180;
            float xRot = (float)Math.toDegrees(Math.atan2(lookVector.y, Math.sqrt(lookVector.x * lookVector.x + lookVector.z * lookVector.z))) +
                    (info.level.random.nextFloat() - 0.5f) * 90;
            effect.setYRot(yRot);
            effect.setXRot(xRot);

            // 设置特效属性
            effect.noPhysics = true;
            effect.setInvulnerable(true);

            // 重要：设置owner为当前玩家
            effect.setOwner(info.owner);

            // 设置特效的目标实体，确保特效跟踪目标而不是释放者
            effect.setTargetEntity(info.targetEntity);

            // 添加实体到世界中
            info.level.addFreshEntity(effect);
            info.spawnedEffects++;

            // 在生成特效时直接对目标实体造成伤害
            if (info.targetEntity instanceof LivingEntity && info.targetEntity != info.owner) {
                LivingEntity targetLiving = (LivingEntity) info.targetEntity;
                // 确保在服务器端才应用伤害
                if (!info.level.isClientSide) {
                    // 对目标造成伤害
                    float damage = 8.0f;
                    targetLiving.hurt(info.level.damageSources().playerAttack(info.owner), damage);
                }
            }
        }
    }

    // 改进的特效生成方法
    private void spawnSlashEffectOnEntity(Entity entity, Level level, Player player, Vec3 direction) {
        // 只在服务器端生成，确保所有客户端都能看到
        if (!level.isClientSide && entity != null) {
            // 添加延迟生成特效的信息
            DELAYED_EFFECTS.put(nextEffectId++,
                    new DelayedEffectInfo(entity, level, player, direction, 0, 7)
            );

            // 立即播放音效，给玩家即时反馈
            playHitSound(level, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ());
        }
    }

    // 播放击中音效
    private void playHitSound(Level level, double x, double y, double z) {
        // 在服务器端使用广播音效，确保所有附近玩家都能听到
        float volume = 0.8F + level.random.nextFloat() * 0.2F;
        float pitch = 0.9F + level.random.nextFloat() * 0.2F;

        // 播放多个音效来增强反馈
        level.playSound(null, x, y, z,
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                volume, pitch);

        // 添加额外的电子音效，增强Ex-Aid的游戏风格
        level.playSound(null, x, y, z,
                net.minecraft.sounds.SoundEvents.BLAZE_SHOOT,
                net.minecraft.sounds.SoundSource.PLAYERS,
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