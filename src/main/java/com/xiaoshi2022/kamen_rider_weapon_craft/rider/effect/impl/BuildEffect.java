package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.build.BuildRiderEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BuildEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：使用BuildRiderEntity生成特效实体，有几率触发
            BuildRiderEntity.trySpawnEffect(level, player, direction, getAttackDamage());
            
            // 为玩家添加气泡兔坦形态的增益效果
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 200, 1));
            
            // 增加伤害抵抗（坦克防御特性）
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 200, 1));
            
            // 增加攻击力（气泡兔坦爆发力量）
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 100, 0));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
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
        return 51.0f; // Build骑士，伤害提升到足以对抗强大敌人
    }

    @Override
    public float getEffectRange() {
        return 5.0f;
    }
    
    @Override
    public double getEnergyCost() {
        return 20.0; // Build骑士技能消耗20点骑士能量
    }
}