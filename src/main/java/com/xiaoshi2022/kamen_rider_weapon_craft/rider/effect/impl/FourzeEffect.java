package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.fourze.FourzeRocketEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FourzeEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Rider Rocket Attack，召唤宇宙能量和火箭炮
            // 1. 给予玩家跳跃增强和缓降效果，模拟太空行走
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 300, 2));
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0));
            
            // 2. 发射3枚追踪火箭炮
            launchRocketAttack(level, player, direction);
        }
    }
    
    private void launchRocketAttack(Level level, Player player, Vec3 direction) {
        // 调用FourzeRocketEntity的静态方法发射3枚追踪火箭炮
        // 每枚火箭的伤害为总伤害的1/3，这样3枚火箭的总伤害与原来的攻击相当
        FourzeRocketEntity.spawnRockets(level, player, direction, getAttackDamage() / 3.0f);
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
        return 55.0f; // 提升Fourze火箭伤害，充分展示宇宙能量的威力
    }

    @Override
    public float getEffectRange() {
        return 15.0f;
    }
}
