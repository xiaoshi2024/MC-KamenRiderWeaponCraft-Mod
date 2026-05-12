package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
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

            if (shooter instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 300, 2));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0));
            }

            launchRocketAttack(level, shooter, normalizedDirection);
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            shooter.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 0));
        }
    }

    private void launchRocketAttack(Level level, LivingEntity shooter, Vec3 direction) {
        float damage = getAttackDamage() / 3.0f;
        for (int i = 0; i < 3; i++) {
            float spread = (level.random.nextFloat() - 0.5f) * 0.3f;
            Vec3 spreadDir = new Vec3(
                direction.x + spread,
                direction.y + spread * 0.5f,
                direction.z + spread
            ).normalize();

            double range = 15.0;
            Vec3 start = shooter.getEyePosition(1.0f);
            Vec3 end = start.add(spreadDir.scale(range));
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(2.0);

            level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
                if (entity == shooter) return false;
                Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
                return toEntity.dot(direction) > 0.5;
            }).forEach(livingEntity -> {
                if (shooter instanceof Player) {
                    livingEntity.hurt(level.damageSources().playerAttack((Player) shooter), damage);
                } else {
                    livingEntity.hurt(level.damageSources().mobAttack(shooter), damage);
                }
                livingEntity.setRemainingFireTicks(60);
            });
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