package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OOOEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            Vec3 normalizedDirection = direction != null && direction.lengthSqr() > 0 ?
                                      direction.normalize() : shooter.getLookAngle().normalize();

            executeFullPowerAttack(level, shooter);
            executeCellMedalSlash(level, shooter, normalizedDirection);

            if (shooter instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
            }
        }
    }

    private void executeFullPowerAttack(Level level, LivingEntity shooter) {
        level.getEntitiesOfClass(LivingEntity.class,
                shooter.getBoundingBox().inflate(7.0),
                entity -> entity != shooter && entity.isAlive())
            .forEach(entity -> {
                if (shooter instanceof Player) {
                    entity.hurt(level.damageSources().playerAttack((Player) shooter), getAttackDamage() * 0.7f);
                } else {
                    entity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage() * 0.7f);
                }
                if (!(entity instanceof Player)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 150, 0));
                }
            });
    }

    private void executeCellMedalSlash(Level level, LivingEntity shooter, Vec3 direction) {
        float attackDamage = getAttackDamage() * 1.5f;
        LivingEntity target = findNearestTargetInDirection(level, shooter, direction, 10.0);

        if (target != null) {
            if (shooter instanceof Player) {
                target.hurt(level.damageSources().playerAttack((Player) shooter), attackDamage);
            } else {
                target.hurt(level.damageSources().mobAttack(shooter), attackDamage);
            }
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
        }
    }

    private LivingEntity findNearestTargetInDirection(Level level, LivingEntity shooter, Vec3 direction, double maxRange) {
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(maxRange));
        AABB searchBox = new AABB(start, end).inflate(2.0);

        return level.getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity != shooter && entity.isAlive())
                .stream()
                .min((e1, e2) -> {
                    double d1 = e1.distanceToSqr(start);
                    double d2 = e2.distanceToSqr(start);
                    return Double.compare(d1, d2);
                })
                .orElse(null);
    }

    @Override
    public String getRiderName() {
        return "OOO";
    }

    @Override
    public String getActivationSoundName() {
        return "Scanning Charge!";
    }

    @Override
    public float getAttackDamage() {
        return 51.0f;
    }

    @Override
    public float getEffectRange() {
        return 14.0f;
    }
}