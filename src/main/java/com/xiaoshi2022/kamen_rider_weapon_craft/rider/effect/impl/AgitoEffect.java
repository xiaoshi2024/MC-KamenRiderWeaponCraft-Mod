package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AgitoEffect implements HeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Ground Flame攻击，造成范围伤害并给予玩家力量效果
            level.getEntities(player, player.getBoundingBox().inflate(getEffectRange()))
                .forEach(entity -> {
                    if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                        ((net.minecraft.world.entity.LivingEntity) entity).hurt(level.damageSources().playerAttack(player), getAttackDamage());
                        // 地面火焰效果：可以添加地面粒子或着火效果
                    }
                });
            
            // 给予玩家力量效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }

    @Override
    public String getRiderName() {
        return "Agito";
    }

    @Override
    public String getActivationSoundName() {
        return "Ground Flame!";
    }

    @Override
    public float getAttackDamage() {
        return 46.0f; // 普通骑士 - Agito作为平成第二位骑士，伤害提高以应对强大敌人
    }

    @Override
    public float getEffectRange() {
        return 6.0f;
    }
}
