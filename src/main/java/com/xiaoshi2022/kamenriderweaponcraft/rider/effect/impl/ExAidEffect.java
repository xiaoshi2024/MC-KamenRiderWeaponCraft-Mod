package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.exaid.ExAidSlashEffectEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ExAidEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            if (shooter instanceof Player player) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 400, 1));

                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 400, 1));

                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.REGENERATION, 200, 2));
            }

            double reach = 8.0;
            Vec3 start = shooter.getEyePosition(1.0f);
            Vec3 end = start.add(direction.scale(reach));

            Entity hitEntity = getTargetEntity(level, shooter, start, end, reach);

            if (hitEntity instanceof LivingEntity livingEntity && hitEntity != shooter) {
                float initialDamage = getAttackDamage() * 0.5f;

                if (shooter instanceof Player player) {
                    livingEntity.hurt(level.damageSources().playerAttack(player), initialDamage);
                } else {
                    livingEntity.hurt(level.damageSources().mobAttack(shooter), initialDamage);
                }

                ExAidSlashEffectEntity.spawnEffectOnTarget(level, shooter, hitEntity);
            } else {
                ExAidSlashEffectEntity.spawnEffect(level, shooter, direction);
            }
        }
    }

    private Entity getTargetEntity(Level level, LivingEntity shooter, Vec3 start, Vec3 end, double reach) {
        net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(start, end).inflate(1.0);

        return level.getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity != shooter && entity.isAlive())
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getRiderName() {
        return "Ex-Aid";
    }

    @Override
    public String getActivationSoundName() {
        return "Hyper Critical Sparking!";
    }

    @Override
    public float getAttackDamage() {
        return 55.0f;
    }

    @Override
    public float getEffectRange() {
        return 10.0f;
    }
}
