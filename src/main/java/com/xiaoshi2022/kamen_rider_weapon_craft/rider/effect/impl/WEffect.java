package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.w.WTornadoEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 只保留CycloneJoker形态的攻击
            executeCycloneJoker(level, player, direction);
            
            // 给予玩家速度加成效果
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
            
            // 服务器端：生成龙卷风特效实体
            WTornadoEntity.trySpawnTornado(level, player, direction);
        }
    }
    
    private void executeCycloneJoker(Level level, Player player, Vec3 direction) {
        // 飓风王牌形态：高速移动和剑攻击
        // 给予玩家高速移动效果
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 3));
        
        // 向前方进行快速斩击
        double reach = 8.0;
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(reach));
        
        net.minecraft.world.phys.HitResult result = player.pick(reach, 0.0f, false);
        
        if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage());
                // 给予敌人短暂的缓速效果
                ((net.minecraft.world.entity.LivingEntity) entity).addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
            }
        }
    }

    @Override
    public String getRiderName() {
        return "W";
    }

    @Override
    public String getActivationSoundName() {
        return "Cyclone Joker!";
    }

    @Override
    public float getAttackDamage() {
        return 51.0f; // 高级骑士 - W是双人一体骑士，拥有强大力量
    }

    @Override
    public float getEffectRange() {
        return 7.0f;
    }
}
