package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class KuugaEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：生成Kuuga特效实体，扑向敌人并爆炸
            com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kuuga.KuugaRiderEntity.trySpawnEffect(level, player, direction, getAttackDamage());
            
            // 给予玩家全能形态相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
        }
    }

    @Override
    public String getRiderName() {
        return "Kuuga";
    }

    @Override
    public String getActivationSoundName() {
        return "Rising Mighty Kick!";
    }

    @Override
    public float getAttackDamage() {
        return 45.0f; // 基础骑士 - 空我作为平成第一位骑士，伤害设置为基础值
    }

    @Override
    public float getEffectRange() {
        return 15.0f;
    }
    
    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 为非玩家实体（如僵尸）生成Kuuga特效实体
            com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kuuga.KuugaRiderEntity.trySpawnEffect(level, shooter, direction, getAttackDamage());
            
            // 给予实体强化效果
            shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
        }
    }
}
