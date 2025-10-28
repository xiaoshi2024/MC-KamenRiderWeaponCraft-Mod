package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * 平成骑士效果的抽象基类，提供getEnergyCost()方法的默认实现
 */
public abstract class AbstractHeiseiRiderEffect implements HeiseiRiderEffect {
    
    /**
     * 默认的骑士能量消耗值
     * 子类可以重写此方法以提供自定义的能量消耗值
     */
    @Override
    public double getEnergyCost() {
        return 20.0; // 默认消耗20点骑士能量
    }
    
    /**
     * 执行特殊攻击效果 - 适配器方法，允许非玩家实体也能使用技能
     */
    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        // 区分玩家和非玩家实体
        if (shooter instanceof Player player) {
            // 玩家实体使用正常的特效逻辑
            executePlayerSpecialAttack(level, player, direction);
        } else {
            // 非玩家实体（如僵尸、骷髅等）使用完整的特效逻辑
            executeNonPlayerSpecialAttack(level, shooter, direction);
        }
    }
    
    /**
     * 为非玩家实体执行特殊攻击效果
     * 为所有生物类型提供完整的特效支持
     * 子类可以重写此方法以提供更定制化的非玩家实体特效
     */
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        // 为所有生物类型提供完整的特效支持
        if (!level.isClientSide) {
            // 播放骑士选择音效
            HeiseiRiderEffectManager.playSelectionSound(level, shooter, getRiderName());
            
            // 播放攻击音效
            HeiseiRiderEffectManager.playAttackSound(level, shooter, getRiderName());
            
            // 获取shooter的朝向，如果direction为空则使用shooter的lookAngle
            Vec3 attackDirection = direction != null && direction.lengthSqr() > 0 ? 
                                   direction.normalize() : shooter.getLookAngle();
            
            // 执行前方定向攻击而非全范围攻击
            float attackRange = getEffectRange();
            float width = attackRange / 2; // 攻击宽度为范围的一半，使攻击区域呈长方体向前延伸
            
            // 计算攻击区域的起始和结束点
            Vec3 start = shooter.position().add(0, shooter.getEyeHeight() * 0.5, 0); // 从shooter胸部高度开始
            Vec3 end = start.add(attackDirection.scale(attackRange)); // 向前延伸attackRange距离
            
            // 创建一个向前延伸的长方体区域，而非以shooter为中心的立方体
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(
                Math.min(start.x, end.x) - width,
                Math.min(start.y, end.y) - 1, // 向下延伸1格
                Math.min(start.z, end.z) - width,
                Math.max(start.x, end.x) + width,
                Math.max(start.y, end.y) + 1, // 向上延伸1格
                Math.max(start.z, end.z) + width
            );
            
            // 只攻击shooter前方区域内的实体
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, attackBox,
                    entity -> entity != shooter && entity != null && entity.isAlive())) {
                // 额外检查目标是否在shooter的前方（通过计算点积）
                Vec3 targetRelative = target.position().subtract(start);
                if (targetRelative.dot(attackDirection) > 0) { // 点积为正表示在前方
                    DamageSource damageSource = level.damageSources().mobAttack(shooter);
                    target.hurt(damageSource, getAttackDamage()); // 使用全额伤害
                }
            }
            
            // 添加更多通用特效逻辑
            applyVisualEffects(level, shooter, attackDirection);
            
            // 子类可以重写此方法以提供更具特色的特效
        }
    }
    
    /**
     * 应用视觉效果（粒子等）
     * 基础视觉效果可以在这里实现，如粒子效果等
     * 子类可以重写以提供特定骑士的视觉效果
     */
    protected void applyVisualEffects(Level level, LivingEntity shooter, Vec3 direction) {
        // 添加基本的粒子效果，确保非玩家实体也能显示视觉特效
        if (level.isClientSide) {
            // 客户端粒子效果
            for (int i = 0; i < 20; i++) {
                double offsetX = random.nextGaussian() * 0.5;
                double offsetY = random.nextGaussian() * 0.5 + 1.0;
                double offsetZ = random.nextGaussian() * 0.5;
                
                // 创建粒子：从射手位置向目标方向发射
                Vec3 particlePos = shooter.position().add(offsetX, offsetY, offsetZ);
                Vec3 particleMotion = direction.scale(0.5).add(
                    random.nextGaussian() * 0.1,
                    random.nextGaussian() * 0.1,
                    random.nextGaussian() * 0.1
                );
                
                // 使用白色粒子作为默认效果
                level.addParticle(
                    net.minecraft.core.particles.ParticleTypes.CLOUD,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    particleMotion.x,
                    particleMotion.y,
                    particleMotion.z
                );
            }
        } else {
            // 服务器端：可以添加音效或其他服务器端效果
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_BLAST, 
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
        }
    }
    
    // 添加随机数生成器，用于粒子效果
    private static final java.util.Random random = new java.util.Random();
    
    /**
     * 执行特殊攻击效果 - 原始方法，仅适用于玩家
     * 提供默认的前方定向攻击实现，子类可以重写以提供更定制化的玩家特效
     */
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 播放骑士选择音效
            HeiseiRiderEffectManager.playSelectionSound(level, player, getRiderName());
            
            // 播放攻击音效
            HeiseiRiderEffectManager.playAttackSound(level, player, getRiderName());
            
            // 获取player的朝向，如果direction为空则使用player的lookAngle
            Vec3 attackDirection = direction != null && direction.lengthSqr() > 0 ? 
                                   direction.normalize() : player.getLookAngle();
            
            // 执行前方定向攻击而非全范围攻击
            float attackRange = getEffectRange();
            float width = attackRange / 2; // 攻击宽度为范围的一半，使攻击区域呈长方体向前延伸
            
            // 计算攻击区域的起始和结束点
            Vec3 start = player.position().add(0, player.getEyeHeight() * 0.5, 0); // 从player胸部高度开始
            Vec3 end = start.add(attackDirection.scale(attackRange)); // 向前延伸attackRange距离
            
            // 创建一个向前延伸的长方体区域，而非以player为中心的立方体
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(
                Math.min(start.x, end.x) - width,
                Math.min(start.y, end.y) - 1, // 向下延伸1格
                Math.min(start.z, end.z) - width,
                Math.max(start.x, end.x) + width,
                Math.max(start.y, end.y) + 1, // 向上延伸1格
                Math.max(start.z, end.z) + width
            );
            
            // 只攻击player前方区域内的实体
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, attackBox,
                    entity -> entity != player && entity != null && entity.isAlive())) {
                // 额外检查目标是否在player的前方（通过计算点积）
                Vec3 targetRelative = target.position().subtract(start);
                if (targetRelative.dot(attackDirection) > 0) { // 点积为正表示在前方
                    DamageSource damageSource = level.damageSources().playerAttack(player);
                    target.hurt(damageSource, getAttackDamage()); // 使用全额伤害
                }
            }
            
            // 添加更多通用特效逻辑
            applyVisualEffects(level, player, attackDirection);
            
            // 子类可以重写此方法以提供更具特色的特效
        }
    }
    
    /**
     * 返回骑士名称
     * 子类必须实现此方法
     */
    @Override
    public abstract String getRiderName();
    
    /**
     * 获取激活音效名称
     * 子类必须实现此方法
     */
    @Override
    public abstract String getActivationSoundName();
    
    /**
     * 攻击伤害值
     * 子类必须实现此方法
     */
    @Override
    public abstract float getAttackDamage();
    
    /**
     * 特效范围
     * 子类必须实现此方法
     */
    @Override
    public abstract float getEffectRange();
}