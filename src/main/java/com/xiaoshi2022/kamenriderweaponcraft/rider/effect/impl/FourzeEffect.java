package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.fourze.FourzeRocketEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FourzeEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            Vec3 normalizedDirection = direction != null && direction.lengthSqr() > 0 ?
                                      direction.normalize() : shooter.getLookAngle().normalize();

            // 生成火箭实体
            FourzeRocketEntity.spawnRockets(level, shooter, normalizedDirection, getAttackDamage());

            if (shooter instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 300, 2));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0));
            }

            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            shooter.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 0));
        }
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
        return 55.0f;
    }

    @Override
    public float getEffectRange() {
        return 15.0f;
    }
}