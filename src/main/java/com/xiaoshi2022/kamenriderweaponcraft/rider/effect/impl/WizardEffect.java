package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.wizard.WizardRiderEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WizardEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            Vec3 normalizedDirection = direction != null && direction.lengthSqr() > 0 ?
                                      direction.normalize() : shooter.getLookAngle().normalize();

            WizardRiderEntity.DragonMagicType selectedDragon = WizardRiderEntity.DragonMagicType.values()[level.random.nextInt(WizardRiderEntity.DragonMagicType.values().length)];

            WizardRiderEntity.trySpawnEffect(level, shooter, normalizedDirection, getAttackDamage(), selectedDragon);

            switch (selectedDragon) {
                case FlameDragon:
                    executeFlameMagic(level, shooter, normalizedDirection);
                    break;
                case WaterDragon:
                    executeWaterMagic(level, shooter, normalizedDirection);
                    break;
                case HurricaneDragon:
                    executeHurricaneMagic(level, shooter, normalizedDirection);
                    break;
                case LandDragon:
                    executeLandMagic(level, shooter, normalizedDirection);
                    break;
            }

            if (shooter instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            }
        }
    }

    private void executeFlameMagic(Level level, LivingEntity shooter, Vec3 direction) {
        double range = 20.0;
        net.minecraft.world.phys.HitResult result = shooter.pick(range, 0.0f, false);

        if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity && entityHitResult.getEntity() != shooter) {
                Vec3 toEntity = livingEntity.position().subtract(shooter.position()).normalize();
                if (toEntity.dot(direction) > 0.5) {
                    if (shooter instanceof Player) {
                        livingEntity.hurt(level.damageSources().playerAttack((Player) shooter), getAttackDamage() * 1.2f);
                    } else {
                        livingEntity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage() * 1.2f);
                    }
                    livingEntity.setRemainingFireTicks(200);
                    level.explode(shooter, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                        getEffectRange(), Level.ExplosionInteraction.MOB);
                }
            }
        }

        if (shooter instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 1));
        }
    }

    private void executeWaterMagic(Level level, LivingEntity shooter, Vec3 direction) {
        double range = 12.0;
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(range));
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(range / 3, 3.0, range / 3);

        level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
            if (entity == shooter) return false;
            Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
            return toEntity.dot(direction) > 0.5;
        }).forEach(entity -> {
            if (shooter instanceof Player) {
                entity.hurt(level.damageSources().playerAttack((Player) shooter), getAttackDamage() * 0.4f);
            } else {
                entity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage() * 0.4f);
            }
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 150, 3));
            if (entity.isOnFire()) {
                entity.clearFire();
            }
        });

        if (shooter instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 300, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
        }
    }

    private void executeHurricaneMagic(Level level, LivingEntity shooter, Vec3 direction) {
        double range = getEffectRange() * 2.0;
        Vec3 start = shooter.position();
        Vec3 end = start.add(direction.scale(range));
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(range / 2, 4.0, range / 2);

        level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
            if (entity == shooter) return false;
            Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
            return toEntity.dot(direction) > 0.5;
        }).forEach(entity -> {
            if (shooter instanceof Player) {
                entity.hurt(level.damageSources().playerAttack((Player) shooter), getAttackDamage() * 0.7f);
            } else {
                entity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage() * 0.7f);
            }
            Vec3 knockback = entity.position().subtract(shooter.position()).normalize().scale(4.0);
            entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 100, 2));
        });

        if (shooter instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 300, 0));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2));
        }
    }

    private void executeLandMagic(Level level, LivingEntity shooter, Vec3 direction) {
        for (int i = 0; i < 5; i++) {
            Vec3 pos = shooter.getEyePosition(1.0f).add(direction.scale(2.0 + i));
            net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos((int)pos.x, (int)pos.y, (int)pos.z);
            if (level.isEmptyBlock(blockPos)) {
                if (level.random.nextBoolean()) {
                    level.setBlockAndUpdate(blockPos, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                } else {
                    level.setBlockAndUpdate(blockPos, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
                }
            }
        }

        double range = getEffectRange() * 1.5;
        Vec3 start = shooter.position();
        Vec3 end = start.add(direction.scale(range));
        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(range / 2, 3.0, range / 2);

        level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
            if (entity == shooter) return false;
            Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
            return toEntity.dot(direction) > 0.5;
        }).forEach(entity -> {
            if (shooter instanceof Player) {
                entity.hurt(level.damageSources().playerAttack((Player) shooter), getAttackDamage() * 0.8f);
            } else {
                entity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage() * 0.8f);
            }
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 4));
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 2));
        });

        if (shooter instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 2));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2));
        }
    }

    @Override
    public String getRiderName() {
        return "Wizard";
    }

    @Override
    public String getActivationSoundName() {
        return "Dragon Form!";
    }

    @Override
    public float getAttackDamage() {
        return 70.0f;
    }

    @Override
    public float getEffectRange() {
        return 12.0f;
    }
}