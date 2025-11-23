package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KabutoEffect extends AbstractHeiseiRiderEffect {
    
    // 存储被减速的TNT实体和箭矢，用于恢复其正常状态
    private static final Map<UUID, Integer> slowedTntEntities = new HashMap<>();
    
    // 时缓倍率 - 0.0表示完全停止，用于箭矢等投射物
    private static final float PROJECTILE_STOP_FACTOR = 0.0f;
    // 时缓倍率 - 0.3表示速度变为原来的30%，用于其他需要减速的实体
    private static final float TIME_SLOW_FACTOR = 0.3f;
    
    // 时缓效果持续时间（游戏刻）
    public static final int TIME_SLOW_DURATION = 200; // 10秒

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：同时激活Clock Up和Clock Down能力，还原剧中设定
            // 1. Clock Up：给予玩家极高的速度和抗性效果
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 5));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2));
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 200, 2)); // 添加跳跃增强
            
            // 移除视觉效果，简化实现
            
            // 2. Clock Down：全面时缓效果 - 只给周围实体和环境造成缓慢，不直接造成伤害
            activateFullClockDown(level, player);
            
            // 3. 向前方冲刺
            Vec3 velocity = direction.scale(4.0);
            player.setDeltaMovement(velocity);
            
            // 4. 播放特殊音效提示
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.PISTON_EXTEND, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    /**
     * 激活完整的Clock Down能力，包括生物减速、弹射物减速和TNT爆炸延迟
     */
    private void activateFullClockDown(Level level, Player player) {
        double effectRange = 10.0; // 从玩家为中心，半径10格范围内发动时缓效果
        
        // 1. 对周围所有生物施加减速效果
        slowLivingEntities(level, player, effectRange);
        
        // 2. 对周围所有弹射物减速
        slowProjectiles(level, player, effectRange);
        
        // 3. 延迟周围所有TNT的爆炸
        delayTntExplosions(level, player, effectRange);
        
        // 4. 停止周围的雨滴粒子
        stopRainParticles(level, player, effectRange);
        
        // 5. 设置定时任务，在时缓效果结束后恢复所有被冻结的箭矢
        scheduleEffectReset(level);
    }
    
    /**
     * 减速周围的生物，但同为甲斗王模式的玩家互相无视时缓效果
     * 使用玩家身上的效果组合来识别甲斗王模式的玩家
     */
    private void slowLivingEntities(Level level, Player player, double range) {
        level.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(range),
                entity -> {
                    // 过滤自己，且只对活着的实体生效
                    if (entity != player && entity.isAlive()) {
                        // 如果是玩家实体，检查是否也是甲斗王模式
                        if (entity instanceof Player targetPlayer) {
                            // 同为甲斗王模式的玩家互相无视时缓效果
                            return !isPlayerInKabutoMode(targetPlayer);
                        }
                        // 非玩家实体正常减速
                        return true;
                    }
                    return false;
                })
            .forEach(entity -> {
                // 施加强力减速效果
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4));
                
                // 检查是否是弓箭手类型实体（骷髅射手等）
                boolean isArcher = entity instanceof net.minecraft.world.entity.monster.AbstractSkeleton || 
                                  entity instanceof net.minecraft.world.entity.monster.Skeleton ||
                                  entity instanceof net.minecraft.world.entity.monster.Stray ||
                                  entity instanceof net.minecraft.world.entity.monster.WitherSkeleton ||
                                  (entity instanceof Player && ((Player)entity).getMainHandItem().getItem() instanceof net.minecraft.world.item.BowItem);
                
                if (isArcher) {
                    // 对于弓箭手，给予最高级别的挖掘/攻击速度减慢效果，使其拉弓极慢
                    entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 300, 4));
                } else {
                    // 其他实体使用普通的减慢效果
                    entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 4));
                }
                
                // 添加缓慢下落效果，进一步限制敌人的行动能力
                entity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 1));
                
                // 添加视觉反馈：发光效果
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 150, 0));
            });
    }
    
    /**
     * 检查玩家是否处于甲斗王模式
     * 通过检查玩家身上的效果组合来识别甲斗王模式（高速移动+抗性）
     */
    private boolean isPlayerInKabutoMode(Player player) {
        // 检查玩家是否同时拥有高速移动和抗性效果，这是甲斗王模式的特征
        return player.hasEffect(MobEffects.MOVEMENT_SPEED) && 
               player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() >= 4 && 
               player.hasEffect(MobEffects.DAMAGE_RESISTANCE);
    }
    
    /**
     * 停止周围的箭矢和其他弹射物，让它们停留在半空中
     */
    private void slowProjectiles(Level level, Player player, double range) {
        level.getEntitiesOfClass(Projectile.class, 
                player.getBoundingBox().inflate(range))
            .forEach(projectile -> {
                // 保存原始速度向量，以便时缓解除后恢复
                // 使用一个特殊的键值来存储速度信息，这里使用负数来表示这是速度数据
                // 由于我们不能直接存储Vec3，我们将用一个简单的标记值表示这是一个被时缓的投射物
                if (!slowedTntEntities.containsKey(projectile.getUUID())) {
                    // 保存一个标记值，表示这是一个被时缓的投射物
                    slowedTntEntities.put(projectile.getUUID(), -1);
                }
                
                // 将投射物速度设置为0，使其完全停止在空中
                projectile.setDeltaMovement(Vec3.ZERO);
                
                // 对于箭矢，确保额外的处理使其完全停留在半空
                if (projectile instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow) {
                    // 临时设置伤害为0，防止伤害计算
                    arrow.setBaseDamage(0.0);
                    
                    // 确保箭矢完全停止 - 覆盖掉可能存在的其他速度设置
                    arrow.setDeltaMovement(Vec3.ZERO);
                    
                    // 确保重力不影响箭矢
                    arrow.setNoGravity(true);
                    
                    // 停止箭矢的所有物理效果
                    if (arrow instanceof net.minecraft.world.entity.projectile.Arrow) {
                        net.minecraft.world.entity.projectile.Arrow minecraftArrow = (net.minecraft.world.entity.projectile.Arrow)arrow;
                        minecraftArrow.setNoPhysics(true);
                    }
                }
            });
    }
    
    /**
     * 延迟周围TNT的爆炸
     */
    private void delayTntExplosions(Level level, Player player, double range) {
        level.getEntitiesOfClass(PrimedTnt.class, 
                player.getBoundingBox().inflate(range))
            .forEach(tnt -> {
                // 记录原始 fuse 值，用于后续可能的恢复
                if (!slowedTntEntities.containsKey(tnt.getUUID())) {
                    slowedTntEntities.put(tnt.getUUID(), tnt.getFuse());
                }
                
                // 增加TNT的 fuse 值，延长爆炸时间（相当于减慢爆炸过程）
                int currentFuse = tnt.getFuse();
                if (currentFuse > 0) {
                    // 每次触发时增加fuse值，模拟时间变慢
                    tnt.setFuse(currentFuse + 10); // 额外增加10tick
                }
            });
    }
    
    private void executeClockUpAttack(Level level, Player player, Vec3 direction) {
        // 对路径上和终点周围的敌人造成伤害
        level.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(10.0),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                // 计算与玩家的距离和方向
                Vec3 toEntity = entity.position().subtract(player.position());
                double dotProduct = toEntity.normalize().dot(direction.normalize());
                
                // 只对前方的敌人造成更高伤害
                float damageFactor = dotProduct > 0.3 ? 2.0f : 1.0f;
                
                entity.hurt(level.damageSources().playerAttack(player), getAttackDamage() * damageFactor);
                
                // 强大的击退效果
                Vec3 knockback = direction.scale(2.0);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            });
    }
    
    /**
     * 为非玩家实体（如僵尸）执行时缓能力
     * 重写基类方法，让僵尸也能使用甲斗王的时缓效果
     */
    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 1. 为非玩家实体激活时缓效果 - 只给周围实体和环境造成缓慢，不直接造成伤害
            activateFullClockDownForEntity(level, shooter);
            
            // 2. 给予非玩家实体速度提升，类似于玩家的Clock Up
            shooter.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 4));
            shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
            
            // 3. 向前方冲刺
            Vec3 velocity = direction.scale(3.0);
            shooter.setDeltaMovement(velocity);
            
            // 4. 播放音效
            level.playSound(null, shooter.blockPosition(), net.minecraft.sounds.SoundEvents.PISTON_EXTEND, net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.5F);
        }
        
        // 5. 应用视觉效果
        super.applyVisualEffects(level, shooter, direction);
    }
    
    /**
     * 为非玩家实体激活时缓效果
     */
    private void activateFullClockDownForEntity(Level level, LivingEntity entity) {
        double effectRange = 10.0; // 从发动者为中心，半径10格范围内发动时缓效果
        
        // 1. 对周围所有生物施加减速效果
        level.getEntitiesOfClass(LivingEntity.class, 
                entity.getBoundingBox().inflate(effectRange),
                target -> {
                    // 过滤自己，且只对活着的实体生效
                    if (target != entity && target.isAlive()) {
                        // 如果是玩家实体，检查是否也是甲斗王模式
                        if (target instanceof Player targetPlayer) {
                            // 同为甲斗王模式的玩家互相无视时缓效果
                            return !isPlayerInKabutoMode(targetPlayer);
                        }
                        // 非玩家实体正常减速
                        return true;
                    }
                    return false;
                })
            .forEach(target -> {
                // 施加强力减速效果
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4));
                
                // 检查是否是弓箭手类型实体（骷髅射手等和手持弓的玩家）
                boolean isArcher = target instanceof net.minecraft.world.entity.monster.AbstractSkeleton || 
                                  target instanceof net.minecraft.world.entity.monster.Skeleton ||
                                  target instanceof net.minecraft.world.entity.monster.Stray ||
                                  target instanceof net.minecraft.world.entity.monster.WitherSkeleton ||
                                  (target instanceof Player && ((Player)target).getMainHandItem().getItem() instanceof net.minecraft.world.item.BowItem);
                
                if (isArcher) {
                    // 对于弓箭手，给予最高级别的挖掘/攻击速度减慢效果，使其拉弓极慢
                    target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 300, 4));
                } else {
                    // 其他实体使用普通的减慢效果
                    target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 4));
                }
                
                // 添加缓慢下落效果，进一步限制敌人的行动能力
                target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 1));
                
                // 添加视觉反馈：发光效果
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 150, 0));
            });
        
        // 2. 对周围所有弹射物减速
        level.getEntitiesOfClass(Projectile.class, 
                entity.getBoundingBox().inflate(effectRange))
            .forEach(projectile -> {
                // 保存原始速度向量，以便时缓解除后恢复
                // 使用一个特殊的键值来存储速度信息，这里使用负数来表示这是速度数据
                if (!slowedTntEntities.containsKey(projectile.getUUID())) {
                    // 保存一个标记值，表示这是一个被时缓的投射物
                    slowedTntEntities.put(projectile.getUUID(), -1);
                }
                
                // 将投射物速度设置为0，使其完全停止在空中
                projectile.setDeltaMovement(Vec3.ZERO);
                
                // 对于箭矢，确保额外的处理使其完全停留在半空
                if (projectile instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow) {
                    // 临时设置伤害为0，防止伤害计算
                    arrow.setBaseDamage(0.0);
                    
                    // 确保箭矢完全停止 - 覆盖掉可能存在的其他速度设置
                    arrow.setDeltaMovement(Vec3.ZERO);
                    
                    // 确保重力不影响箭矢
                    arrow.setNoGravity(true);
                    
                    // 停止箭矢的所有物理效果
                    if (arrow instanceof net.minecraft.world.entity.projectile.Arrow) {
                        net.minecraft.world.entity.projectile.Arrow minecraftArrow = (net.minecraft.world.entity.projectile.Arrow)arrow;
                        minecraftArrow.setNoPhysics(true);
                    }
                }
            });
        
        // 3. 延迟周围所有TNT的爆炸
        level.getEntitiesOfClass(PrimedTnt.class, 
                entity.getBoundingBox().inflate(effectRange))
            .forEach(tnt -> {
                // 记录原始 fuse 值，用于后续可能的恢复
                if (!slowedTntEntities.containsKey(tnt.getUUID())) {
                    slowedTntEntities.put(tnt.getUUID(), tnt.getFuse());
                }
                
                // 增加TNT的 fuse 值，延长爆炸时间
                int currentFuse = tnt.getFuse();
                if (currentFuse > 0) {
                    tnt.setFuse(currentFuse + 10); // 额外增加10tick
                }
            });
            
        // 4. 停止周围的雨滴粒子
        stopRainParticles(level, entity, effectRange);
        
        // 5. 设置定时任务，在时缓效果结束后恢复所有被冻结的箭矢
        scheduleEffectReset(level);
    }
    
    /**
     * 为非玩家实体执行Clock Up攻击
     */
    private void executeNonPlayerClockUpAttack(Level level, LivingEntity shooter, Vec3 direction) {
        // 对路径上和终点周围的敌人造成伤害
        level.getEntitiesOfClass(LivingEntity.class, 
                shooter.getBoundingBox().inflate(10.0),
                entity -> entity != shooter) // 过滤掉使用者自己
            .forEach(entity -> {
                // 计算与使用者的距离和方向
                Vec3 toEntity = entity.position().subtract(shooter.position());
                double dotProduct = toEntity.normalize().dot(direction.normalize());
                
                // 只对前方的敌人造成更高伤害
                float damageFactor = dotProduct > 0.3 ? 1.5f : 1.0f;
                
                entity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage() * damageFactor * 0.8f); // 略微降低伤害平衡
                
                // 强大的击退效果
                Vec3 knockback = direction.scale(1.5);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            });
    }

    @Override
    public String getRiderName() {
        return "Kabuto";
    }

    @Override
    public String getActivationSoundName() {
        return "Clock Up! Clock Down!";
    }

    @Override
    public float getAttackDamage() {
        return 55.0f; // 提升伤害值，甲斗王作为骑士之王应该更强
    }

    @Override
    public float getEffectRange() {
        return 10.0f; // 修改为10格范围，与时缓效果保持一致
    }
    
    /**
     * 恢复被时缓冻结的箭矢，使它们在时缓解除后能够继续飞行
     */
    public static void restoreProjectiles(Level level) {
        // 获取所有被记录的实体UUID
        ArrayList<UUID> entitiesToProcess = new ArrayList<>(slowedTntEntities.keySet());
        
        // 使用更大的范围来确保能找到所有被时缓的投射物
        double range = 50.0; // 使用更大的范围以确保能覆盖所有被时缓的投射物
        
        // 为了效率，使用更大的搜索范围但避免使用原点中心
        // 由于我们不知道玩家位置，使用足够大的范围来覆盖可能的区域
        AABB searchBounds = new AABB(-range, -range, -range, range, range, range);
        
        // 查找并处理所有类型的投射物
        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, searchBounds)) {
            // 检查投射物是否在我们的记录中
            if (entitiesToProcess.contains(projectile.getUUID())) {
                // 对于所有投射物，根据其类型设置适当的恢复速度
                if (projectile instanceof AbstractArrow) {
                    // 箭矢使用稍微快一点的速度
                    projectile.setDeltaMovement(projectile.getLookAngle().normalize().scale(2.5));
                } else {
                    // 其他投射物使用标准速度
                    projectile.setDeltaMovement(projectile.getLookAngle().normalize().scale(2.0));
                }
                
                // 对于箭矢，进行额外的恢复处理
                if (projectile instanceof AbstractArrow arrow) {
                    // 恢复箭矢的物理效果
                    arrow.setNoGravity(false);
                    
                    // 恢复箭矢的正常物理行为
                    if (arrow instanceof Arrow) {
                        Arrow minecraftArrow = (Arrow)arrow;
                        minecraftArrow.setNoPhysics(false);
                    }
                    
                    // 恢复箭矢的伤害
                    arrow.setBaseDamage(3.0);
                }
                
                // 从记录中移除
                slowedTntEntities.remove(projectile.getUUID());
            }
        }
        
        // 查找并处理所有TNT
        for (PrimedTnt tnt : level.getEntitiesOfClass(PrimedTnt.class, searchBounds)) {
            if (entitiesToProcess.contains(tnt.getUUID()) && slowedTntEntities.containsKey(tnt.getUUID())) {
                // 获取原始fuse值
                Integer originalFuse = slowedTntEntities.get(tnt.getUUID());
                if (originalFuse != null && originalFuse > 0) {
                    // 设置回原始fuse值（减去一些以补偿时间流逝）
                    int adjustedFuse = Math.max(0, originalFuse - 20); // 减去约1秒的时间
                    tnt.setFuse(adjustedFuse);
                }
                // 从记录中移除
                slowedTntEntities.remove(tnt.getUUID());
            }
        }
    }
    
    /**
     * 清除所有被时缓的效果，恢复箭矢和TNT的正常状态
     * 当甲斗王时缓效果结束时调用此方法
     */
    public static void clearAllSlowedEffects(Level level) {
        // 恢复所有被冻结的箭矢
        restoreProjectiles(level);
        
        // 清空记录的实体列表
        slowedTntEntities.clear();
    }
    
    @Override
    public double getEnergyCost() {
        return 40.0; // 增加能量消耗，因为实现了更全面的时缓效果
    }
    
    /**
     * 设置定时任务，在时缓效果持续时间结束后自动恢复所有被冻结的箭矢和TNT
     */
    private void scheduleEffectReset(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            // 使用服务器的调度系统来设置延迟任务
            serverLevel.getServer().execute(() -> {
                // 创建一个新的线程来处理延迟
                Thread resetThread = new Thread(() -> {
                    try {
                        // 等待时缓效果持续时间（游戏刻转换为毫秒：20刻=1秒）
                        Thread.sleep(TIME_SLOW_DURATION * 50);
                        
                        // 在主线程中恢复效果
                        serverLevel.getServer().execute(() -> {
                            try {
                                // 确保在服务器线程中执行
                                if (!serverLevel.isClientSide) {
                                    clearAllSlowedEffects(serverLevel);
                                }
                            } catch (Exception e) {
                                // 捕获任何异常，防止服务器崩溃
                                System.err.println("Error during effect reset: " + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    } catch (InterruptedException e) {
                        // 线程被中断
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        // 捕获其他可能的异常
                        System.err.println("Error in reset thread: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                
                // 设置线程为守护线程，防止阻止服务器关闭
                resetThread.setDaemon(true);
                resetThread.start();
            });
        }
    }
    /**
     * 停止周围的雨滴粒子
     * 在服务器端，可以通过停止生成新的雨滴粒子来模拟时间停止效果
     * 在客户端，可以通过修改粒子速度来实现类似效果（此处仅实现服务器端）
     */
    private void stopRainParticles(Level level, LivingEntity entity, double range) {
        if (level instanceof ServerLevel serverLevel) {
            // 获取玩家周围的雨滴粒子
            // 由于直接访问粒子系统比较复杂，这里采用另一种方法：
            // 在实体周围生成静止的水滴粒子，创造时间停止的视觉效果
            for (int x = (int)(entity.getX() - range); x <= entity.getX() + range; x++) {
                for (int y = (int)(entity.getY() - range); y <= entity.getY() + range; y++) {
                    for (int z = (int)(entity.getZ() - range); z <= entity.getZ() + range; z++) {
                        // 只在雨中区域生成静止的水滴粒子
                        if (level.isRainingAt(new net.minecraft.core.BlockPos(x, y, z))) {
                            // 计算距离，确保在范围内
                            double distance = Math.sqrt(
                                Math.pow(x - entity.getX(), 2) + 
                                Math.pow(y - entity.getY(), 2) + 
                                Math.pow(z - entity.getZ(), 2)
                            );
                            
                            if (distance <= range) {
                                // 在雨滴位置生成静止的水滴粒子
                                serverLevel.sendParticles(
                                    ParticleTypes.DRIPPING_WATER,  // 使用静止的水滴粒子
                                    x + level.random.nextDouble(),
                                    y + level.random.nextDouble() * 2,
                                    z + level.random.nextDouble(),
                                    1,  // 每个位置生成1个粒子
                                    0, 0, 0,  // 零速度，表示静止
                                    0  // 无额外数据
                                );
                            }
                        }
                    }
                }
            }
        }
    }
}
