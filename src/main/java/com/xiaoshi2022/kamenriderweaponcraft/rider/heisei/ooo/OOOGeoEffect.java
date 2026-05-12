package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.ooo;

import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.ooo.OOOGeoEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class OOOGeoEffect {
    
    public static void spawnCellMedalSlash(Level level, LivingEntity owner, Vec3 direction, float attackDamage, String coinType) {
        OOOGeoEntity.trySpawnEffect(level, owner, direction, attackDamage, coinType);
    }
    
    public static void spawnCellMedalSlashByRotation(Level level, LivingEntity owner, float yRot, float xRot, float attackDamage, String coinType) {
        Vec3 direction = calculateDirectionFromRotations(yRot, xRot);
        spawnCellMedalSlash(level, owner, direction, attackDamage, coinType);
    }
    
    public static void spawnCellMedalSlashByOwnerDirection(Level level, LivingEntity owner, float attackDamage, String coinType) {
        Vec3 lookVector = owner.getViewVector(1.0F);
        spawnCellMedalSlash(level, owner, lookVector, attackDamage, coinType);
    }
    
    private static Vec3 calculateDirectionFromRotations(float yRot, float xRot) {
        float yRadians = (float)Math.toRadians(yRot);
        float xRadians = (float)Math.toRadians(xRot);
        
        float xComponent = -Mth.sin(yRadians) * Mth.cos(xRadians);
        float zComponent = Mth.cos(yRadians) * Mth.cos(xRadians);
        float yComponent = -Mth.sin(xRadians);
        
        return new Vec3(xComponent, yComponent, zComponent).normalize();
    }
    
    public static final String PUTOTYRA = "putotyra";
    
    private static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2;
    
    public static void spawnCellMedalSwallow(Level level, LivingEntity owner, Vec3 baseDirection, float attackDamage, LivingEntity target) {
        LivingEntity finalTarget = target;
        if (finalTarget == null) {
            finalTarget = findTargetInDirection(owner, baseDirection, 15.0);
        }
        
        int count = 12;
        Vec3 playerPos = owner.getEyePosition(1.0F);
        
        spawnTrackingOrbPattern(level, owner, baseDirection, attackDamage, count, playerPos, finalTarget);
    }
    
    private static void spawnTrackingOrbPattern(Level level, LivingEntity owner, Vec3 baseDirection, float attackDamage, int count, Vec3 playerPos, LivingEntity target) {
        for (int i = 0; i < 12; i++) {
            double t = i * GOLDEN_RATIO;
            double inclination = Math.acos(1 - 2 * (i + 0.5) / 12);
            double azimuth = 2 * Math.PI * t;
            
            double x = Math.sin(inclination) * Math.cos(azimuth);
            double y = Math.sin(inclination) * Math.sin(azimuth);
            double z = Math.cos(inclination);
            
            Vec3 localOffset = new Vec3(x, y, z).normalize();
            
            Vec3 direction = calculateDirectionWithOffset(baseDirection, localOffset);
            
            direction = new Vec3(
                direction.x + (level.random.nextDouble() - 0.5) * 0.1,
                direction.y + (level.random.nextDouble() - 0.5) * 0.1,
                direction.z + (level.random.nextDouble() - 0.5) * 0.1
            ).normalize();
            
            OOOGeoEntity.trySpawnEffect(level, owner, direction, attackDamage, PUTOTYRA);
        }
    }
    
    private static LivingEntity findTargetInDirection(LivingEntity owner, Vec3 direction, double maxDistance) {
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;
        
        Vec3 start = owner.getEyePosition();
        Vec3 end = start.add(direction.normalize().scale(maxDistance));
        double searchRadius = 2.0;
        
        AABB searchBox = new AABB(
            Math.min(start.x, end.x) - searchRadius,
            Math.min(start.y, end.y) - searchRadius,
            Math.min(start.z, end.z) - searchRadius,
            Math.max(start.x, end.x) + searchRadius,
            Math.max(start.y, end.y) + searchRadius,
            Math.max(start.z, end.z) + searchRadius
        );
        
        List<LivingEntity> entitiesInRange = owner.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        
        for (LivingEntity entity : entitiesInRange) {
            if (entity != owner && owner.canAttack(entity) && entity.isAlive()) {
                double distanceToLine = distanceToLineSegment(start, end, entity.position());
                if (distanceToLine <= searchRadius) {
                    double distance = entity.distanceToSqr(owner);
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestTarget = entity;
                    }
                }
            }
        }
        
        return nearestTarget;
    }
    
    private static double distanceToLineSegment(Vec3 lineStart, Vec3 lineEnd, Vec3 point) {
        Vec3 lineVector = lineEnd.subtract(lineStart);
        Vec3 pointVector = point.subtract(lineStart);
        double lineLengthSq = lineVector.lengthSqr();
        
        if (lineLengthSq == 0.0) {
            return point.distanceTo(lineStart);
        }
        
        double t = Math.max(0, Math.min(1, pointVector.dot(lineVector) / lineLengthSq));
        Vec3 projection = lineStart.add(lineVector.scale(t));
        return point.distanceTo(projection);
    }
    
    private static Vec3 calculateDirectionWithOffset(Vec3 baseDirection, Vec3 offset) {
        Vec3 normalizedBase = baseDirection.normalize();
        
        Vec3 up = new Vec3(0, 1, 0);
        if (Math.abs(normalizedBase.y) > 0.99) {
            up = new Vec3(1, 0, 0);
        }
        Vec3 right = normalizedBase.cross(up).normalize();
        up = right.cross(normalizedBase).normalize();
        
        return right.scale(offset.x)
            .add(up.scale(offset.y))
            .add(normalizedBase.scale(offset.z))
            .normalize();
    }
    
    private static Vec3 rotateVectorAroundY(Vec3 vector, float angle) {
        double radians = Math.toRadians(angle);
        
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        
        double x = vector.x * cos - vector.z * sin;
        double z = vector.x * sin + vector.z * cos;
        
        return new Vec3(x, vector.y, z).normalize();
    }
    
    public static void spawnPutotyraCellMedalSwallow(Level level, LivingEntity owner, float attackDamage) {
        Vec3 lookVector = owner.getViewVector(1.0F);
        spawnCellMedalSwallow(level, owner, lookVector, attackDamage, null);
        
        if (owner instanceof Player) {
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
        }
    }
}