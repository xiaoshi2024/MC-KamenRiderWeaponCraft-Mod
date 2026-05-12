package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.kiva;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public class KivaBatEntity extends AbstractHurtingProjectile implements GeoEntity {
    
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenPlay("bats");
    
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(KivaBatEntity.class, EntityDataSerializers.FLOAT);
    
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 100;
    
    private UUID ownerUUID = null;
    
    private UUID targetUUID = null;
    private LivingEntity targetEntity = null;
    private boolean isTracking = false;
    private double trackingSpeed = 0.8;
    private double wanderSpeed = 0.3;
    private double currentSpeed = 0.8;
    
    public KivaBatEntity(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = false;
    }
    
    private KivaBatEntity(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        super(EntityRegister.KIVA_BAT_EFFECT.get(), level);
        this.setOwner(owner);
        this.noPhysics = false;
        this.setPos(owner.getEyePosition().add(direction.scale(1.0)));
        this.shoot(direction.x, direction.y, direction.z, 1.2f, 5.0f);
        this.entityData.set(DAMAGE, attackDamage);
        this.setOwnerUUID(owner.getUUID());
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
        
        this.setVisualFire(false);
        this.setRemainingFireTicks(0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DAMAGE, 0.0f);
    }
    
    private float getDamageValue() {
        return this.entityData.get(DAMAGE);
    }
    
    public UUID getOwnerUUID() {
        return ownerUUID;
    }
    
    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::animationPredicate));
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    
    private PlayState animationPredicate(AnimationState<KivaBatEntity> event) {
        event.getController().setAnimation(FLY_ANIMATION);
        return PlayState.CONTINUE;
    }
    
    @Override
    public void tick() {
        this.clearFire();
        this.setVisualFire(false);
        
        lifetime++;
        
        handleTrackingBehavior();
        
        if (lifetime >= MAX_LIFETIME) {
            this.discard();
            return;
        }
        
        if (lifetime < 20) {
            currentSpeed = Math.min(trackingSpeed, currentSpeed + 0.05);
        } else if (lifetime > MAX_LIFETIME - 30) {
            currentSpeed = Math.max(wanderSpeed, currentSpeed - 0.02);
        }
        
        super.tick();
        this.clearFire();
        this.setVisualFire(false);
    }
    
    private void handleTrackingBehavior() {
        LivingEntity owner = this.getOwner();
        
        if (!isTracking && targetEntity == null && owner != null) {
            findAndTrackTarget(owner);
        }
        
        if (isTracking && targetEntity != null && targetEntity.isAlive()) {
            trackTarget();
        } else {
            if (owner != null) {
                findAndTrackTarget(owner);
            }
            if (targetEntity == null) {
                isTracking = false;
            }
        }
    }
    
    private void findAndTrackTarget(LivingEntity owner) {
        double searchRange = lifetime < 30 ? 15.0 : 8.0;
        
        Vec3 pos = this.position();
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(pos.x - searchRange, pos.y - searchRange, pos.z - searchRange, 
                         pos.x + searchRange, pos.y + searchRange, pos.z + searchRange),
                entity -> entity != owner && entity.isAlive() && owner.canAttack(entity));
        
        if (!nearbyEntities.isEmpty()) {
            LivingEntity nearestTarget = null;
            double nearestDistance = Double.MAX_VALUE;
            
            for (LivingEntity entity : nearbyEntities) {
                double distance = entity.distanceToSqr(this);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestTarget = entity;
                }
            }
            
            if (nearestTarget != null) {
                this.targetEntity = nearestTarget;
                this.targetUUID = nearestTarget.getUUID();
                this.isTracking = true;
                
                currentSpeed = trackingSpeed;
            }
        }
    }
    
    private void trackTarget() {
        if (targetEntity == null) return;
        
        Vec3 targetPos = targetEntity.position().add(0, targetEntity.getBbHeight() * 0.5, 0);
        Vec3 toTarget = targetPos.subtract(this.position()).normalize();
        
        double randomX = (this.level().random.nextDouble() - 0.5) * 0.1;
        double randomY = (this.level().random.nextDouble() - 0.5) * 0.1;
        double randomZ = (this.level().random.nextDouble() - 0.5) * 0.1;
        
        Vec3 adjustedDirection = new Vec3(
            toTarget.x + randomX,
            toTarget.y + randomY,
            toTarget.z + randomZ
        ).normalize();
        
        this.setDeltaMovement(adjustedDirection.scale(currentSpeed));
        
        updateRotationToTarget(toTarget);
    }
    
    private void updateRotationToTarget(Vec3 direction) {
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F;
        float pitch = (float) Math.toDegrees(Math.atan2(direction.y, horizontalDistance));
        
        this.setYRot(yaw);
        this.setXRot(pitch);
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        if (this.targetUUID != null) {
            compound.putUUID("TargetUUID", this.targetUUID);
        }
        compound.putInt("Lifetime", this.lifetime);
        compound.putFloat("Damage", this.getDamageValue());
        compound.putBoolean("IsTracking", this.isTracking);
        compound.putDouble("CurrentSpeed", this.currentSpeed);
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
        if (compound.hasUUID("TargetUUID")) {
            this.targetUUID = compound.getUUID("TargetUUID");
        }
        this.lifetime = compound.getInt("Lifetime");
        this.entityData.set(DAMAGE, compound.getFloat("Damage"));
        this.isTracking = compound.getBoolean("IsTracking");
        this.currentSpeed = compound.getDouble("CurrentSpeed");
    }
    
    protected DamageSource getDamageSource() {
        LivingEntity owner = this.getOwner();
        if (owner != null) {
            return this.damageSources().mobProjectile(this, owner);
        } else {
            return this.damageSources().magic();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        
        if (result.getEntity() instanceof LivingEntity target) {
            float damage = this.getDamageValue() * 0.3f;
            target.hurt(this.getDamageSource(), damage);
            
            this.discard();
        }
    }
    
    @Override
    protected float getInertia() {
        return 0.95F;
    }
    
    public boolean isVisualFire() {
        return false;
    }
    
    public void setVisualFire(boolean visualFire) {
    }
    
    public void setSecondsOnFire(int seconds) {
    }
    
    @Override
    public boolean isNoGravity() {
        return true;
    }
    
    public LivingEntity getOwner() {
        UUID uuid = this.getOwnerUUID();
        if (uuid == null || this.level() == null) return null;
        
        Entity owner = this.level().getPlayerByUUID(uuid);
        if (owner instanceof LivingEntity) {
            return (LivingEntity) owner;
        }
        
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, 
                this.getBoundingBox().inflate(20.0D))) {
            if (entity.getUUID().equals(uuid) && entity.isAlive()) {
                return entity;
            }
        }
        
        return null;
    }
    
    public static void trySpawnEffect(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        if (!level.isClientSide && EntityRegister.KIVA_BAT_EFFECT.get() != null) {
            KivaBatEntity effect = new KivaBatEntity(level, owner, direction, attackDamage);
            level.addFreshEntity(effect);
        }
    }
}