package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.gaim.GaimLockSeedEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GaimEffect extends AbstractHeiseiRiderEffect {

    private enum LockSeed {
        Orange,
        Pineapple,
        Banana,
        Melon
    }

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            Vec3 normalizedDirection = direction != null && direction.lengthSqr() > 0 ?
                                      direction.normalize() : player.getLookAngle().normalize();

            LockSeed selectedSeed = getWeightedRandomLockSeed(level);

            spawnLockSeedEntity(level, player, normalizedDirection, selectedSeed);

            switch (selectedSeed) {
                case Orange:
                    executeOrangeLockSeed(level, player, normalizedDirection);
                    break;
                case Pineapple:
                    executePineappleLockSeed(level, player, normalizedDirection);
                    break;
                case Banana:
                    executeBananaLockSeed(level, player, normalizedDirection);
                    break;
                case Melon:
                    executeMelonLockSeed(level, player, normalizedDirection);
                    break;
            }

            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
        }
    }

    private void spawnLockSeedEntity(Level level, Player player, Vec3 direction, LockSeed lockSeed) {
        Vec3 spawnPos = player.getEyePosition().add(direction.scale(1.0));

        if (lockSeed != LockSeed.Pineapple) {
            GaimLockSeedEntity lockSeedEntity = new GaimLockSeedEntity(
                    level,
                    player,
                    spawnPos,
                    direction,
                    lockSeed.name().toUpperCase(),
                    getAttackDamage()
            );
            level.addFreshEntity(lockSeedEntity);
        }
    }

    private LockSeed getWeightedRandomLockSeed(Level level) {
        int rand = level.random.nextInt(100);

        if (rand < 40) {
            return LockSeed.Orange;
        } else if (rand < 65) {
            return LockSeed.Banana;
        } else if (rand < 85) {
            return LockSeed.Melon;
        } else {
            return LockSeed.Pineapple;
        }
    }

    private void executeOrangeLockSeed(Level level, Player player, Vec3 direction) {
        for (int i = 0; i < 5; i++) {
            double distance = 2.0 + (i * 0.5);
            Vec3 targetPos = player.getEyePosition(1.0f).add(direction.scale(distance));
            level.getEntities(player, new net.minecraft.world.phys.AABB(targetPos, targetPos).inflate(1.0))
                .forEach(entity -> {
                    if (entity instanceof LivingEntity livingEntity && entity != player) {
                        livingEntity.hurt(level.damageSources().playerAttack(player), getAttackDamage() * 0.4f);
                    }
                });
        }

        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
    }

    private void executePineappleLockSeed(Level level, Player player, Vec3 direction) {
        for (int i = 0; i < 5; i++) {
            float spreadAngle = (level.random.nextFloat() - 0.5f) * 0.4f;
            Vec3 spreadDirection = new Vec3(
                direction.x + (level.random.nextFloat() - 0.5f) * 0.3f,
                direction.y + spreadAngle,
                direction.z + (level.random.nextFloat() - 0.5f) * 0.3f
            ).normalize();

            Vec3 spawnPos = player.getEyePosition().add(spreadDirection.scale(1.0));

            GaimLockSeedEntity lockSeedEntity = new GaimLockSeedEntity(
                    level,
                    player,
                    spawnPos,
                    spreadDirection,
                    LockSeed.Pineapple.name().toUpperCase(),
                    getAttackDamage() * 0.6f
            );

            level.addFreshEntity(lockSeedEntity);
        }
    }

    private void executeBananaLockSeed(Level level, Player player, Vec3 direction) {
        Vec3 slideVelocity = direction.scale(3.0);
        player.setDeltaMovement(slideVelocity);

        for (int i = 0; i < 5; i++) {
            double distance = 1.0 + (i * 0.75);
            Vec3 pathPos = player.getEyePosition(1.0f).add(direction.scale(distance));

            level.getEntitiesOfClass(LivingEntity.class,
                    new net.minecraft.world.phys.AABB(pathPos, pathPos).inflate(1.5),
                    entity -> entity != player)
                .forEach(entity -> {
                    if (entity instanceof LivingEntity livingEntity) {
                        livingEntity.hurt(level.damageSources().playerAttack(player), getAttackDamage() * 0.8f);
                    }
                });
        }
    }

    private void executeMelonLockSeed(Level level, Player player, Vec3 direction) {
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 255));
    }

    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            Vec3 normalizedDirection = direction != null && direction.lengthSqr() > 0 ?
                                      direction.normalize() : shooter.getLookAngle().normalize();

            LockSeed selectedSeed = getWeightedRandomLockSeed(level);
            spawnLockSeedEntityForMob(level, shooter, normalizedDirection, selectedSeed);

            shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
        }
    }

    private void spawnLockSeedEntityForMob(Level level, LivingEntity shooter, Vec3 direction, LockSeed lockSeed) {
        Vec3 spawnPos = shooter.getEyePosition().add(direction.scale(1.0));

        if (lockSeed != LockSeed.Pineapple) {
            GaimLockSeedEntity lockSeedEntity = new GaimLockSeedEntity(
                    level,
                    shooter,
                    spawnPos,
                    direction,
                    lockSeed.name().toUpperCase(),
                    getAttackDamage()
            );
            level.addFreshEntity(lockSeedEntity);
        }
    }

    @Override
    public String getRiderName() {
        return "Gaim";
    }

    @Override
    public String getActivationSoundName() {
        return "Kachidoki Arms!";
    }

    @Override
    public float getAttackDamage() {
        return 52.0f;
    }

    @Override
    public float getEffectRange() {
        return 12.0f;
    }
}
