package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.drive;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DriveRiderEffect {
    
    public static void spawnWheelEffect(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        DriveRiderEntity.trySpawnEffect(level, owner, direction, attackDamage);
    }
    
    public static void spawnWheelEffectByRotation(Level level, LivingEntity owner, float yRot, float xRot, float attackDamage) {
        Vec3 direction = calculateDirectionFromRotations(yRot, xRot);
        spawnWheelEffect(level, owner, direction, attackDamage);
    }
    
    public static void spawnWheelEffectByOwnerDirection(Level level, LivingEntity owner, float attackDamage) {
        Vec3 lookVector = owner.getViewVector(1.0F);
        spawnWheelEffect(level, owner, lookVector, attackDamage);
    }
    
    private static Vec3 calculateDirectionFromRotations(float yRot, float xRot) {
        float yRadians = (float)Math.toRadians(yRot);
        float xRadians = (float)Math.toRadians(xRot);
        
        float xComponent = -Mth.sin(yRadians) * Mth.cos(xRadians);
        float zComponent = Mth.cos(yRadians) * Mth.cos(xRadians);
        float yComponent = -Mth.sin(xRadians);
        
        return new Vec3(xComponent, yComponent, zComponent).normalize();
    }
    
    public static void spawnMultiWheelEffect(Level level, LivingEntity owner, Vec3 baseDirection, float attackDamage, int count, float spreadAngle) {
        if (count <= 1) {
            spawnWheelEffect(level, owner, baseDirection, attackDamage);
            return;
        }
        
        float angleIncrement = spreadAngle / (count - 1);
        float startAngle = -spreadAngle / 2;
        
        for (int i = 0; i < count; i++) {
            float currentAngle = startAngle + i * angleIncrement;
            Vec3 direction = rotateVectorAroundY(baseDirection, currentAngle);
            spawnWheelEffect(level, owner, direction, attackDamage);
        }
    }
    
    private static Vec3 rotateVectorAroundY(Vec3 vector, float angle) {
        double radians = Math.toRadians(angle);
        
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        
        double x = vector.x * cos - vector.z * sin;
        double z = vector.x * sin + vector.z * cos;
        
        return new Vec3(x, vector.y, z).normalize();
    }
}