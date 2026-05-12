package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.exaid;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class ExAidSlashEffectEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation SLASH_ANIMATION = RawAnimation.begin().then("animation.exaid_slash_effect.slash", Animation.LoopType.PLAY_ONCE);

    private Entity owner;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 120;
    private Entity hitEntity = null;
    private int followTicks = 0;
    private Entity targetEntity = null;

    private static final EntityDataAccessor<Integer> HIT_ENTITY_ID = SynchedEntityData.defineId(ExAidSlashEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_HIT_ENTITY = SynchedEntityData.defineId(ExAidSlashEffectEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TARGET_ENTITY_ID = SynchedEntityData.defineId(ExAidSlashEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_TARGET_ENTITY = SynchedEntityData.defineId(ExAidSlashEffectEntity.class, EntityDataSerializers.BOOLEAN);

    public ExAidSlashEffectEntity(EntityType<? extends ExAidSlashEffectEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvulnerable(true);
    }

    public ExAidSlashEffectEntity(Level level, Entity owner, Vec3 position, Vec3 direction) {
        super(EntityRegister.EXAID_SLASH_EFFECT.get(), level);
        this.setPos(position);
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yRot = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float xRot = (float) Math.toDegrees(Math.atan2(direction.y, horizontalDistance));
        this.setYRot(yRot);
        this.setXRot(xRot);
        this.owner = owner;
        this.noPhysics = true;
        this.setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(HIT_ENTITY_ID, 0);
        builder.define(HAS_HIT_ENTITY, false);
        builder.define(TARGET_ENTITY_ID, 0);
        builder.define(HAS_TARGET_ENTITY, false);
    }

    @Override
    public void tick() {
        super.tick();

        checkNearbyPlayers();

        lifetime++;
        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (this.level().isClientSide) {
            updateHitEntityFromSyncedData();
            updateTargetEntityFromSyncedData();
        }

        if (targetEntity != null && hitEntity == null && !targetEntity.isRemoved() && targetEntity != owner) {
            double randomOffsetX = (this.level().random.nextDouble() - 0.5) * 1.0;
            double randomOffsetY = (this.level().random.nextDouble() - 0.5) * 0.8;
            double randomOffsetZ = (this.level().random.nextDouble() - 0.5) * 1.0;

            this.setPos(targetEntity.getX() + randomOffsetX,
                    targetEntity.getY() + targetEntity.getBbHeight() * 0.5 + randomOffsetY,
                    targetEntity.getZ() + randomOffsetZ);
            followTicks++;

            if (followTicks > 60) {
                followTicks = 0;
                Vec3 motionAwayFromOwner = getMotionAwayFromOwner();
                this.setDeltaMovement(motionAwayFromOwner);
            }
        } else if (hitEntity != null && !hitEntity.isRemoved()) {
            if (hitEntity != owner) {
                this.setPos(hitEntity.getX(), hitEntity.getY() + hitEntity.getBbHeight() * 0.5, hitEntity.getZ());
            } else {
                this.discard();
                return;
            }
            followTicks++;

            if (followTicks > 30) {
                hitEntity = null;
                followTicks = 0;
                Vec3 motionAwayFromOwner = getMotionAwayFromOwner();
                this.setDeltaMovement(motionAwayFromOwner);
            }
        } else {
            Vec3 currentMotion = this.getDeltaMovement();
            if (currentMotion.length() < 0.5) {
                Vec3 motionAwayFromOwner = getMotionAwayFromOwner();
                this.setDeltaMovement(motionAwayFromOwner);
            } else {
                this.setDeltaMovement(currentMotion.normalize().scale(0.8).add(
                        (this.level().random.nextDouble() - 0.5) * 0.1,
                        0.02 + this.level().random.nextDouble() * 0.05,
                        (this.level().random.nextDouble() - 0.5) * 0.1
                ));
            }

            checkEntityCollision();
        }
    }

    private Vec3 getMotionAwayFromOwner() {
        if (owner != null && !owner.isRemoved()) {
            Vec3 ownerToEffect = this.position().subtract(owner.position());
            if (ownerToEffect.length() > 0.1) {
                return ownerToEffect.normalize().scale(0.5).add(
                        (this.level().random.nextDouble() - 0.5) * 0.3,
                        0.1 + this.level().random.nextDouble() * 0.3,
                        (this.level().random.nextDouble() - 0.5) * 0.3
                );
            }
        }
        return new Vec3(
                (this.level().random.nextDouble() - 0.5) * 0.5,
                0.1 + this.level().random.nextDouble() * 0.3,
                (this.level().random.nextDouble() - 0.5) * 0.5
        );
    }

    public void setTargetEntity(Entity targetEntity) {
        if (targetEntity != null && targetEntity != owner) {
            this.targetEntity = targetEntity;
            if (!this.level().isClientSide) {
                syncTargetEntity(targetEntity);
            }
        } else {
            this.targetEntity = null;
            if (!this.level().isClientSide) {
                syncTargetEntity(null);
            }
        }
    }

    private void syncTargetEntity(Entity entity) {
        if (entity != null) {
            this.entityData.set(TARGET_ENTITY_ID, entity.getId());
            this.entityData.set(HAS_TARGET_ENTITY, true);
        } else {
            this.entityData.set(TARGET_ENTITY_ID, 0);
            this.entityData.set(HAS_TARGET_ENTITY, false);
        }
    }

    private void updateTargetEntityFromSyncedData() {
        if (this.entityData.get(HAS_TARGET_ENTITY)) {
            int entityId = this.entityData.get(TARGET_ENTITY_ID);
            if (entityId > 0) {
                Entity entity = this.level().getEntity(entityId);
                if (entity != null && !entity.isRemoved()) {
                    this.targetEntity = entity;
                } else {
                    this.entityData.set(HAS_TARGET_ENTITY, false);
                    this.entityData.set(TARGET_ENTITY_ID, 0);
                    this.targetEntity = null;
                }
            }
        }
    }

    private void checkEntityCollision() {
        if (hitEntity == null) {
            if (owner != null && this.distanceTo(owner) < 3.0D) {
                return;
            }

            List<Entity> nearbyEntities = this.level().getEntities(this, this.getBoundingBox().inflate(2.0D),
                    entity -> entity instanceof LivingEntity &&
                            entity != owner &&
                            !entity.isSpectator() &&
                            entity.isAlive());

            for (Entity entity : nearbyEntities) {
                if (entity == owner) {
                    continue;
                }

                hitEntity = entity;
                followTicks = 0;

                if (!this.level().isClientSide) {
                    syncHitEntity(hitEntity);
                }

                this.setDeltaMovement(0, 0, 0);

                if (!this.level().isClientSide && owner != null) {
                    if (entity instanceof LivingEntity livingEntity) {
                        if (livingEntity != owner) {
                            net.minecraft.world.damagesource.DamageSource damageSource;
                            if (owner instanceof Player player) {
                                damageSource = level().damageSources().playerAttack(player);
                            } else if (owner instanceof net.minecraft.world.entity.Mob mob) {
                                damageSource = level().damageSources().mobAttack(mob);
                            } else {
                                damageSource = level().damageSources().magic();
                            }

                            livingEntity.hurt(damageSource, 5.0F);
                            livingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    net.minecraft.world.effect.MobEffects.WITHER,
                                    40, 1, false, false
                            ));
                        }
                    }
                }

                break;
            }
        }
    }

    private void syncHitEntity(Entity entity) {
        if (entity != null) {
            this.entityData.set(HIT_ENTITY_ID, entity.getId());
            this.entityData.set(HAS_HIT_ENTITY, true);
        } else {
            this.entityData.set(HIT_ENTITY_ID, 0);
            this.entityData.set(HAS_HIT_ENTITY, false);
        }
    }

    private void updateHitEntityFromSyncedData() {
        if (this.entityData.get(HAS_HIT_ENTITY)) {
            int entityId = this.entityData.get(HIT_ENTITY_ID);
            if (entityId > 0) {
                Entity entity = this.level().getEntity(entityId);
                if (entity != null && !entity.isRemoved()) {
                    this.hitEntity = entity;
                } else {
                    this.entityData.set(HAS_HIT_ENTITY, false);
                    this.entityData.set(HIT_ENTITY_ID, 0);
                    this.hitEntity = null;
                }
            }
        }
    }

    private void checkNearbyPlayers() {
        if (owner != null && owner.isAlive()) {
            double distance = this.distanceTo(owner);
            if (distance < 2.0D) {
                this.discard();
            }
        }
    }

    private PlayState animationPredicate(AnimationState<ExAidSlashEffectEntity> event) {
        event.getController().setAnimation(SLASH_ANIMATION);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::animationPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.lifetime = nbt.getInt("Lifetime");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Lifetime", this.lifetime);
    }

    public static void spawnEffect(Level level, Entity owner, Vec3 direction) {
        if (!level.isClientSide) {
            Vec3 startPos = owner.getEyePosition(1.0F).add(direction.scale(2.5));
            ExAidSlashEffectEntity effect = new ExAidSlashEffectEntity(level, owner, startPos, direction);
            effect.setDeltaMovement(direction.scale(1.2));
            level.addFreshEntity(effect);
        }
    }

    public static void spawnEffectOnTarget(Level level, Entity owner, Entity target) {
        if (!level.isClientSide && target != null) {
            double x = target.getX();
            double y = target.getY() + target.getBbHeight() * 0.5;
            double z = target.getZ();

            Vec3 direction = new Vec3(
                    level.random.nextDouble() - 0.5,
                    level.random.nextDouble() - 0.5,
                    level.random.nextDouble() - 0.5
            ).normalize();

            ExAidSlashEffectEntity effect = new ExAidSlashEffectEntity(level, owner, new Vec3(x, y, z), direction);
            effect.setTargetEntity(target);
            level.addFreshEntity(effect);
        }
    }
}
