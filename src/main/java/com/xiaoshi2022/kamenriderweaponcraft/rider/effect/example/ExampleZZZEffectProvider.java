package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.example;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.ExternalRiderEffectProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ExampleZZZEffectProvider implements ExternalRiderEffectProvider {

    public static final String RIDER_ID = "zzz";
    public static final String MOD_ID = "zzz_mod";

    @Override
    public String getExternalRiderId() {
        return RIDER_ID;
    }

    @Override
    public String getExternalRiderName() {
        return "ZZZ";
    }

    @Override
    public float getAttackDamage() {
        return 45.0f;
    }

    @Override
    public float getEffectRange() {
        return 6.0f;
    }

    @Override
    public double getEnergyCost() {
        return 25.0;
    }

    @Override
    public String getActivationSoundName() {
        return "Dreaming Strike!";
    }

    @Override
    public void executeSkill(Level level, LivingEntity shooter, Vec3 direction) {
        if (level.isClientSide) {
            spawnDreamParticles(level, shooter, direction);
            return;
        }

        Vec3 normalizedDirection = direction.normalize();
        double range = getEffectRange();
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(normalizedDirection.scale(range));

        net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(2.0, 2.0, 2.0);

        level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
            if (entity == shooter) return false;
            Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
            double dotProduct = toEntity.dot(normalizedDirection);
            return dotProduct > 0.5;
        }).forEach(target -> {
            if (shooter instanceof net.minecraft.world.entity.player.Player player) {
                target.hurt(level.damageSources().playerAttack(player), getAttackDamage());
            } else {
                target.hurt(level.damageSources().mobAttack(shooter), getAttackDamage());
            }

            if (target instanceof Monster) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
            }

            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));

            spawnHitParticles(level, target);
        });

        if (shooter instanceof net.minecraft.world.entity.player.Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200, 1));
        }

        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.8F, 1.2F);

        spawnDreamWave(level, shooter, normalizedDirection);
    }

    private void spawnDreamParticles(Level level, LivingEntity shooter, Vec3 direction) {
        for (int i = 0; i < 30; i++) {
            double offsetX = (level.random.nextGaussian() * 1.5);
            double offsetY = level.random.nextGaussian() * 1.5 + 1.0;
            double offsetZ = (level.random.nextGaussian() * 1.5);

            Vec3 particlePos = shooter.position().add(offsetX, offsetY, offsetZ);
            Vec3 particleMotion = direction.scale(0.3).add(
                    level.random.nextGaussian() * 0.1,
                    level.random.nextGaussian() * 0.1,
                    level.random.nextGaussian() * 0.1
            );

            level.addParticle(ParticleTypes.WARPED_SPORE, particlePos.x, particlePos.y, particlePos.z,
                    particleMotion.x, particleMotion.y, particleMotion.z);
            level.addParticle(ParticleTypes.SOUL, particlePos.x, particlePos.y, particlePos.z,
                    particleMotion.x * 0.5, particleMotion.y * 0.5, particleMotion.z * 0.5);
        }
    }

    private void spawnHitParticles(Level level, Entity target) {
        for (int i = 0; i < 15; i++) {
            double offsetX = (level.random.nextGaussian() * 0.5);
            double offsetY = level.random.nextGaussian() * 0.5 + 0.5;
            double offsetZ = (level.random.nextGaussian() * 0.5);

            Vec3 particlePos = target.position().add(offsetX, offsetY, offsetZ);

            level.addParticle(ParticleTypes.SOUL, particlePos.x, particlePos.y, particlePos.z,
                    level.random.nextGaussian() * 0.2, level.random.nextGaussian() * 0.2, level.random.nextGaussian() * 0.2);
            level.addParticle(ParticleTypes.END_ROD, particlePos.x, particlePos.y, particlePos.z,
                    level.random.nextGaussian() * 0.1, 0.1, level.random.nextGaussian() * 0.1);
        }
    }

    private void spawnDreamWave(Level level, LivingEntity shooter, Vec3 direction) {
        Vec3 start = shooter.getEyePosition(1.0f);

        for (int i = 0; i < 20; i++) {
            double progress = (double) i / 20.0;
            double distance = progress * getEffectRange();

            Vec3 wavePos = start.add(direction.scale(distance));
            wavePos = wavePos.add(
                    (level.random.nextGaussian() * 0.8),
                    (level.random.nextGaussian() * 0.8),
                    (level.random.nextGaussian() * 0.8)
            );

            level.addParticle(ParticleTypes.WARPED_SPORE, wavePos.x, wavePos.y, wavePos.z,
                    0.0, 0.05, 0.0);
        }
    }

    @Nullable
    @Override
    public ResourceLocation getExternalModelLocation() {
        return null;
    }

    @Nullable
    @Override
    public Supplier<ResourceLocation> getExternalAnimController() {
        return null;
    }

    @Override
    public boolean isExternal() {
        return true;
    }

    @Override
    public boolean supportsScrambleMode() {
        return true;
    }

    @Override
    public int getScrambleModeMaxLayers() {
        return 4;
    }
}