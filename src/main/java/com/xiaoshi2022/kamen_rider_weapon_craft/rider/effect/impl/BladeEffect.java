package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BladeEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Lightning Slash攻击，使用Blade的光刃
            // 1. 向前方发射光刃，对直线上的敌人造成伤害
            double reach = 10.0;
            Vec3 start = player.getEyePosition(1.0f);
            Vec3 end = start.add(direction.scale(reach));
            
            // 优化：使用更高效的实体查找方式
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                    new net.minecraft.world.phys.AABB(start, end).inflate(1.0),
                    entity -> entity != player) // 提前过滤掉玩家自己
                .forEach(entity -> {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage());
                });
            
            // 2. 给予玩家速度提升效果
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }

    @Override
    public String getRiderName() {
        return "Blade";
    }

    @Override
    public String getActivationSoundName() {
        return "Lightning Slash!";
    }

    @Override
    public float getAttackDamage() {
        return 49.0f; // 普通骑士 - Blade拥有醒剑力量，伤害略高于基础值
    }

    @Override
    public float getEffectRange() {
        return 10.0f;
    }
}
