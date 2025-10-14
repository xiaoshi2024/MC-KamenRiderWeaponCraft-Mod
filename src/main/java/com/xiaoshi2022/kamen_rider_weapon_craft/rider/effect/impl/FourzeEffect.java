package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FourzeEffect implements HeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Rider Rocket Drill Kick，召唤宇宙能量
            // 1. 给予玩家跳跃增强和缓降效果，模拟太空行走
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 300, 2));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0));
            
            // 2. 释放火箭钻头踢，对前方敌人造成伤害
            executeRocketDrillKick(level, player, direction);
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    private void executeRocketDrillKick(Level level, Player player, Vec3 direction) {
        // 火箭钻头踢，对前方直线上的敌人造成伤害
        double reach = 10.0;
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(reach));
        
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                new net.minecraft.world.phys.AABB(start, end).inflate(1.0),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 1.1f);
                
                // 击退敌人
                Vec3 knockback = direction.scale(2.0);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            });
    }

    @Override
    public String getRiderName() {
        return "Fourze";
    }

    @Override
    public String getActivationSoundName() {
        return "Rider Rocket Drill Kick!";
    }

    @Override
    public float getAttackDamage() {
        return 49.0f; // 普通骑士 - Fourze拥有宇宙能量和多种开关，伤害略高于基础值
    }

    @Override
    public float getEffectRange() {
        return 15.0f;
    }
}
