package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.drive;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DriveRiderEntity extends Projectile implements GeoEntity {
    public enum WheelType {
        ENGINEER(0, "engineer", "绿色工程车轮", 1.0f, 1.2f, ParticleTypes.ITEM_SLIME),
        FIRE(1, "fire", "黄色火焰车轮", 1.2f, 1.0f, ParticleTypes.FLAME),
        NINJA(2, "ninja", "紫色忍者手里刀车轮", 0.9f, 1.5f, ParticleTypes.ENCHANTED_HIT);
        
        private final int id;
        private final String name;
        private final String displayName;
        private final float damageMultiplier;
        private final float speedMultiplier;
        private final ParticleOptions particleType;
        
        WheelType(int id, String name, String displayName, float damageMultiplier, float speedMultiplier, ParticleOptions particleType) {
            this.id = id;
            this.name = name;
            this.displayName = displayName;
            this.damageMultiplier = damageMultiplier;
            this.speedMultiplier = speedMultiplier;
            this.particleType = particleType;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public float getDamageMultiplier() { return damageMultiplier; }
        public float getSpeedMultiplier() { return speedMultiplier; }
        public ParticleOptions getParticleType() { return particleType; }
        
        public static WheelType fromId(int id) {
            for (WheelType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return ENGINEER;
        }
        
        public WheelType getNextType() {
            int nextId = (this.id + 1) % values().length;
            return fromId(nextId);
        }
    }
    
    private static final Map<UUID, WheelType> PLAYER_WHEEL_TYPE_MAP = new HashMap<>();
    private static final RawAnimation MOVE_ANIMATION = RawAnimation.begin().thenPlay("move");
    private static final RawAnimation HIT_ANIMATION = RawAnimation.begin().thenPlay("attack.hit");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int lifetime = 0;
    private static final int MAX_LIFETIME = 200;

    private Vec3 attackDirection;
    private Vec3 initialPosition;
    private int boomerangPhase = 0;
    private static final double MAX_FLIGHT_DISTANCE = 20.0;
    private boolean hasReachedTurningPoint = false;
    private float speed = 1.0f;

    private UUID ownerUUID = null;

    private static final float TRIGGER_CHANCE = 1.0f;

    private static final EntityDataAccessor<Boolean> HAS_HIT_ENTITY = SynchedEntityData.defineId(DriveRiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(DriveRiderEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> WHEEL_TYPE = SynchedEntityData.defineId(DriveRiderEntity.class, EntityDataSerializers.INT);

    private boolean hasHitEntity() {
        return this.getEntityData().get(HAS_HIT_ENTITY);
    }

    private void setHasHitEntity(boolean value) {
        this.getEntityData().set(HAS_HIT_ENTITY, value);
    }

    private float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    private void setDamage(float value) {
        this.getEntityData().set(DAMAGE, value);
    }
    
    public WheelType getWheelType() {
        return WheelType.fromId(this.getEntityData().get(WHEEL_TYPE));
    }
    
    private void setWheelType(WheelType type) {
        this.getEntityData().set(WHEEL_TYPE, type.getId());
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public boolean isHitEntity() {
        return this.hasHitEntity();
    }

    private DriveRiderEntity(Level level, LivingEntity owner, Vec3 direction, float attackDamage, WheelType wheelType) {
        super(EntityRegister.DRIVE_RIDER.get(), level);
        this.setOwner(owner);
        this.noPhysics = true;
        this.initialPosition = owner.getEyePosition();
        this.setPos(this.initialPosition.add(direction.scale(1.0)));
        this.attackDirection = direction.normalize();
        this.setDamage(attackDamage * wheelType.getDamageMultiplier());
        this.setOwnerUUID(owner.getUUID());
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
        this.speed = 1.0f * wheelType.getSpeedMultiplier();
        this.setWheelType(wheelType);
    }

    public DriveRiderEntity(EntityType<? extends DriveRiderEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.attackDirection = Vec3.ZERO;
        this.initialPosition = Vec3.ZERO;
    }

    public static void trySpawnEffect(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        if (level.random.nextFloat() <= TRIGGER_CHANCE && !level.isClientSide) {
            UUID ownerUUID = owner.getUUID();
            WheelType currentType = PLAYER_WHEEL_TYPE_MAP.getOrDefault(ownerUUID, WheelType.ENGINEER);
            
            DriveRiderEntity effect = new DriveRiderEntity(level, owner, direction, attackDamage, currentType);
            level.addFreshEntity(effect);
            
            PLAYER_WHEEL_TYPE_MAP.put(ownerUUID, currentType.getNextType());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(HAS_HIT_ENTITY, false);
        builder.define(DAMAGE, 0.0f);
        builder.define(WHEEL_TYPE, WheelType.ENGINEER.getId());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
        this.setHasHitEntity(compound.getBoolean("HasHitEntity"));
        this.lifetime = compound.getInt("Lifetime");
        if (compound.contains("Damage")) {
            this.setDamage(compound.getFloat("Damage"));
        }
        if (compound.contains("WheelType")) {
            this.setWheelType(WheelType.fromId(compound.getInt("WheelType")));
        }
        if (compound.contains("BoomerangPhase")) {
            this.boomerangPhase = compound.getInt("BoomerangPhase");
        }
        if (compound.contains("HasReachedTurningPoint")) {
            this.hasReachedTurningPoint = compound.getBoolean("HasReachedTurningPoint");
        }
        if (compound.contains("Speed")) {
            this.speed = compound.getFloat("Speed");
        }
        if (compound.contains("InitialX")) {
            double x = compound.getDouble("InitialX");
            double y = compound.getDouble("InitialY");
            double z = compound.getDouble("InitialZ");
            this.initialPosition = new Vec3(x, y, z);
        }
        if (compound.contains("DirectionX")) {
            double x = compound.getDouble("DirectionX");
            double y = compound.getDouble("DirectionY");
            double z = compound.getDouble("DirectionZ");
            this.attackDirection = new Vec3(x, y, z);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        compound.putBoolean("HasHitEntity", this.hasHitEntity());
        compound.putInt("Lifetime", this.lifetime);
        compound.putFloat("Damage", this.getDamage());
        compound.putInt("WheelType", this.getWheelType().getId());
        compound.putInt("BoomerangPhase", this.boomerangPhase);
        compound.putBoolean("HasReachedTurningPoint", this.hasReachedTurningPoint);
        compound.putFloat("Speed", this.speed);
        compound.putDouble("InitialX", this.initialPosition.x);
        compound.putDouble("InitialY", this.initialPosition.y);
        compound.putDouble("InitialZ", this.initialPosition.z);
        compound.putDouble("DirectionX", this.attackDirection.x);
        compound.putDouble("DirectionY", this.attackDirection.y);
        compound.putDouble("DirectionZ", this.attackDirection.z);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::animationPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private PlayState animationPredicate(AnimationState<DriveRiderEntity> state) {
        if (this.hasHitEntity()) {
            state.getController().setAnimation(HIT_ANIMATION);
        } else {
            state.getController().setAnimation(MOVE_ANIMATION);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;

        if (this.hasHitEntity()) {
            if (lifetime > 40) {
                this.explode();
                this.discard();
            }
            return;
        }

        if (lifetime > MAX_LIFETIME) {
            this.explode();
            this.discard();
            return;
        }

        if (!this.level().isClientSide()) {
            this.handleBoomerangMovement();
            this.checkEntityCollision();
        }
        
        this.attractEntities();
    }

    private void handleBoomerangMovement() {
        Vec3 currentPos = this.position();
        
        if (attackDirection == null) {
            this.attackDirection = Vec3.ZERO;
        }
        
        double distanceFromStart = currentPos.distanceTo(this.initialPosition);
        
        switch (boomerangPhase) {
            case 0:
                if (distanceFromStart >= MAX_FLIGHT_DISTANCE || hasReachedTurningPoint) {
                    boomerangPhase = 1;
                    hasReachedTurningPoint = true;
                }
                break;
            
            case 1:
                LivingEntity owner = this.getOwner();
                Vec3 targetPos = (owner != null) ? owner.position().add(0, 1, 0) : this.initialPosition;
                Vec3 returnDirection = targetPos.subtract(currentPos).normalize();
                
                this.attackDirection = this.attackDirection.scale(0.9).add(returnDirection.scale(0.1)).normalize();
                this.speed = Math.min(this.speed + 0.01f, 1.5f);
                
                if (this.attackDirection.dot(returnDirection) > 0.95) {
                    boomerangPhase = 2;
                }
                break;
            
            case 2:
                LivingEntity owner2 = this.getOwner();
                Vec3 returnTarget = (owner2 != null) ? owner2.position().add(0, 1, 0) : this.initialPosition;
                double distanceToTarget = currentPos.distanceTo(returnTarget);
                
                if (distanceToTarget < 2.0) {
                    this.explode();
                    this.discard();
                    return;
                }
                
                Vec3 finalReturnDirection = returnTarget.subtract(currentPos).normalize();
                this.attackDirection = this.attackDirection.scale(0.8).add(finalReturnDirection.scale(0.2)).normalize();
                this.speed = Math.min(this.speed + 0.01f, 1.8f);
                break;
        }
        
        Vec3 newPos = currentPos.add(attackDirection.scale(this.speed));
        this.setPos(newPos.x, newPos.y, newPos.z);
        
        this.updateRotation();
    }
    
    public void updateRotation() {
        if (attackDirection == null || attackDirection.length() < 0.01) {
            return;
        }
        
        double horizontalDistance = Math.sqrt(attackDirection.x * attackDirection.x + attackDirection.z * attackDirection.z);
        float yaw = (float) Math.toDegrees(Math.atan2(-attackDirection.x, attackDirection.z));
        float pitch = (float) Math.toDegrees(Math.atan2(-attackDirection.y, horizontalDistance));
        
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    private void checkEntityCollision() {
        if (attackDirection == null) {
            this.attackDirection = Vec3.ZERO;
        }
        
        AABB boundingBox = this.getBoundingBox().expandTowards(attackDirection.scale(1.0)).inflate(1.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, boundingBox,
                entity -> entity != this.getOwner() && entity.isAlive() && this.canAttack(entity));

        for (LivingEntity entity : entities) {
            this.onHitEntity(entity);
            break;
        }
    }

    private void onHitEntity(LivingEntity target) {
        if (this.hasHitEntity()) return;

        this.setHasHitEntity(true);

        float finalDamage = this.getDamage();

        LivingEntity owner = this.getOwner();
        if (owner != null) {
            target.hurt(this.damageSources().mobProjectile(this, owner), finalDamage);
        } else {
            target.hurt(this.damageSources().magic(), finalDamage);
        }

        lifetime = 0;
    }

    private boolean canAttack(LivingEntity target) {
        LivingEntity owner = getOwner();
        if (owner == null) return true;
        if (target == owner) return false;
        return owner.canAttack(target);
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
    
    private void attractEntities() {
        if (this.level().isClientSide()) return;
        
        Vec3 center = this.position();
        AABB searchArea = this.getBoundingBox().inflate(5.0D);
        WheelType currentType = this.getWheelType();
        
        List<Entity> entities = this.level().getEntities(this, searchArea, entity -> 
                entity instanceof LivingEntity && 
                entity != this.getOwner() && 
                entity.isAlive() && 
                !(entity instanceof Projectile)
        );
        
        for (Entity entity : entities) {
            Vec3 entityPos = entity.position();
            Vec3 direction = center.subtract(entityPos).normalize();
            
            double distance = entityPos.distanceTo(center);
            if (distance > 0.3) {
                double baseForce = 0.3;
                if (currentType == WheelType.ENGINEER) {
                    baseForce = 0.4;
                } else if (currentType == WheelType.NINJA) {
                    baseForce = 0.2;
                }
                
                double force = baseForce * (1.0 - distance / 5.0);
                Vec3 pushVector = direction.scale(force);
                
                entity.push(pushVector.x, pushVector.y, pushVector.z);
                
                if (this.level().random.nextFloat() < 0.4) {
                    this.level().addParticle(currentType.getParticleType(), 
                            entityPos.x, entityPos.y + 0.5, entityPos.z, 
                            direction.x * 0.5, direction.y * 0.5, direction.z * 0.5);
                }
            }
            
            if (currentType == WheelType.FIRE && entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                if (this.level().random.nextFloat() < 0.2) {
                    livingEntity.setRemainingFireTicks(40);  // 2秒 = 40 ticks
                }
            }
        }
    }
    
    private void explode() {
        if (this.level().isClientSide()) return;
        
        Vec3 center = this.position();
        WheelType currentType = this.getWheelType();
        
        float explosionRadius = 2.0F;
        if (currentType == WheelType.ENGINEER) {
            explosionRadius = 2.5F;
        } else if (currentType == WheelType.FIRE) {
            explosionRadius = 1.8F;
        }
        
        this.level().explode(this, center.x, center.y, center.z, 
                explosionRadius, Level.ExplosionInteraction.NONE);
        
        AABB explosionArea = this.getBoundingBox().inflate(3.0D);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, explosionArea, 
                entity -> entity != this.getOwner() && entity.isAlive());
        
        for (LivingEntity entity : entities) {
            float damageMultiplier = 0.5F;
            if (currentType == WheelType.NINJA) {
                damageMultiplier = 0.7F;
            }
            
            float explosionDamage = this.getDamage() * damageMultiplier;
            
            LivingEntity owner = this.getOwner();
            if (owner != null) {
                entity.hurt(this.damageSources().mobAttack(owner), explosionDamage);
            } else {
                entity.hurt(this.damageSources().magic(), explosionDamage);
            }
            
            switch (currentType) {
                case FIRE:
                    entity.setRemainingFireTicks(60);  // 3秒 = 60 ticks
                    break;
                case ENGINEER:
                    Vec3 knockbackDir = entity.position().subtract(center).normalize().scale(0.5);
                    entity.push(knockbackDir.x, 0.3, knockbackDir.z);
                    break;
                case NINJA:
                    break;
            }
        }
        
        for (int i = 0; i < 20; i++) {
            double offsetX = (this.level().random.nextDouble() - 0.5) * explosionRadius * 2;
            double offsetY = (this.level().random.nextDouble() - 0.5) * explosionRadius * 2;
            double offsetZ = (this.level().random.nextDouble() - 0.5) * explosionRadius * 2;
            
            this.level().addParticle(currentType.getParticleType(),
                    center.x + offsetX, center.y + offsetY, center.z + offsetZ,
                    0, 0, 0);
        }
    }
}