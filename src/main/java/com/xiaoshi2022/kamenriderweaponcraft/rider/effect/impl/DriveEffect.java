package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.drive.DriveRiderEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DriveEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            Vec3 normalizedDirection = (direction != null && direction.lengthSqr() > 0) ?
                                      direction.normalize() : shooter.getLookAngle().normalize();

            if (shooter instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 3));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));

                Vec3 velocity = normalizedDirection.scale(3.0);
                player.setDeltaMovement(velocity);
                player.fallDistance = 0.0f;

                DriveRiderEntity.trySpawnEffect(level, player, normalizedDirection, getAttackDamage());
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.HORSE_GALLOP, SoundSource.PLAYERS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            } else {
                DriveRiderEntity.trySpawnEffect(level, shooter, normalizedDirection, getAttackDamage());
                level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.HORSE_GALLOP, SoundSource.HOSTILE, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
                shooter.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2));
            }

            double range = 10.0;
            Vec3 start = shooter.position().add(0, shooter.getEyeHeight() * 0.5, 0);
            Vec3 end = start.add(normalizedDirection.scale(range));
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(range / 2, 2.0, range / 2);

            level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
                if (entity == shooter) return false;
                Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
                return toEntity.dot(normalizedDirection) > 0.5;
            }).forEach(entity -> {
                if (shooter instanceof Player) {
                    entity.hurt(level.damageSources().playerAttack((Player) shooter), getAttackDamage() * 0.5f);
                } else {
                    entity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage() * 0.5f);
                }
            });
        }
    }

    @Override
    public String getRiderName() {
        return "Drive";
    }

    @Override
    public String getActivationSoundName() {
        return "SpeeDemon!";
    }

    @Override
    public float getAttackDamage() {
        return 50.0f;
    }

    @Override
    public float getEffectRange() {
        return 15.0f;
    }
}