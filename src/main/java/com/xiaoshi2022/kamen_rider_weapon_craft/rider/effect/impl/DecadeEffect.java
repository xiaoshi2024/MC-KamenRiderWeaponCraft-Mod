package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.decade.DecadeRiderEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DecadeEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        // 调用父类方法实现前方定向攻击
        super.executePlayerSpecialAttack(level, player, direction);
        
        if (!level.isClientSide) {
            // 为玩家生成Decade特效实体
            DecadeRiderEntity.trySpawnEffect(level, player, direction, getAttackDamage());
            
            // 给予玩家增益效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 0));
        }
    }
    
    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        // 调用父类方法实现前方定向攻击
        super.executeNonPlayerSpecialAttack(level, shooter, direction);
        
        if (!level.isClientSide) {
            // 为非玩家实体（如僵尸）生成Decade特效实体
            DecadeRiderEntity.trySpawnEffect(level, shooter, direction, getAttackDamage());
            
            // 添加音效
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            
            // 给予实体增益效果
            shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 1));
            shooter.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 0));
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
