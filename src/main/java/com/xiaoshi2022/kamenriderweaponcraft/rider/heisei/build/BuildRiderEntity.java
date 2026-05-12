package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.build;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
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

import java.util.List;
import java.util.UUID;

public class BuildRiderEntity extends Projectile implements GeoEntity {
    private static final RawAnimation MOVE_ANIMATION = RawAnimation.begin().thenPlay("move");
    private static final RawAnimation HIT_ANIMATION = RawAnimation.begin().thenPlay("attack.hit");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int lifetime = 0;
    private static final int MAX_LIFETIME = 100;

    private Vec3 attackDirection;
    private UUID ownerUUID = null;

    private static final float TRIGGER_CHANCE = 0.3f;

    private static final EntityDataAccessor<Boolean> HAS_HIT_ENTITY = SynchedEntityData.defineId(BuildRiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(BuildRiderEntity.class, EntityDataSerializers.FLOAT);

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

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public boolean isHitEntity() {
        return this.hasHitEntity();
    }

    private BuildRiderEntity(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        super(EntityRegister.BUILD_RIDER_EFFECT.get(), level);
        this.setOwner(owner);
        this.noPhysics = true;
        this.setPos(owner.getEyePosition().add(direction.scale(1.0)));
        this.attackDirection = direction;
        this.setDamage(attackDamage);
        this.setOwnerUUID(owner.getUUID());
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
    }

    public BuildRiderEntity(EntityType<? extends BuildRiderEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.attackDirection = Vec3.ZERO;
    }

    public static void trySpawnEffect(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        if (level.random.nextFloat() <= TRIGGER_CHANCE && !level.isClientSide) {
            BuildRiderEntity effect = new BuildRiderEntity(level, owner, direction, attackDamage);
            level.addFreshEntity(effect);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(HAS_HIT_ENTITY, false);
        builder.define(DAMAGE, 0.0f);
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        compound.putBoolean("HasHitEntity", this.hasHitEntity());
        compound.putInt("Lifetime", this.lifetime);
        compound.putFloat("Damage", this.getDamage());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::animationPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private PlayState animationPredicate(AnimationState<BuildRiderEntity> state) {
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
            Vec3 currentPos = this.position();
            if (attackDirection != null) {
                Vec3 newPos = currentPos.add(attackDirection.scale(0.6));
                this.setPos(newPos.x, newPos.y, newPos.z);
                this.checkEntityCollision();
            } else {
                this.attackDirection = Vec3.ZERO;
            }
        }

        this.attractEntities();
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

    private void attractEntities() {
        if (this.level().isClientSide()) return;

        Vec3 center = this.position();
        AABB searchArea = this.getBoundingBox().inflate(5.0D);

        List<Entity> entities = this.level().getEntities(this, searchArea, entity ->
                entity instanceof LivingEntity &&
                        entity != this.getOwner() &&
                        entity.isAlive() &&
                        !(entity instanceof Projectile) &&
                        !entity.isSpectator()
        );

        for (Entity entity : entities) {
            Vec3 entityPos = entity.position();
            Vec3 toCenter = center.subtract(entityPos);
            double distance = toCenter.length();

            if (distance < 0.5) continue;

            Vec3 direction = toCenter.normalize();

            double maxDistance = 5.0;
            double minForce = 0.1;
            double maxForce = 0.8;

            double normalizedDistance = distance / maxDistance;
            double force = maxForce * (1.0 - normalizedDistance * normalizedDistance);
            force = Math.max(force, minForce);

            Vec3 attraction = direction.scale(force * 0.3);

            if (entity instanceof LivingEntity livingEntity) {
                Vec3 currentMotion = entity.getDeltaMovement();
                Vec3 newMotion = currentMotion.add(attraction);

                double maxSpeed = 1.5;
                if (newMotion.length() > maxSpeed) {
                    newMotion = newMotion.normalize().scale(maxSpeed);
                }

                entity.setDeltaMovement(newMotion);
                livingEntity.hurtMarked = true;
            } else {
                entity.push(attraction.x, attraction.y, attraction.z);
            }

            if (force > minForce * 1.5 && this.level().random.nextFloat() < 0.3) {
                this.level().addParticle(ParticleTypes.ENCHANT,
                        entityPos.x, entityPos.y + entity.getBbHeight() * 0.5, entityPos.z,
                        direction.x * 0.2, direction.y * 0.2, direction.z * 0.2);
            }

            if (this.level().random.nextFloat() < 0.02) {
                this.level().playSound(null, center.x, center.y, center.z,
                        net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.NEUTRAL, 0.3F, 1.5F);
            }
        }
    }

    private void explode() {
        if (this.level().isClientSide()) return;

        Vec3 center = this.position();

        this.level().explode(this, center.x, center.y, center.z,
                2.0F, Level.ExplosionInteraction.NONE);

        AABB explosionArea = this.getBoundingBox().inflate(3.0D);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, explosionArea,
                entity -> entity != this.getOwner() && entity.isAlive());

        for (LivingEntity entity : entities) {
            float explosionDamage = this.getDamage() * 0.5F;

            LivingEntity owner = this.getOwner();
            if (owner != null) {
                entity.hurt(this.damageSources().mobAttack(owner), explosionDamage);
            } else {
                entity.hurt(this.damageSources().magic(), explosionDamage);
            }
        }
    }
}
