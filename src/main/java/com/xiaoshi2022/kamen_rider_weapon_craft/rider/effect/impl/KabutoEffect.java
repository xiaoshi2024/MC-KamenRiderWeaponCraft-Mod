package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleTypes;

public class KabutoEffect implements HeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Rider Kick，使用Clock Up能力
            // 1. 给予玩家极高的速度和抗性效果，模拟Clock Up
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 5));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));
            
            // 2. 向前方冲刺并造成伤害
            Vec3 velocity = direction.scale(5.0);
            player.setDeltaMovement(velocity);
            
            // 3. 直接对前方路径上的敌人造成伤害，避免使用Thread.sleep
            executeClockUpAttack(level, player, direction);
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    private void executeClockUpAttack(Level level, Player player, Vec3 direction) {
        // 对路径上和终点周围的敌人造成伤害 - 优化：检测更大的范围以弥补没有延迟的效果
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                player.getBoundingBox().inflate(10.0), // 增大检测范围
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                // 计算与玩家的距离和方向
                Vec3 toEntity = entity.position().subtract(player.position());
                double dotProduct = toEntity.normalize().dot(direction.normalize());
                
                // 只对前方的敌人造成更高伤害
                float damageFactor = dotProduct > 0.3 ? 1.8f : 1.0f; // 略微降低方向要求并提高伤害
                
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * damageFactor);
                
                // 强大的击退效果
                Vec3 knockback = direction.scale(2.0);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            });
    }

    @Override
    public String getRiderName() {
        return "Kabuto";
    }

    @Override
    public String getActivationSoundName() {
        return "Clock Up!";
    }

    @Override
    public float getAttackDamage() {
        return 50.0f; // 高级骑士 - Kabuto拥有Clock Up能力，是非常强大的主角骑士，伤害较高
    }

    @Override
    public float getEffectRange() {
        return 12.0f;
    }
}
