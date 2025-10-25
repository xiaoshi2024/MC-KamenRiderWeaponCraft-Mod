package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.decade.DecadeRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DecadeEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Dimension Kick攻击，使用专门的特效实体
            DecadeRiderEffect.spawnDimensionKickEffect(level, player, direction, getAttackDamage());
            
            // 给予玩家抗性效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
            
            // 有几率复制附近一个骑士的能力（这里简化实现）
            if (level.random.nextFloat() < 0.2) {
                // 复制能力的逻辑可以后续扩展
            }
        }
    }

    @Override
    public String getRiderName() {
        return "Decade";
    }

    @Override
    public String getActivationSoundName() {
        return "Dimension Kick!";
    }

    @Override
    public float getAttackDamage() {
        return 52.0f; // 高级骑士 - Decade作为骑士破坏者，拥有极高的伤害能力
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
}
