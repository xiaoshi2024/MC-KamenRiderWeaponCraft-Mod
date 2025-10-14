package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.build;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Kamen Rider Build 特效实现类
 * 单一气泡兔坦形态实现，包含招牌技能公式踢
 */
public class BuildRiderEffect implements HeiseiRiderEffect {

    // 单一形态 - 气泡兔坦（RabbitTank Sparkling）
    // 攻击和防御参数
    private static final float ATTACK_MULTIPLIER = 1.2f; // 攻击力倍率
    private static final float DEFENSE_MULTIPLIER = 1.1f; // 防御力倍率

    // 公式踢的相关参数
    private static final float ROCKET_PUNCH_DAMAGE = 30.0f; // 火箭拳伤害
    private static final float SPECIAL_KICK_DAMAGE = 51.0f; // 公式踢基础伤害
    private static final float SPECIAL_KICK_RANGE = 8.0f; // 公式踢范围


    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 执行公式踢（气泡兔坦形态的招牌技能）
            executeFormulaKick(level, player, direction);

            // 给予玩家气泡兔坦形态的增益效果
            applyRabbitTankEffects(level, player);
        }
    }

    @Override
    public String getRiderName() {
        return "Build";
    }

    @Override
    public String getActivationSoundName() {
        return "Best Match!";
    }

    @Override
    public float getAttackDamage() {
        // 基础伤害乘以气泡兔坦形态的攻击倍数
        return SPECIAL_KICK_DAMAGE * ATTACK_MULTIPLIER;
    }

    @Override
    public float getEffectRange() {
        return 5.0f;
    }

    // 执行公式踢（气泡兔坦形态的招牌技能）
    private void executeFormulaKick(Level level, Player player, Vec3 direction) {
        // 1. 首先执行前置的火箭拳攻击（连续拳击）
        executeRocketPunch(level, player, direction);
        
        // 2. 生成特效实体
        BuildRiderEntity.trySpawnEffect(level, player, direction, getAttackDamage());
        
        // 3. 然后执行公式踢的终结攻击
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(SPECIAL_KICK_RANGE));
        
        // 查找范围内的敌人
        LivingEntity target = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, 
                new net.minecraft.world.phys.AABB(start, end).inflate(1.0),
                entity -> entity != player)) {
            double distance = entity.distanceToSqr(player);
            if (distance < closestDistance) {
                closestDistance = distance;
                target = entity;
            }
        }
        
        if (target != null) {
            // 播放踢击音效
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, 
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            
            // 造成伤害（附带击退效果）
            DamageSource kickDamage = level.damageSources().playerAttack(player);
            boolean hurt = target.hurt(kickDamage, getAttackDamage());
            
            if (hurt) {
                // 添加公式踢特有的粒子效果
                spawnKickParticles(level, target);
                
                // 强大的击退效果
                Vec3 knockback = direction.scale(2.0).add(0, 0.5, 0);
                target.push(knockback.x, knockback.y, knockback.z);
            }
        }
    }

    // 执行火箭拳攻击（公式踢的前置动作）
    private void executeRocketPunch(Level level, Player player, Vec3 direction) {
        // 快速连续拳击
        for (int i = 0; i < 3; i++) {
            // 对前方敌人造成伤害
            Vec3 start = player.getEyePosition(1.0f);
            Vec3 end = start.add(direction.scale(5.0 + i * 1.0));

            level.getEntitiesOfClass(LivingEntity.class,
                            new net.minecraft.world.phys.AABB(start, end).inflate(0.5),
                            entity -> entity != player)
                    .forEach(entity -> {
                        entity.hurt(
                                level.damageSources().playerAttack(player), ROCKET_PUNCH_DAMAGE);
                    });
        }
    }

    // 生成公式踢的粒子效果
    private void spawnKickParticles(Level level, LivingEntity target) {
        // 生成大量的火花和光效粒子
        for (int i = 0; i < 20; ++i) {
            double d0 = level.random.nextGaussian() * 0.02D;
            double d1 = level.random.nextGaussian() * 0.02D;
            double d2 = level.random.nextGaussian() * 0.02D;
            level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                    target.getX() + (double)(level.random.nextFloat() * target.getBbWidth() * 2.0F) - (double)target.getBbWidth(),
                    target.getY() + 0.5D + (double)(level.random.nextFloat() * target.getBbHeight()),
                    target.getZ() + (double)(level.random.nextFloat() * target.getBbWidth() * 2.0F) - (double)target.getBbWidth(),
                    d0, d1, d2);
        }

        // 生成中心的光效
        level.addParticle(ParticleTypes.GLOW_SQUID_INK,
                target.getX(), target.getY() + 0.5D, target.getZ(),
                0.0D, 0.0D, 0.0D);
    }

    // 应用气泡兔坦形态的增益效果
    private void applyRabbitTankEffects(Level level, Player player) {
        // 兔子速度和坦克防御增强
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));

        // 气泡兔坦特有的爆发力量
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 0));
    }
}