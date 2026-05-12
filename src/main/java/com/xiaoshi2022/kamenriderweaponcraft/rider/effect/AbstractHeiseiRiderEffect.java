package com.xiaoshi2022.kamenriderweaponcraft.rider.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractHeiseiRiderEffect implements HeiseiRiderEffect {

    // 提供默认实现，匹配接口签名
    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        float damage = getAttackDamage();
        float range = getEffectRange();

        // 范围伤害效果
        level.getEntitiesOfClass(LivingEntity.class,
                        shooter.getBoundingBox().inflate(range),
                        entity -> entity != shooter && entity.isAlive())
                .forEach(entity -> {
                    entity.hurt(serverLevel.damageSources().mobAttack(shooter), damage);
                });

        // 基础粒子特效
        for (int i = 0; i < 10; i++) {
            double x = shooter.getX() + (level.random.nextDouble() - 0.5) * range;
            double y = shooter.getY() + level.random.nextDouble() * 2;
            double z = shooter.getZ() + (level.random.nextDouble() - 0.5) * range;
            serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, 1, 0, 0, 0, 0.1);
        }
    }

    @Override
    public double getEnergyCost() {
        return 20.0;
    }

    @Override
    public abstract String getRiderName();

    @Override
    public abstract String getActivationSoundName();

    @Override
    public abstract float getAttackDamage();

    @Override
    public abstract float getEffectRange();
}