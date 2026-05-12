package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.fourze;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public class FourzeRocketEntity extends Projectile implements GeoEntity {
    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenPlay("fly");
    private static final RawAnimation EXPLODE_ANIMATION = RawAnimation.begin().thenPlay("explode");

    private int lifetime = 0;
    private static final int MAX_LIFETIME = 100;
    private static final int EXPLODE_ANIMATION_LENGTH = 20;

    private Vec3 attackDirection;
    private LivingEntity trackingTarget = null;
    private int trackingCooldown = 0;

    private static final double MAX_TRACKING_DISTANCE = 20.0;
    private float trackingSpeed = 1.0f;
    private static final float MAX_TRACKING_SPEED = 2.0f;
    private static final float ACCELERATION_RATE = 0.05f;

    private UUID ownerUUID = null;

    private static final EntityDataAccessor<Boolean> IS_EXPLODING = SynchedEntityData.defineId(FourzeRocketEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(FourzeRocketEntity.class, EntityDataSerializers.FLOAT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private boolean isExploding() {
        return this.getEntityData().get(IS_EXPLODING);
    }

    private void setExploding(boolean value) {
        this.getEntityData().set(IS_EXPLODING, value);
    }

    private float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    private void setDamage(float value) {
        this.getEntityData().set(DAMAGE, value);
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    private FourzeRocketEntity(Level level, LivingEntity owner, Vec3 position, Vec3 initialDirection, float damage) {
        super(EntityRegister.FOURZE_ROCKET.get(), level);
        this.setOwner(owner);
        this.setOwnerUUID(owner.getUUID());
        this.setPos(position);
        this.attackDirection = initialDirection.normalize();
        this.setDeltaMovement(initialDirection.normalize().scale(trackingSpeed));
        this.setDamage(damage);
        this.noPhysics = false;
    }

    public FourzeRocketEntity(EntityType<? extends FourzeRocketEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public static void spawnRockets(Level level, LivingEntity owner, Vec3 direction, float damage) {
        if (level.isClientSide) return;

        for (int i = 0; i < 3; i++) {
            float spreadAngle = (i - 1) * 5.0f;
            Vec3 spreadDirection = rotateVectorAroundY(direction, spreadAngle);
            
            Vec3 offset = new Vec3(
                (i - 1) * 0.5,
                0.3,
                (i - 1) * 0.5
            );
            Vec3 spawnPos = owner.getEyePosition().add(direction.normalize().scale(1.0)).add(offset);
            
            FourzeRocketEntity rocket = new FourzeRocketEntity(level, owner, spawnPos, spreadDirection, damage);
            level.addFreshEntity(rocket);
        }
    }

    private static Vec3 rotateVectorAroundY(Vec3 vector, float degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        
        double x = vector.x * cos + vector.z * sin;
        double z = vector.z * cos - vector.x * sin;
        
        return new Vec3(x, vector.y, z).normalize();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(IS_EXPLODING, false);
        builder.define(DAMAGE, 0.0f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
        this.setExploding(compound.getBoolean("IsExploding"));
        this.lifetime = compound.getInt("Lifetime");
        this.trackingSpeed = compound.getFloat("TrackingSpeed");
        if (compound.contains("Damage")) {
            this.setDamage(compound.getFloat("Damage"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        compound.putBoolean("IsExploding", this.isExploding());
        compound.putInt("Lifetime", this.lifetime);
        compound.putFloat("TrackingSpeed", this.trackingSpeed);
        compound.putFloat("Damage", this.getDamage());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::animationPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private PlayState animationPredicate(AnimationState<FourzeRocketEntity> state) {
        if (this.isExploding()) {
            state.getController().setAnimation(EXPLODE_ANIMATION);
        } else {
            state.getController().setAnimation(FLY_ANIMATION);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;

        if (this.isExploding()) {
            if (lifetime > EXPLODE_ANIMATION_LENGTH) {
                this.discard();
            }
            return;
        }

        if (lifetime > MAX_LIFETIME) {
            this.explode();
            return;
        }

        if (this.level().isClientSide()) {
            spawnExhaustParticles();
        }

        if (!this.level().isClientSide()) {
            updateTrackingTarget();
            enhancedTrackingMovement();
            checkCollision();
        }
    }
    
    private void updateTrackingTarget() {
        if (trackingCooldown > 0) {
            if (trackingTarget != null && (!trackingTarget.isAlive() || 
                    trackingTarget.distanceToSqr(this) > MAX_TRACKING_DISTANCE * MAX_TRACKING_DISTANCE)) {
                trackingTarget = null;
            }
            trackingCooldown--;
            return;
        }
        
        LivingEntity owner = getOwner();
        
        LivingEntity nearestTarget = this.level().getEntitiesOfClass(LivingEntity.class, 
                this.getBoundingBox().inflate(MAX_TRACKING_DISTANCE),
                e -> e != owner && e.isAlive() && canAttack(e))
                .stream()
                .min(java.util.Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
        
        if (nearestTarget != null) {
            trackingTarget = nearestTarget;
            trackingCooldown = 10;
        }
    }
    
    private void enhancedTrackingMovement() {
        trackingSpeed = Math.min(trackingSpeed + ACCELERATION_RATE, MAX_TRACKING_SPEED);
        
        Vec3 moveVec;
        
        if (trackingTarget != null && trackingTarget.isAlive()) {
            Vec3 targetPos = trackingTarget.position().add(0, trackingTarget.getBbHeight() / 2, 0);
            Vec3 entityPos = this.position();
            
            Vec3 direction = targetPos.subtract(entityPos).normalize();
            
            moveVec = direction.scale(trackingSpeed);
            
            lookAt(targetPos);
        } else if (attackDirection != null) {
            moveVec = attackDirection.scale(trackingSpeed);
        } else {
            moveVec = this.getDeltaMovement();
        }
        
        this.setDeltaMovement(moveVec);
        
        this.move(MoverType.SELF, this.getDeltaMovement());
        
        updateRotation();
    }
    
    private void lookAt(Vec3 targetPos) {
        Vec3 entityPos = this.position();
        double dx = targetPos.x - entityPos.x;
        double dy = targetPos.y - entityPos.y;
        double dz = targetPos.z - entityPos.z;
        
        double yaw = Math.atan2(dz, dx) * (180 / Math.PI) - 90.0;
        double distance = Math.sqrt(dx * dx + dz * dz);
        double pitch = Math.atan2(dy, distance) * (180 / Math.PI);
        
        this.setYRot((float) yaw);
        this.setXRot((float) pitch);
        this.setYBodyRot((float) yaw);
        this.setYHeadRot((float) yaw);
    }

    private void spawnExhaustParticles() {
        Vec3 motion = this.getDeltaMovement().normalize();
        Vec3 exhaustPos = this.position().subtract(motion.scale(0.5));
        
        for (int i = 0; i < 3; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (this.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.3;
            
            this.level().addParticle(
                ParticleTypes.FLAME,
                exhaustPos.x + offsetX,
                exhaustPos.y + offsetY,
                exhaustPos.z + offsetZ,
                -motion.x * 0.5,
                -motion.y * 0.5,
                -motion.z * 0.5
            );
            
            if (this.random.nextBoolean()) {
                this.level().addParticle(
                    ParticleTypes.SMOKE,
                    exhaustPos.x + offsetX,
                    exhaustPos.y + offsetY,
                    exhaustPos.z + offsetZ,
                    -motion.x * 0.3,
                    -motion.y * 0.3,
                    -motion.z * 0.3
                );
            }
        }
    }

    public void updateRotation() {
        Vec3 motion = this.getDeltaMovement();
        if (motion.length() < 0.01) return;
        
        double horizontalDistance = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float yaw = (float) Math.toDegrees(Math.atan2(-motion.x, motion.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-motion.y, horizontalDistance));
        
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    private void checkCollision() {
        if (trackingTarget != null && trackingTarget.isAlive()) {
            if (this.getBoundingBox().intersects(trackingTarget.getBoundingBox())) {
                explode();
                return;
            }
        }
        
        if (this.level().getBlockState(this.blockPosition()).isSolid()) {
            explode();
            return;
        }
    }

    private void explode() {
        if (this.isExploding() || this.level().isClientSide()) return;
        
        this.setExploding(true);
        this.lifetime = 0;
        
        Vec3 center = this.position();
        float explosionRadius = 2.0F;
        
        this.level().explode(
            this,
            center.x,
            center.y,
            center.z,
            explosionRadius,
            Level.ExplosionInteraction.NONE
        );
        
        AABB explosionArea = this.getBoundingBox().inflate(explosionRadius);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, explosionArea, 
            entity -> entity != this.getOwner() && entity.isAlive());
        
        LivingEntity owner = this.getOwner();
        float explosionDamage = this.getDamage() * 1.2f;
        
        for (LivingEntity entity : entities) {
            double distance = entity.distanceTo(this);
            float finalDamage = (float) (explosionDamage * (1.0 - distance / (explosionRadius * 2.0)));
            
            if (finalDamage > 0) {
                if (owner != null) {
                    entity.hurt(this.damageSources().mobAttack(owner), finalDamage);
                } else {
                    entity.hurt(this.damageSources().magic(), finalDamage);
                }
                
                Vec3 knockbackDir = entity.position().subtract(center).normalize();
                entity.push(knockbackDir.x * 0.5, 0.3, knockbackDir.z * 0.5);
            }
        }
        
        for (int i = 0; i < 30; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * explosionRadius * 2;
            double offsetY = (this.random.nextDouble() - 0.5) * explosionRadius * 2;
            double offsetZ = (this.random.nextDouble() - 0.5) * explosionRadius * 2;
            
            this.level().addParticle(
                ParticleTypes.EXPLOSION,
                center.x + offsetX,
                center.y + offsetY,
                center.z + offsetZ,
                0,
                0,
                0
            );
            
            if (this.random.nextBoolean()) {
                this.level().addParticle(
                    ParticleTypes.FLAME,
                    center.x + offsetX,
                    center.y + offsetY,
                    center.z + offsetZ,
                    0,
                    0,
                    0
                );
            }
        }
    }

    private boolean canAttack(LivingEntity target) {
        LivingEntity owner = getOwner();
        
        if (owner != null && target == owner) return false;
        
        return true;
    }

    public LivingEntity getOwner() {
        UUID uuid = this.getOwnerUUID();
        if (uuid == null || this.level() == null) return null;
        
        AABB searchArea = this.getBoundingBox().inflate(32.0D);
        List<Entity> entities = this.level().getEntities(this, searchArea, entity ->
            entity instanceof LivingEntity && entity.getUUID().equals(uuid)
        );
        
        if (!entities.isEmpty()) {
            return (LivingEntity) entities.get(0);
        }
        
        Entity owner = this.level().getPlayerByUUID(uuid);
        if (owner instanceof LivingEntity) {
            return (LivingEntity) owner;
        }
        
        return null;
    }
}