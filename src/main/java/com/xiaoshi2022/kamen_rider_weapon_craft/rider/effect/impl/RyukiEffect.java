package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RyukiEffect extends AbstractHeiseiRiderEffect {

    // 龙骑的不同卡片
    private enum RyukiCard {
        SwordVent, 
        ShootVent, 
        GuardVent, 
        StrikeVent, 
        FinalVent
    }

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 为玩家提供火焰抗性，避免在战斗中被火焰伤害
            addFireResistance(player);
            
            // 服务器端：发动Dragon Rider Kick攻击，使用不同的卡片能力
            // 随机选择一个卡片能力
            RyukiCard selectedCard = RyukiCard.values()[level.random.nextInt(RyukiCard.values().length)];
            
            switch (selectedCard) {
                case SwordVent:
                    executeSwordVent(level, player, direction);
                    break;
                case ShootVent:
                    executeShootVent(level, player, direction);
                    break;
                case GuardVent:
                    executeGuardVent(level, player, direction);
                    break;
                case StrikeVent:
                    executeStrikeVent(level, player, direction);
                    break;
                case FinalVent:
                    executeFinalVent(level, player, direction);
                    break;
            }
            
            // 给予玩家镜世界相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    private void executeSwordVent(Level level, Player player, Vec3 direction) {
        // 剑降临：使用龙剑攻击
        double reach = getEffectRange();
        Vec3 normalizedDirection = direction.normalize();
        
        // 使用pick获取目标
        net.minecraft.world.phys.HitResult result = player.pick(reach, 0.0f, false);
        
        if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity && entity != player) {
                // 检查目标是否在玩家面前
                Vec3 toEntity = livingEntity.position().subtract(player.position()).normalize();
                double dotProduct = toEntity.dot(normalizedDirection);
                
                if (dotProduct > 0.5) { // 约60度角范围内
                    livingEntity.hurt(
                        level.damageSources().playerAttack(player), getAttackDamage());
                    // 给予敌人缓慢效果
                    livingEntity.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                    // 添加火焰效果
                    livingEntity.setSecondsOnFire(5);
                }
            }
        }
    }
    
    private void executeShootVent(Level level, Player player, Vec3 direction) {
        // 射击降临：发射龙弹
        Vec3 normalizedDirection = direction.normalize();
        double maxReach = 15.0;
        
        // 查找玩家面前范围内的实体
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(normalizedDirection.scale(maxReach));
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(2.0);
        
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, attackBox, entity -> {
            if (entity == player) return false;
            
            // 确保目标在玩家面前的角度范围内（约60度）
            Vec3 toEntity = entity.position().subtract(player.position()).normalize();
            return toEntity.dot(normalizedDirection) > 0.5;
        }).forEach(livingEntity -> {
            // 对目标造成伤害
            livingEntity.hurt(
                level.damageSources().playerAttack(player), getAttackDamage() * 0.3f);
            // 增强火焰效果
            livingEntity.setSecondsOnFire(7);
        });
        
        // 仍然保留原有的角度偏移发射逻辑，但通过范围检查限制目标
        for (int i = 0; i < 5; i++) {
            double angleOffset = (i - 2) * Math.PI / 12;
            Vec3 bulletDir = new Vec3(
                Math.cos(angleOffset) * direction.x - Math.sin(angleOffset) * direction.z,
                direction.y + (level.random.nextDouble() - 0.5) * 0.3,
                Math.sin(angleOffset) * direction.x + Math.cos(angleOffset) * direction.z
            ).normalize();
        }
    }
    
    private void executeGuardVent(Level level, Player player, Vec3 direction) {
        // 防御降临：使用龙盾防御
        // 给予玩家高额防御
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2));
        
        // 立即对玩家面前的敌人造成反弹伤害
        Vec3 normalizedDirection = direction.normalize();
        double range = getEffectRange();
        
        // 创建基于玩家前方的锥形AABB区域
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(normalizedDirection.scale(range));
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(range / 2, 2.0, range / 2);
        
        // 优化：使用更高效的实体查找方式，并添加方向检查
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, attackBox, entity -> {
            if (entity == player) return false;
            
            // 确保目标在玩家面前的角度范围内（约60度）
            Vec3 toEntity = entity.position().subtract(player.position()).normalize();
            return toEntity.dot(normalizedDirection) > 0.5;
        }).forEach(entity -> {
            // 使用荆棘伤害来源模拟反弹效果
            entity.hurt(
                level.damageSources().thorns(player), getAttackDamage() * 0.3f);
        });
    }
    
    private void executeStrikeVent(Level level, Player player, Vec3 direction) {
        // 突击降临：使用龙爪攻击
        Vec3 normalizedDirection = direction.normalize();
        double range = getEffectRange();
        
        // 创建基于玩家前方的锥形AABB区域
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(normalizedDirection.scale(range));
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(range / 2, 2.0, range / 2);
        
        // 添加方向检查
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, attackBox, entity -> {
            if (entity == player) return false;
            
            // 确保目标在玩家面前的角度范围内（约60度）
            Vec3 toEntity = entity.position().subtract(player.position()).normalize();
            return toEntity.dot(normalizedDirection) > 0.5;
        }).forEach(livingEntity -> {
            livingEntity.hurt(
                level.damageSources().playerAttack(player), getAttackDamage() * 0.6f);
            // 给予玩家短暂的速度加成
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));
            // 添加火焰效果
            livingEntity.setSecondsOnFire(5);
        });
    }
    
    private void executeFinalVent(Level level, Player player, Vec3 direction) {
        // 最终降临：召唤契约兽无双龙，发动Dragon Rider Kick
        Vec3 normalizedDirection = direction.normalize();
        double range = getEffectRange();
        Vec3 targetPos = player.getEyePosition(1.0f).add(normalizedDirection.scale(range));
        
        // 制造爆炸效果，使用EXPLOSION类型并检查mobGriefing规则
        level.explode((Entity) player, targetPos.x, targetPos.y, targetPos.z,
                (float) (range / 2), Level.ExplosionInteraction.NONE); // 使用NONE类型避免方块破坏
        
        // 创建基于玩家前方的攻击区域
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(targetPos, targetPos).inflate(4.0);
        
        // 对范围内敌人造成高额伤害，并添加方向检查
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, attackBox, entity -> {
            if (entity == player) return false;
            
            // 确保目标在玩家面前的角度范围内（约60度）
            Vec3 toEntity = entity.position().subtract(player.position()).normalize();
            return toEntity.dot(normalizedDirection) > 0.5;
        }).forEach(livingEntity -> {
            livingEntity.hurt(
                level.damageSources().playerAttack(player), getAttackDamage() * 1.5f);
            // 强大的击退效果
            Vec3 knockback = normalizedDirection.scale(2.0);
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(knockback));
            // 添加持续火焰效果
            livingEntity.setSecondsOnFire(10);
        });
        
        // 给予玩家临时无敌效果
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 5));
    }

    @Override
    public String getRiderName() {
        return "Ryuki";
    }

    @Override
    public String getActivationSoundName() {
        return "Dragon Rider Kick!";
    }

    @Override
    public float getAttackDamage() {
        return 47.0f; // 普通骑士 - Ryuki是平成第三位骑士，使用卡片系统和契约兽，伤害略高于基础值
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
    
    /**
     * 发射火球技能 - 右键点击时触发
     * 实现龙骑发射火焰火球的能力
     */
    public void executeFireballAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 播放攻击音效
            com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager.playAttackSound(level, shooter, getRiderName());
            
            // 获取发射方向
            Vec3 fireballDirection = direction != null && direction.lengthSqr() > 0 ? 
                                     direction.normalize() : shooter.getLookAngle();
            
            // 创建并发射火球
            SmallFireball fireball = new SmallFireball(
                level,
                shooter,
                fireballDirection.x * 1.5,
                fireballDirection.y * 1.5,
                fireballDirection.z * 1.5
            );
            
            // 设置火球的初始位置
            fireball.setPos(
                shooter.getX() + fireballDirection.x * 0.5,
                shooter.getEyeY() - 0.3 + fireballDirection.y * 0.5,
                shooter.getZ() + fireballDirection.z * 0.5
            );
            
            // 设置火球伤害
            fireball.xPower = fireballDirection.x * 1.5;
            fireball.yPower = fireballDirection.y * 1.5;
            fireball.zPower = fireballDirection.z * 1.5;
            
            // 添加到世界
            level.addFreshEntity(fireball);
        }
    }
    
    /**
     * 为玩家提供火焰抗性
     * 这是龙骑的标志性能力之一，体现其与火焰的联系
     */
    private void addFireResistance(Player player) {
        // 给予玩家火焰抗性，避免在战斗中被火焰伤害
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
    }
    
    /**
     * 为非玩家实体（如僵尸）执行龙骑的特殊攻击效果
     */
    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 为非玩家实体提供火焰抗性
            if (shooter instanceof net.minecraft.world.entity.Mob) {
                shooter.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
            }
            
            // 非玩家实体右键默认使用火球攻击
            executeFireballAttack(level, shooter, direction);
        }
    }
}
