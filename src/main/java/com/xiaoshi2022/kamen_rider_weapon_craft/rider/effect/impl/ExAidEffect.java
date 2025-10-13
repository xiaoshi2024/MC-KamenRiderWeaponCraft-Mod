package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;

public class ExAidEffect implements HeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Critical Strike攻击，造成伤害并给予玩家生命回复效果
            // 1. 对前方敌人造成伤害
            double reach = 8.0;
            Vec3 start = player.getEyePosition(1.0f);
            Vec3 end = start.add(direction.scale(reach));
            
            net.minecraft.world.phys.HitResult result = player.pick(reach, 0.0f, false);
            
            if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
                Entity entity = entityHitResult.getEntity();
                if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                    // 游戏风格的暴击伤害，有几率双倍伤害
                    float damage = getAttackDamage();
                    if (level.random.nextFloat() < 0.3) {
                        damage *= 2.0f;
                    }
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), damage);
                }
            };
            
            // 2. 给予玩家生命回复和速度提升效果
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 2));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }

    @Override
    public String getRiderName() {
        return "Ex-Aid";
    }

    @Override
    public String getActivationSoundName() {
        return "Critical Strike!";
    }

    @Override
    public float getAttackDamage() {
        return 52.0f; // 普通骑士 - Ex-Aid作为游戏主题骑士，拥有多种等级形态，伤害较高
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
}
