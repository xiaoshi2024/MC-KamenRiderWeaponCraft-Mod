package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.ooo;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

public class OOOGeoEntity extends AbstractHurtingProjectile implements GeoEntity {
    
    private String coinType = "tatoba";
    
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    private static final RawAnimation SPIN_ANIMATION = RawAnimation.begin().thenPlay("spin");
    
    private static final EntityDataAccessor<String> COIN_TYPE = SynchedEntityData.defineId(OOOGeoEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(OOOGeoEntity.class, EntityDataSerializers.FLOAT);
    
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 80;
    
    private UUID ownerUUID = null;
    
    private UUID targetUUID = null;
    private LivingEntity targetEntity = null;
    private boolean isTracking = false;
    private double orbitRadius = 1.0;
    private double orbitAngle = 0.0;
    private double orbitSpeed = 0.1;
    
    public OOOGeoEntity(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }
    
    private OOOGeoEntity(Level level, LivingEntity owner, Vec3 direction, float attackDamage, String coinType) {
        super(EntityRegister.OOO_GEO_EFFECT.get(), level);
        this.setOwner(owner);
        this.noPhysics = true;
        this.setPos(owner.getEyePosition().add(direction.scale(1.0)));
        this.shoot(direction.x, direction.y, direction.z, 1.5f, 0.0f);
        this.setCoinType(coinType);
        this.entityData.set(DAMAGE, attackDamage);
        this.setOwnerUUID(owner.getUUID());
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
        
        this.orbitAngle = level.random.nextDouble() * Math.PI * 2;
        
        this.setVisualFire(false);
        this.setSecondsOnFire(0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COIN_TYPE, "tatoba");
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
    
    public void setCoinType(String coinType) {
        this.coinType = coinType;
        this.entityData.set(COIN_TYPE, coinType);
    }
    
    public String getCoinType() {
        return this.entityData.get(COIN_TYPE);
    }
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::animationPredicate));
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    
    private PlayState animationPredicate(AnimationState<OOOGeoEntity> event) {
        event.getController().setAnimation(SPIN_ANIMATION);
        return PlayState.CONTINUE;
    }
    
    @Override
    public void tick() {
        this.clearFire();
        this.setVisualFire(false);
        
        lifetime++;
        
        if ("putotyra".equals(this.getCoinType())) {
            handleTrackingBehavior();
        }
        
        if (lifetime >= MAX_LIFETIME) {
            this.discard();
            return;
        }
        
        if (!isTracking) {
            super.tick();
            this.clearFire();
            this.setVisualFire(false);
        }
    }
    
    private void handleTrackingBehavior() {
        LivingEntity owner = this.getOwner();
        
        if (!isTracking && targetEntity == null && owner != null) {
            findAndTrackTarget(owner);
        }
        
        if (isTracking && targetEntity != null && targetEntity.isAlive()) {
            orbitAroundTarget();
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
        double searchRange = lifetime < 20 ? 15.0 : 8.0;
        
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
                
                this.orbitRadius = 0.8 + nearestTarget.getBbWidth() * 0.5;
                
                this.orbitSpeed = 0.1 + (Math.min(lifetime, 60) / 60.0) * 0.1;
            }
        }
    }
    
    private void orbitAroundTarget() {
        if (targetEntity == null) return;
        
        this.orbitAngle += this.orbitSpeed;
        if (this.orbitAngle > Math.PI * 2) {
            this.orbitAngle -= Math.PI * 2;
        }
        
        double offsetX = Math.cos(this.orbitAngle) * this.orbitRadius;
        double offsetY = Math.sin(this.orbitAngle * 0.5) * 0.3;
        double offsetZ = Math.sin(this.orbitAngle) * this.orbitRadius;
        
        Vec3 targetCenter = targetEntity.position().add(0, targetEntity.getBbHeight() * 0.5, 0);
        
        Vec3 newPos = targetCenter.add(offsetX, offsetY, offsetZ);
        
        this.setPos(newPos.x, newPos.y, newPos.z);
        
        this.orbitSpeed += (this.level().random.nextDouble() - 0.5) * 0.01;
        this.orbitSpeed = Math.max(0.05, Math.min(this.orbitSpeed, 0.2));
        
        if (lifetime > MAX_LIFETIME / 2) {
            this.orbitRadius *= 0.99;
            this.orbitRadius = Math.max(this.orbitRadius, 0.3);
        }
        
        if (lifetime % 10 == 0) {
            if (targetEntity.isAlive()) {
                targetEntity.hurt(this.getDamageSource(), this.getDamageValue() * 0.2f);
            }
        }
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
        compound.putString("CoinType", this.getCoinType());
        compound.putInt("Lifetime", this.lifetime);
        compound.putFloat("Damage", this.getDamageValue());
        compound.putBoolean("IsTracking", this.isTracking);
        compound.putDouble("OrbitRadius", this.orbitRadius);
        compound.putDouble("OrbitAngle", this.orbitAngle);
        compound.putDouble("OrbitSpeed", this.orbitSpeed);
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
        this.setCoinType(compound.getString("CoinType"));
        this.lifetime = compound.getInt("Lifetime");
        this.entityData.set(DAMAGE, compound.getFloat("Damage"));
        this.isTracking = compound.getBoolean("IsTracking");
        this.orbitRadius = compound.getDouble("OrbitRadius");
        this.orbitAngle = compound.getDouble("OrbitAngle");
        this.orbitSpeed = compound.getDouble("OrbitSpeed");
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
            float damage = this.getDamageValue();
            
            if ("latorartar".equals(this.getCoinType())) {
                damage *= 1.3f;
            } else if ("shauta".equals(this.getCoinType())) {
                damage *= 1.1f;
            } else if ("sagohzo".equals(this.getCoinType())) {
                damage *= 1.0f;
            } else if ("putotyra".equals(this.getCoinType())) {
                damage *= 1.5f;
            } else {
                damage *= 1.2f;
            }
            
            target.hurt(this.getDamageSource(), damage);
        }
        
        this.discard();
    }
    
    @Override
    protected float getInertia() {
        switch (this.getCoinType()) {
            case "latorartar":
                return 0.95F;
            case "shauta":
                return 0.98F;
            case "sagohzo":
                return 0.92F;
            case "putotyra":
                return 0.90F;
            case "tatoba":
            default:
                return 0.96F;
        }
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
        return "shauta".equals(this.getCoinType()) || "tatoba".equals(this.getCoinType());
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player) {
            this.discard();
            return true;
        }
        return false;
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
    
    public LivingEntity getTarget() {
        if (targetEntity != null && targetEntity.isAlive()) {
            return targetEntity;
        }
        
        if (targetUUID != null && this.level() != null) {
            for (Entity entity : this.level().getEntitiesOfClass(Entity.class, this.getBoundingBox().inflate(20.0D))) {
                if (entity.getUUID().equals(targetUUID) && entity instanceof LivingEntity && entity.isAlive()) {
                    this.targetEntity = (LivingEntity) entity;
                    return this.targetEntity;
                }
            }
        }
        
        this.targetEntity = null;
        this.targetUUID = null;
        this.isTracking = false;
        return null;
    }
    
    public static void trySpawnEffect(Level level, LivingEntity owner, Vec3 direction, float attackDamage, String coinType) {
        if (!level.isClientSide && EntityRegister.OOO_GEO_EFFECT.get() != null) {
            OOOGeoEntity effect = new OOOGeoEntity(level, owner, direction, attackDamage, coinType);
            level.addFreshEntity(effect);
        }
    }
}