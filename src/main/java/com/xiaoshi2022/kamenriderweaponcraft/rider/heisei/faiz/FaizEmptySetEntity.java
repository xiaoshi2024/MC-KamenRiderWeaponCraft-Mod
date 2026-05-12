package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.faiz;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FaizEmptySetEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation APPEAR_ANIMATION = RawAnimation.begin().then("appear", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().then("idle", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation DISAPPEAR_ANIMATION = RawAnimation.begin().then("disappear", Animation.LoopType.PLAY_ONCE);

    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(FaizEmptySetEntity.class, EntityDataSerializers.INT);
    private static final int MAX_LIFETIME = 40;

    public FaizEmptySetEntity(EntityType<? extends FaizEmptySetEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvulnerable(true);
    }

    public FaizEmptySetEntity(Level level, LivingEntity enemy) {
        super(EntityRegister.FAIZ_EMPTY_SET.get(), level);
        this.noPhysics = true;
        this.setInvulnerable(true);

        if (enemy != null) {
            this.setPos(enemy.getX(), enemy.getY() + enemy.getBbHeight() * 0.5, enemy.getZ());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LIFETIME, MAX_LIFETIME);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(LIFETIME, compound.getInt("Lifetime"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Lifetime", this.entityData.get(LIFETIME));
    }

    @Override
    public void tick() {
        super.tick();

        int currentLifetime = this.entityData.get(LIFETIME) - 1;
        this.entityData.set(LIFETIME, currentLifetime);

        if (currentLifetime <= 0) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private PlayState predicate(AnimationState<FaizEmptySetEntity> event) {
        int lifetime = this.entityData.get(LIFETIME);

        if (lifetime > MAX_LIFETIME - 10) {
            event.getController().setAnimation(APPEAR_ANIMATION);
        } else if (lifetime > 10) {
            event.getController().setAnimation(IDLE_ANIMATION);
        } else {
            event.getController().setAnimation(DISAPPEAR_ANIMATION);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public float getAnimationProgress() {
        return 1.0F - (float)this.entityData.get(LIFETIME) / (float)MAX_LIFETIME;
    }

    public int getRemainingTicks() {
        return this.entityData.get(LIFETIME);
    }

    public int getMaxTicks() {
        return MAX_LIFETIME;
    }

    public boolean isDisappearing() {
        return this.entityData.get(LIFETIME) <= 10;
    }

    public boolean isAppearing() {
        return this.entityData.get(LIFETIME) > MAX_LIFETIME - 10;
    }

    public static void spawnEmptySet(Level level, LivingEntity enemy) {
        if (!level.isClientSide) {
            FaizEmptySetEntity emptySet = new FaizEmptySetEntity(level, enemy);
            level.addFreshEntity(emptySet);
        }
    }
}