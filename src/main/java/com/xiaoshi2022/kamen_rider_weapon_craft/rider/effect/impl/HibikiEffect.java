package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HibikiEffect implements HeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动音击之技，释放强大的音波攻击
            // 1. 创建一个环形的音波，对周围敌人造成伤害
            for (int i = 0; i < 3; i++) {
                double radius = 2.0 + (i * 2.0);
                executeSoundWaveAttack(level, player, radius);
            }
            
            // 2. 给予玩家抗性效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    private void executeSoundWaveAttack(Level level, Player player, double radius) {
        // 创建环形音波，对范围内的敌人造成伤害
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                new net.minecraft.world.phys.AABB(
                        player.getX() - radius, player.getY() - 1, player.getZ() - radius,
                        player.getX() + radius, player.getY() + 2, player.getZ() + radius),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                // 计算与玩家的距离
                double distance = entity.distanceTo(player);
                // 在环形范围内造成伤害
                if (distance >= radius - 0.5 && distance <= radius + 0.5) {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.5f);
                    
                    // 使敌人摇晃（可以添加蹒跚效果）
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                        (level.random.nextDouble() - 0.5) * 0.3,
                        0.1,
                        (level.random.nextDouble() - 0.5) * 0.3
                    ));
                }
            });
    }

    @Override
    public String getRiderName() {
        return "Hibiki";
    }

    @Override
    public String getActivationSoundName() {
        return "Oni no Koe!";
    }

    @Override
    public float getAttackDamage() {
        return 47.0f; // 普通骑士 - Hibiki使用音击之技，是平成第七位骑士，伤害略高于基础值
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
}
