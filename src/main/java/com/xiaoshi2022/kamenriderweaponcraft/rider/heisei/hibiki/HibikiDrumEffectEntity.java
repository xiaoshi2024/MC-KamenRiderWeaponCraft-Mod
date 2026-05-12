package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.hibiki;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

public class HibikiDrumEffectEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().then("animation.hibiki_drum.idle", Animation.LoopType.LOOP);
    private static final RawAnimation CHARGE_ANIMATION = RawAnimation.begin().then("animation.hibiki_drum.charge", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation EXPLOSION_ANIMATION = RawAnimation.begin().then("animation.hibiki_drum.explosion", Animation.LoopType.PLAY_ONCE);

    private Entity owner;
    private LivingEntity targetEntity;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 200;
    private boolean isCharging = false;
    private int chargeTicks = 0;
    private static final int CHARGE_DURATION = 60;
    private boolean hasExploded = false;
    private double damageAmount = 45.0;

    private static final EntityDataAccessor<Integer> TARGET_ENTITY_ID = SynchedEntityData.defineId(HibikiDrumEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_TARGET = SynchedEntityData.defineId(HibikiDrumEffectEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CHARGING = SynchedEntityData.defineId(HibikiDrumEffectEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_EXPLODED = SynchedEntityData.defineId(HibikiDrumEffectEntity.class, EntityDataSerializers.BOOLEAN);

    public HibikiDrumEffectEntity(EntityType<? extends HibikiDrumEffectEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvulnerable(true);
    }

    public HibikiDrumEffectEntity(Level level, Entity owner, LivingEntity target) {
        super(EntityRegister.HIBIKI_DRUM_EFFECT.get(), level);
        this.owner = owner;
        this.targetEntity = target;
        this.noPhysics = true;
        this.setInvulnerable(true);

        if (!level.isClientSide) {
            syncTargetEntity(target);
        }

        updatePositionToTarget();
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;

        if (this.level().isClientSide) {
            updateTargetEntityFromSyncedData();
        }

        this.entityData.set(IS_CHARGING, isCharging);
        this.entityData.set(HAS_EXPLODED, hasExploded);

        if (hasExploded) {
            if (lifetime > MAX_LIFETIME - 20) {
                this.discard();
                return;
            }
            return;
        }

        if (targetEntity != null && !targetEntity.isRemoved() && !targetEntity.isDeadOrDying()) {
            applyControlEffect(targetEntity);

            updatePositionToTarget();

            if (!isCharging && lifetime > 20) {
                isCharging = true;
                chargeTicks = 0;
            }

            if (isCharging) {
                chargeTicks++;

                if (chargeTicks >= CHARGE_DURATION) {
                    triggerExplosion();
                }
            }
        } else {
            this.discard();
            return;
        }

        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }
    }

    private void updatePositionToTarget() {
        if (targetEntity != null && !targetEntity.isRemoved()) {
            Vec3 lookVector = targetEntity.getViewVector(1.0F).normalize();

            double offsetDistance = 1.5;
            double heightOffset = targetEntity.getBbHeight() * 0.4;

            Vec3 offsetPos = targetEntity.position()
                    .add(0, heightOffset, 0)
                    .add(lookVector.scale(offsetDistance));

            this.setPos(offsetPos.x, offsetPos.y, offsetPos.z);

            this.setYRot(targetEntity.getYRot() + 180);
            this.setXRot(0);
        }
    }

    private void applyControlEffect(LivingEntity target) {
        if (!level().isClientSide) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20, 3, false, false));
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false));

            target.setDeltaMovement(target.getDeltaMovement().x, Math.min(target.getDeltaMovement().y, 0), target.getDeltaMovement().z);
        }
    }

    private void triggerExplosion() {
        if (hasExploded || level().isClientSide) return;

        hasExploded = true;

        if (targetEntity != null && !targetEntity.isRemoved()) {
            net.minecraft.world.damagesource.DamageSource damageSource;
            if (owner instanceof Player player) {
                damageSource = level().damageSources().playerAttack(player);
            } else if (owner instanceof net.minecraft.world.entity.Mob mob) {
                damageSource = level().damageSources().mobAttack(mob);
            } else {
                damageSource = level().damageSources().magic();
            }

            targetEntity.hurt(damageSource, (float) damageAmount);

            Vec3 lookVector = targetEntity.getViewVector(1.0F).normalize();
            targetEntity.setDeltaMovement(lookVector.scale(-2.0));

            targetEntity.setRemainingFireTicks(100);
        }

        level().playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(), net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);    }

    public void setTargetEntity(LivingEntity targetEntity) {
        this.targetEntity = targetEntity;
        if (!this.level().isClientSide) {
            syncTargetEntity(targetEntity);
        }
    }

    private void syncTargetEntity(LivingEntity entity) {
        if (entity != null) {
            this.entityData.set(TARGET_ENTITY_ID, entity.getId());
            this.entityData.set(HAS_TARGET, true);
        } else {
            this.entityData.set(TARGET_ENTITY_ID, 0);
            this.entityData.set(HAS_TARGET, false);
        }
    }

    private void updateTargetEntityFromSyncedData() {
        if (this.entityData.get(HAS_TARGET)) {
            int entityId = this.entityData.get(TARGET_ENTITY_ID);
            if (entityId > 0) {
                Entity entity = this.level().getEntity(entityId);
                if (entity instanceof LivingEntity livingEntity && !entity.isRemoved()) {
                    this.targetEntity = livingEntity;
                } else {
                    this.entityData.set(HAS_TARGET, false);
                    this.entityData.set(TARGET_ENTITY_ID, 0);
                    this.targetEntity = null;
                }
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.targetEntity = null;
        this.owner = null;
    }

    private PlayState animationPredicate(AnimationState<HibikiDrumEffectEntity> event) {
        if (this.entityData.get(HAS_EXPLODED)) {
            event.getController().setAnimation(EXPLOSION_ANIMATION);
        } else if (this.entityData.get(IS_CHARGING)) {
            event.getController().setAnimation(CHARGE_ANIMATION);
        } else {
            event.getController().setAnimation(IDLE_ANIMATION);
        }
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TARGET_ENTITY_ID, 0);
        builder.define(HAS_TARGET, false);
        builder.define(IS_CHARGING, false);
        builder.define(HAS_EXPLODED, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.lifetime = nbt.getInt("Lifetime");
        this.isCharging = nbt.getBoolean("IsCharging");
        this.chargeTicks = nbt.getInt("ChargeTicks");
        this.hasExploded = nbt.getBoolean("HasExploded");
        this.damageAmount = nbt.getDouble("DamageAmount");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Lifetime", this.lifetime);
        nbt.putBoolean("IsCharging", this.isCharging);
        nbt.putInt("ChargeTicks", this.chargeTicks);
        nbt.putBoolean("HasExploded", this.hasExploded);
        nbt.putDouble("DamageAmount", this.damageAmount);
    }

    public static void spawnEffect(Level level, Entity owner, LivingEntity target) {
        if (!level.isClientSide && target != null && !target.isRemoved()) {
            HibikiDrumEffectEntity effect = new HibikiDrumEffectEntity(level, owner, target);
            level.addFreshEntity(effect);
        }
    }

    public boolean isCharging() {
        return this.entityData.get(IS_CHARGING);
    }

    public boolean hasExploded() {
        return this.entityData.get(HAS_EXPLODED);
    }

    public LivingEntity getTargetEntity() {
        return this.targetEntity;
    }
}