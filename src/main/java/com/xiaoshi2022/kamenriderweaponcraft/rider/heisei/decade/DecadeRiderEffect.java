package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.decade;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DecadeRiderEffect {
    
    public static void spawnDimensionKickEffect(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        DecadeRiderEntity.trySpawnEffect(level, owner, direction, attackDamage);
    }
    
    public static void spawnDimensionKickEffectByOwnerDirection(Level level, LivingEntity owner, float attackDamage) {
        Vec3 lookVector = owner.getViewVector(1.0F);
        spawnDimensionKickEffect(level, owner, lookVector, attackDamage);
    }
    
    public static void spawnCompleteFormDimensionKick(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        for (int i = 0; i < 3; i++) {
            final float damage = attackDamage * (1.0f + i * 0.1f);
            DecadeRiderEntity.trySpawnEffect(level, owner, direction.add(0, i * 0.1, 0), damage);
        }
    }
}