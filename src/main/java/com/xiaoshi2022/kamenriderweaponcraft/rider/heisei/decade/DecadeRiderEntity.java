package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.decade;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
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

import java.util.List;
import java.util.UUID;

public class DecadeRiderEntity extends Projectile implements GeoEntity {
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(DecadeRiderEntity.class, EntityDataSerializers.FLOAT);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation DIMENSION_KICK_ANIMATION = RawAnimation.begin().then("dcd", Animation.LoopType.PLAY_ONCE);

    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 40;
    private boolean hasDealtDamage = false;
    private static final int DAMAGE_TICK = 10;
    private static final int EXPLOSION_TICK = 30;

    private UUID ownerUUID;

    public DecadeRiderEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public DecadeRiderEntity(Level level, LivingEntity owner, float damage, Vec3 direction) {
        super(EntityRegister.DECADE_RIDER.get(), level);
        if (owner != null) {
            this.setOwnerUUID(owner.getUUID());
        }
        this.setDamage(damage);
        this.setDeltaMovement(direction.normalize().scale(2.0));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_DAMAGE, 52.0f);
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public void setDamage(float damage) {
        this.entityData.set(DATA_DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    @Override
    public void tick() {
        super.tick();
        lifeTicks++;

        if (this.level().isClientSide) {
            spawnParticles();
        }

        if (!this.level().isClientSide) {
            if (lifeTicks == DAMAGE_TICK && !hasDealtDamage) {
                dealDamageToArea();
                hasDealtDamage = true;
            }

            if (lifeTicks == EXPLOSION_TICK) {
                createExplosion();
            }
        }

        if (lifeTicks >= MAX_LIFE_TICKS) {
            this.discard();
        }
    }

    private void spawnParticles() {
        for (int i = 0; i < 5; i++) {
            Vec3 pos = this.position().add(this.random.nextGaussian() * 0.5, this.random.nextGaussian() * 0.5, this.random.nextGaussian() * 0.5);
            this.level().addParticle(ParticleTypes.PORTAL, pos.x, pos.y, pos.z, 0, 0, 0);
        }
    }

    private void dealDamageToArea() {
        AABB damageBox = new AABB(
                this.position().x - 1.5, this.position().y - 1.0, this.position().z - 1.5,
                this.position().x + 1.5, this.position().y + 2.0, this.position().z + 5.0
        );

        damageBox = rotateAABB(damageBox, this.getYRot() * (float)Math.PI / 180.0F);

        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, damageBox);
        LivingEntity owner = this.getOwner();

        for (LivingEntity entity : entities) {
            if (entity != owner && entity.isAlive()) {
                entity.hurt(this.level().damageSources().mobProjectile(this, owner), this.getDamage());
                Vec3 lookVector = this.getLookAngle().normalize();
                entity.push(lookVector.x * 1.5, 0.5, lookVector.z * 1.5);
            }
        }
    }

    private void createExplosion() {
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, Level.ExplosionInteraction.NONE);
    }

    private AABB rotateAABB(AABB box, float radians) {
        double centerX = (box.minX + box.maxX) / 2;
        double centerZ = (box.minZ + box.maxZ) / 2;

        double minX = box.minX - centerX;
        double maxX = box.maxX - centerX;
        double minZ = box.minZ - centerZ;
        double maxZ = box.maxZ - centerZ;

        double sin = Math.sin(radians);
        double cos = Math.cos(radians);

        double newMinX = Double.MAX_VALUE;
        double newMaxX = Double.MIN_VALUE;
        double newMinZ = Double.MAX_VALUE;
        double newMaxZ = Double.MIN_VALUE;

        double[][] corners = {{minX, minZ}, {maxX, minZ}, {minX, maxZ}, {maxX, maxZ}};
        for (double[] corner : corners) {
            double x = corner[0] * cos - corner[1] * sin;
            double z = corner[0] * sin + corner[1] * cos;

            newMinX = Math.min(newMinX, x);
            newMaxX = Math.max(newMaxX, x);
            newMinZ = Math.min(newMinZ, z);
            newMaxZ = Math.max(newMaxZ, z);
        }

        return new AABB(
                centerX + newMinX, box.minY, centerZ + newMinZ,
                centerX + newMaxX, box.maxY, centerZ + newMaxZ
        );
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Damage", this.getDamage());
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        compound.putInt("LifeTicks", lifeTicks);
        compound.putBoolean("HasDealtDamage", hasDealtDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setDamage(compound.getFloat("Damage"));
        if (compound.hasUUID("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
        lifeTicks = compound.getInt("LifeTicks");
        hasDealtDamage = compound.getBoolean("HasDealtDamage");
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != this.getOwner();
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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "dimension_kick_controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<DecadeRiderEntity> event) {
        event.getController().setAnimation(DIMENSION_KICK_ANIMATION);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static void trySpawnEffect(Level level, LivingEntity owner, Vec3 direction, float damage) {
        if (!level.isClientSide) {
            Vec3 lookVector = owner.getLookAngle().normalize();
            Vec3 spawnPos = owner.position().add(lookVector.x * 5, owner.getEyeHeight() * 0.5, lookVector.z * 5);

            DecadeRiderEntity entity = new DecadeRiderEntity(level, owner, damage, direction);
            entity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

            entity.setYRot(owner.getYRot());
            entity.setXRot(owner.getXRot());

            level.addFreshEntity(entity);
        }
    }
}