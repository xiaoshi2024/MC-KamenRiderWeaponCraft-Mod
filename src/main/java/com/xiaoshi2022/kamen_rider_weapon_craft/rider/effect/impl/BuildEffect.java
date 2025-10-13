package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.build.BuildRiderEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;

public class BuildEffect implements HeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：使用BuildRiderEntity生成特效实体，有几率触发
            BuildRiderEntity.trySpawnEffect(level, player, direction, getAttackDamage());
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
}