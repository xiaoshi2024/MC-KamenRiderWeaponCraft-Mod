package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleTypes;

public class FaizEffect implements HeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Axel Form攻击，高速移动并对敌人造成连续伤害
            // 1. 给予玩家极高的速度和抗性效果
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 3));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
            
            // 2. 对前方敌人造成连击伤害
            double reach = 8.0;
            Vec3 start = player.getEyePosition(1.0f);
            Vec3 end = start.add(direction.scale(reach));
            
            // 优化：使用更高效的实体查找方式
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                    new net.minecraft.world.phys.AABB(start, end).inflate(1.0),
                    entity -> entity != player) // 提前过滤掉玩家自己
                .forEach(entity -> {
                    // 连续攻击，每次伤害较低但快速连续
                    for (int i = 0; i < 3; i++) {
                        ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                            level.damageSources().playerAttack(player), getAttackDamage() * 0.4f);
                    }
                });
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
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
        return 48.0f; // 普通骑士 - Faiz使用光子血液，拥有Axel Form等形态，伤害略高于基础值
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
}
