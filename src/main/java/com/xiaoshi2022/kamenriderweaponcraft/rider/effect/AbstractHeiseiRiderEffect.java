package com.xiaoshi2022.kamenriderweaponcraft.rider.effect;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractHeiseiRiderEffect implements HeiseiRiderEffect {
    
    @Override
    public double getEnergyCost() {
        return 20.0;
    }
    
    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (shooter instanceof Player player) {
            executePlayerSpecialAttack(level, player, direction);
        } else {
            executeNonPlayerSpecialAttack(level, shooter, direction);
        }
    }
    
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            HeiseiRiderEffectManager.playSelectionSound(level, shooter, getRiderName());
            HeiseiRiderEffectManager.playAttackSound(level, shooter, getRiderName());
            
            Vec3 attackDirection = direction != null && direction.lengthSqr() > 0 ? 
                                   direction.normalize() : shooter.getLookAngle();
            
            float attackRange = getEffectRange();
            float width = attackRange / 2;
            
            Vec3 start = shooter.position().add(0, shooter.getEyeHeight() * 0.5, 0);
            Vec3 end = start.add(attackDirection.scale(attackRange));
            
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(
                Math.min(start.x, end.x) - width,
                Math.min(start.y, end.y) - 1,
                Math.min(start.z, end.z) - width,
                Math.max(start.x, end.x) + width,
                Math.max(start.y, end.y) + 1,
                Math.max(start.z, end.z) + width
            );
            
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, attackBox,
                    entity -> entity != shooter && entity != null && entity.isAlive())) {
                Vec3 targetRelative = target.position().subtract(start);
                if (targetRelative.dot(attackDirection) > 0) {
                    DamageSource damageSource = level.damageSources().mobAttack(shooter);
                    target.hurt(damageSource, getAttackDamage());
                }
            }
            
            applyVisualEffects(level, shooter, attackDirection);
        }
    }
    
    protected void applyVisualEffects(Level level, LivingEntity shooter, Vec3 direction) {
        if (level.isClientSide) {
            for (int i = 0; i < 20; i++) {
                double offsetX = random.nextGaussian() * 0.5;
                double offsetY = random.nextGaussian() * 0.5 + 1.0;
                double offsetZ = random.nextGaussian() * 0.5;
                
                Vec3 particlePos = shooter.position().add(offsetX, offsetY, offsetZ);
                Vec3 particleMotion = direction.scale(0.5).add(
                    random.nextGaussian() * 0.1,
                    random.nextGaussian() * 0.1,
                    random.nextGaussian() * 0.1
                );
                
                level.addParticle(
                    net.minecraft.core.particles.ParticleTypes.CLOUD,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    particleMotion.x,
                    particleMotion.y,
                    particleMotion.z
                );
            }
        } else {
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                net.minecraft.sounds.SoundEvents.FIREWORK_ROCKET_BLAST, 
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
        }
    }
    
    private static final java.util.Random random = new java.util.Random();
    
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            HeiseiRiderEffectManager.playSelectionSound(level, player, getRiderName());
            HeiseiRiderEffectManager.playAttackSound(level, player, getRiderName());
            
            Vec3 attackDirection = direction != null && direction.lengthSqr() > 0 ? 
                                   direction.normalize() : player.getLookAngle();
            
            float attackRange = getEffectRange();
            float width = attackRange / 2;
            
            Vec3 start = player.position().add(0, player.getEyeHeight() * 0.5, 0);
            Vec3 end = start.add(attackDirection.scale(attackRange));
            
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(
                Math.min(start.x, end.x) - width,
                Math.min(start.y, end.y) - 1,
                Math.min(start.z, end.z) - width,
                Math.max(start.x, end.x) + width,
                Math.max(start.y, end.y) + 1,
                Math.max(start.z, end.z) + width
            );
            
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, attackBox,
                    entity -> entity != player && entity != null && entity.isAlive())) {
                Vec3 targetRelative = target.position().subtract(start);
                if (targetRelative.dot(attackDirection) > 0) {
                    DamageSource damageSource = level.damageSources().playerAttack(player);
                    target.hurt(damageSource, getAttackDamage());
                }
            }
            
            applyVisualEffects(level, player, attackDirection);
        }
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