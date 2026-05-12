package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.ghost.GhostHeroicSoulEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GhostEffect extends AbstractHeiseiRiderEffect {

    private final Random random = new Random();
    private final List<String> usedHeroicSouls = new ArrayList<>();

    private enum HeroicSoul {
        MUSASHI("Musashi", 0xFF4500, 1.2f, true),
        EDISON("Edison", 0xFFFF00, 1.1f, false),
        NEWTON("Newton", 0x1E90FF, 1.0f, false);

        private final String name;
        private final int color;
        private final float damageMultiplier;
        private final boolean isFireDamage;

        HeroicSoul(String name, int color, float damageMultiplier, boolean isFireDamage) {
            this.name = name;
            this.color = color;
            this.damageMultiplier = damageMultiplier;
            this.isFireDamage = isFireDamage;
        }

        public String getName() { return name; }
        public int getColor() { return color; }
        public float getDamageMultiplier() { return damageMultiplier; }
        public boolean isFireDamage() { return isFireDamage; }
    }

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            List<HeroicSoul> availableSouls = new ArrayList<>();
            for (HeroicSoul soul : HeroicSoul.values()) {
                if (!usedHeroicSouls.contains(soul.name())) {
                    availableSouls.add(soul);
                }
            }

            if (availableSouls.isEmpty()) {
                usedHeroicSouls.clear();
                availableSouls.addAll(List.of(HeroicSoul.values()));
            }

            HeroicSoul selectedSoul = availableSouls.get(random.nextInt(availableSouls.size()));
            usedHeroicSouls.add(selectedSoul.name());

            spawnHeroicSoulEntity(level, shooter, direction, selectedSoul);

            executeHeroicSoulAbility(level, shooter, direction, selectedSoul);

            if (shooter instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0));
            }
        }
    }

    private void spawnHeroicSoulEntity(Level level, LivingEntity shooter, Vec3 direction, HeroicSoul soul) {
        if (!level.isClientSide) {
            GhostHeroicSoulEntity.trySpawnEffect(
                    level,
                    shooter,
                    direction,
                    soul.getColor(),
                    getAttackDamage() * soul.getDamageMultiplier(),
                    soul.isFireDamage(),
                    soul.name()
            );
        }
    }

    private void executeHeroicSoulAbility(Level level, LivingEntity shooter, Vec3 direction, HeroicSoul soul) {
        Vec3 normalizedDirection = direction != null && direction.lengthSqr() > 0 ?
                                  direction.normalize() : shooter.getLookAngle().normalize();

        switch (soul) {
            case MUSASHI:
                executeMusashiAbility(level, shooter, normalizedDirection, getAttackDamage() * soul.getDamageMultiplier());
                break;
            case EDISON:
                executeEdisonAbility(level, shooter, normalizedDirection, getAttackDamage() * soul.getDamageMultiplier());
                break;
            case NEWTON:
                executeNewtonAbility(level, shooter, normalizedDirection, getAttackDamage() * soul.getDamageMultiplier());
                break;
        }
    }

    private void executeMusashiAbility(Level level, LivingEntity shooter, Vec3 direction, float damage) {
        Vec3 start = shooter.getEyePosition(1.0f);
        float attackRange = getEffectRange();

        Vec3 rightDirection = direction.yRot((float) Math.toRadians(20));
        Vec3 leftDirection = direction.yRot((float) Math.toRadians(-20));

        Vec3 rightEnd = start.add(rightDirection.scale(attackRange));
        level.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(start, rightEnd).inflate(1.5),
                entity -> entity != shooter && entity.isAlive())
            .forEach(entity -> {
                Vec3 targetRelative = entity.position().subtract(start);
                if (targetRelative.normalize().dot(direction.normalize()) > 0.6) {
                    if (shooter instanceof Player) {
                        entity.hurt(level.damageSources().playerAttack((Player) shooter), damage);
                    } else {
                        entity.hurt(level.damageSources().mobAttack(shooter), damage);
                    }
                    entity.setRemainingFireTicks(160);
                }
            });

        Vec3 leftEnd = start.add(leftDirection.scale(attackRange));
        level.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(start, leftEnd).inflate(1.5),
                entity -> entity != shooter && entity.isAlive())
            .forEach(entity -> {
                Vec3 targetRelative = entity.position().subtract(start);
                if (targetRelative.normalize().dot(direction.normalize()) > 0.6) {
                    if (shooter instanceof Player) {
                        entity.hurt(level.damageSources().playerAttack((Player) shooter), damage);
                    } else {
                        entity.hurt(level.damageSources().mobAttack(shooter), damage);
                    }
                    entity.setRemainingFireTicks(160);
                }
            });

        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
    }

    private void executeEdisonAbility(Level level, LivingEntity shooter, Vec3 direction, float damage) {
        Vec3 start = shooter.getEyePosition(1.0f);
        float attackRange = getEffectRange();
        float width = attackRange / 2;
        Vec3 end = start.add(direction.scale(attackRange));

        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(
                Math.min(start.x, end.x) - width,
                Math.min(start.y, end.y) - 2,
                Math.min(start.z, end.z) - width,
                Math.max(start.x, end.x) + width,
                Math.max(start.y, end.y) + 2,
                Math.max(start.z, end.z) + width
        );

        level.getEntitiesOfClass(LivingEntity.class,
                attackBox,
                entity -> entity != shooter && entity.isAlive())
            .forEach(entity -> {
                Vec3 targetRelative = entity.position().subtract(start);
                if (targetRelative.normalize().dot(direction.normalize()) > 0.7) {
                    if (shooter instanceof Player) {
                        entity.hurt(level.damageSources().playerAttack((Player) shooter), damage);
                    } else {
                        entity.hurt(level.damageSources().mobAttack(shooter), damage);
                    }
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                    entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
                }
            });

        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
    }

    private void executeNewtonAbility(Level level, LivingEntity shooter, Vec3 direction, float damage) {
        Vec3 start = shooter.getEyePosition(1.0f);
        float attackRange = getEffectRange();
        float width = attackRange / 2;
        Vec3 center = start.add(direction.scale(Math.min(6.0, attackRange / 2)));

        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(
                center.x - width,
                center.y - 3,
                center.z - width,
                center.x + width,
                center.y + 3,
                center.z + width
        );

        level.getEntitiesOfClass(LivingEntity.class,
                attackBox,
                entity -> entity != shooter && entity.isAlive())
            .forEach(entity -> {
                Vec3 targetRelative = entity.position().subtract(start);
                if (targetRelative.normalize().dot(direction.normalize()) > 0.7) {
                    Vec3 attraction = center.subtract(entity.position()).normalize().scale(0.3);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(attraction));
                    if (shooter instanceof Player) {
                        entity.hurt(level.damageSources().playerAttack((Player) shooter), damage);
                    } else {
                        entity.hurt(level.damageSources().mobAttack(shooter), damage);
                    }
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                }
            });

        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
    }

    @Override
    public String getRiderName() {
        return "Ghost";
    }

    @Override
    public String getActivationSoundName() {
        return "Omega Drive!";
    }

    @Override
    public float getAttackDamage() {
        return 49.0f;
    }

    @Override
    public float getEffectRange() {
        return 15.0f;
    }
}
