package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DecadeEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Dimension Kick攻击，创建小型爆炸并给予玩家抗性效果
            // 1. 在玩家前方创建小型爆炸
            Vec3 targetPos = player.getEyePosition(1.0f).add(direction.scale(3.0));
            level.explode(player, targetPos.x, targetPos.y, targetPos.z, 
                getAttackDamage() / 5, Level.ExplosionInteraction.MOB); // 从/4改为/5
            
            // 2. 给予玩家抗性效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
            
            // 3. 有几率复制附近一个骑士的能力（这里简化实现）
            if (level.random.nextFloat() < 0.2) { // 从0.3降低到0.2
                // 复制能力的逻辑可以后续扩展
            }
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
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
