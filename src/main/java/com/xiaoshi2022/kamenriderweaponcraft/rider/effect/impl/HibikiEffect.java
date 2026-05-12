package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.hibiki.HibikiDrumEffectEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HibikiEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            Vec3 normalizedDirection = direction != null && direction.lengthSqr() > 0 ?
                                      direction.normalize() : shooter.getLookAngle().normalize();

            LivingEntity target = findNearestTarget(shooter, getEffectRange());
            if (target != null) {
                HibikiDrumEffectEntity.spawnEffect(level, shooter, target);
                if (shooter instanceof Player) {
                    target.hurt(level.damageSources().playerAttack((Player) shooter), getAttackDamage());
                } else {
                    target.hurt(level.damageSources().mobAttack(shooter), getAttackDamage());
                }
            }

            if (shooter instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
            }
        }
    }

    private LivingEntity findNearestTarget(LivingEntity shooter, double range) {
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(shooter.getLookAngle().scale(range));
        net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(start, end).inflate(range / 2, 2.0, range / 2);

        return shooter.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity != shooter && entity.isAlive())
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getRiderName() {
        return "Hibiki";
    }

    @Override
    public String getActivationSoundName() {
        return "Oni no Koe!";
    }

    @Override
    public float getAttackDamage() {
        return 47.0f;
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
}