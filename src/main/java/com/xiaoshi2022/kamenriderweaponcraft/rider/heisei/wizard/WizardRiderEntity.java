package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.wizard;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class WizardRiderEntity extends Projectile implements GeoEntity {
    public enum DragonMagicType {
        FlameDragon,
        WaterDragon,
        HurricaneDragon,
        LandDragon
    }

    private static final EntityDataAccessor<Integer> DRAGON_MAGIC_TYPE = SynchedEntityData.defineId(WizardRiderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(WizardRiderEntity.class, EntityDataSerializers.FLOAT);

    private static final RawAnimation FLAME_DRAGON_ANIMATION = RawAnimation.begin().then("flamedragon", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation WATER_DRAGON_ANIMATION = RawAnimation.begin().then("waterdragon", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation HURRICANE_DRAGON_ANIMATION = RawAnimation.begin().then("hurricanedragon", Animation.LoopType.PLAY_ONCE);
    private static final RawAnimation LAND_DRAGON_ANIMATION = RawAnimation.begin().then("landdragon", Animation.LoopType.PLAY_ONCE);

    private int lifeTicks = 80;
    private boolean hasCollided = false;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public DragonMagicType getDragonMagicType() {
        return DragonMagicType.values()[this.getEntityData().get(DRAGON_MAGIC_TYPE)];
    }

    public void setDragonMagicType(DragonMagicType type) {
        this.getEntityData().set(DRAGON_MAGIC_TYPE, type.ordinal());
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    public void setDamage(float value) {
        this.getEntityData().set(DAMAGE, value);
    }

    public WizardRiderEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public static void trySpawnEffect(Level level, LivingEntity shooter, Vec3 direction, float damage, DragonMagicType dragonMagicType) {
        if (level.isClientSide) return;

        WizardRiderEntity entity = new WizardRiderEntity(EntityRegister.WIZARD_RIDER.get(), level);
        entity.setOwner(shooter);
        entity.setDragonMagicType(dragonMagicType);
        entity.setDamage(damage);
        entity.setPos(shooter.getEyePosition().add(direction.normalize().scale(1.5)));
        entity.shoot(direction.x, direction.y, direction.z, 1.0f, 0);
        level.addFreshEntity(entity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DRAGON_MAGIC_TYPE, DragonMagicType.FlameDragon.ordinal());
        builder.define(DAMAGE, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();

        if (--this.lifeTicks <= 0) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide && !hasCollided) {
            List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(3.0));

            for (Entity entity : entities) {
                Entity ownerEntity = this.getOwner();
                if (entity instanceof LivingEntity livingEntity && entity != ownerEntity && (ownerEntity == null || !entity.isAlliedTo(ownerEntity))) {
                    this.hasCollided = true;

                    switch (this.getDragonMagicType()) {
                        case FlameDragon:
                            handleFlameDragonEffect(livingEntity);
                            break;
                        case WaterDragon:
                            handleWaterDragonEffect(livingEntity);
                            break;
                        case HurricaneDragon:
                            handleHurricaneDragonEffect(livingEntity);
                            break;
                        case LandDragon:
                            handleLandDragonEffect(livingEntity);
                            break;
                    }

                    this.lifeTicks = Math.min(this.lifeTicks, 20);
                    break;
                }
            }
        }

        if (!this.hasCollided) {
            Vec3 deltaMovement = this.getDeltaMovement();
            this.setDeltaMovement(deltaMovement.multiply(0.98, 0.98, 0.98));
            this.setPos(this.getX() + deltaMovement.x, this.getY() + deltaMovement.y, this.getZ() + deltaMovement.z);
        }
    }

    private void handleFlameDragonEffect(LivingEntity target) {
        Entity ownerEntity = this.getOwner();
        LivingEntity owner = ownerEntity instanceof LivingEntity ? (LivingEntity) ownerEntity : null;
        if (owner != null) {
            target.hurt(this.level().damageSources().mobProjectile(this, owner), this.getDamage());
        } else {
            target.hurt(this.level().damageSources().magic(), this.getDamage());
        }
        target.setRemainingFireTicks(160);  // 8秒 = 160 ticks

        for (int i = 0; i < 3; i++) {
            SmallFireball fireball = new SmallFireball(this.level(), this.getX(), this.getY(), this.getZ(),
                    new Vec3((target.getX() - this.getX() + (this.random.nextDouble() - 0.5) * 2) * 0.2,
                            (target.getY() - this.getY() + (this.random.nextDouble() - 0.5) * 2) * 0.2,
                            (target.getZ() - this.getZ() + (this.random.nextDouble() - 0.5) * 2) * 0.2));
            fireball.setPos(this.getX(), this.getY() + 1.0, this.getZ());
            this.level().addFreshEntity(fireball);
        }

        AABB area = this.getBoundingBox().inflate(5.0);
        List<Entity> nearbyEntities = this.level().getEntities(this, area);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity nearbyLiving &&
                    entity != ownerEntity &&
                    (ownerEntity == null || !entity.isAlliedTo(ownerEntity)) &&
                    entity != target) {
                if (owner != null) {
                    nearbyLiving.hurt(this.level().damageSources().indirectMagic(this, owner), this.getDamage() * 0.5f);
                } else {
                    nearbyLiving.hurt(this.level().damageSources().magic(), this.getDamage() * 0.5f);
                }
                nearbyLiving.setRemainingFireTicks(80);  // 4秒 = 80 ticks
            }
        }
    }

    private void handleWaterDragonEffect(LivingEntity target) {
        Entity ownerEntity = this.getOwner();
        LivingEntity owner = ownerEntity instanceof LivingEntity ? (LivingEntity) ownerEntity : null;
        if (owner != null) {
            target.hurt(this.level().damageSources().mobProjectile(this, owner), this.getDamage() * 1.1f);
        } else {
            target.hurt(this.level().damageSources().magic(), this.getDamage() * 1.1f);
        }

        int freezeRadius = 5;
        BlockPos centerPos = this.blockPosition();

        for (int dx = -freezeRadius; dx <= freezeRadius; dx++) {
            for (int dy = -freezeRadius; dy <= freezeRadius; dy++) {
                for (int dz = -freezeRadius; dz <= freezeRadius; dz++) {
                    BlockPos checkPos = centerPos.offset(dx, dy, dz);
                    if (this.level().getBlockState(checkPos).getBlock() == Blocks.WATER) {
                        this.level().setBlockAndUpdate(checkPos, Blocks.ICE.defaultBlockState());
                    }
                }
            }
        }

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));

        if (target.isOnFire()) {
            target.clearFire();
        }

        AABB area = this.getBoundingBox().inflate(4.0);
        List<Entity> nearbyEntities = this.level().getEntities(this, area);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity nearbyLiving && entity != ownerEntity) {
                nearbyLiving.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                if (nearbyLiving.isOnFire()) {
                    nearbyLiving.clearFire();
                }
            }
        }
    }

    private void handleHurricaneDragonEffect(LivingEntity target) {
        Entity ownerEntity = this.getOwner();
        LivingEntity owner = ownerEntity instanceof LivingEntity ? (LivingEntity) ownerEntity : null;
        if (owner != null) {
            target.hurt(this.level().damageSources().mobProjectile(this, owner), this.getDamage() * 0.8f);
        } else {
            target.hurt(this.level().damageSources().magic(), this.getDamage() * 0.8f);
        }

        AABB area = this.getBoundingBox().inflate(12.0);
        List<Entity> nearbyEntities = this.level().getEntities(this, area);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingTarget && entity != ownerEntity) {
                livingTarget.setDeltaMovement(
                        livingTarget.getDeltaMovement().x() * 0.2,
                        3.0 + this.random.nextDouble() * 2.0,
                        livingTarget.getDeltaMovement().z() * 0.2
                );
                livingTarget.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 2));
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 1));
                livingTarget.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
        }

        Vec3 center = this.position();
        float explosionRadius = 3.0F;

        this.level().explode(this, center.x, center.y, center.z, explosionRadius, Level.ExplosionInteraction.NONE);

        float explosionDamage = this.getDamage() * 0.5f;

        AABB explosionArea = this.getBoundingBox().inflate(explosionRadius);
        List<LivingEntity> explosionEntities = this.level().getEntitiesOfClass(LivingEntity.class, explosionArea,
                living -> living != ownerEntity && living.isAlive());

        for (LivingEntity entity : explosionEntities) {
            double distance = entity.distanceTo(this);
            float finalDamage = (float) (explosionDamage * (1.0 - distance / (explosionRadius * 2.0)));
            if (finalDamage > 0) {
                if (owner != null) {
                    entity.hurt(this.level().damageSources().indirectMagic(this, owner), finalDamage);
                } else {
                    entity.hurt(this.level().damageSources().magic(), finalDamage);
                }
            }
        }
    }

    private void handleLandDragonEffect(LivingEntity target) {
        Entity ownerEntity = this.getOwner();
        LivingEntity owner = ownerEntity instanceof LivingEntity ? (LivingEntity) ownerEntity : null;
        if (owner != null) {
            target.hurt(this.level().damageSources().mobProjectile(this, owner), this.getDamage() * 1.3f);
        } else {
            target.hurt(this.level().damageSources().magic(), this.getDamage() * 1.3f);
        }

        int centerX = (int) target.getX();
        int centerY = (int) Math.floor(target.getY());
        int centerZ = (int) target.getZ();
        int wallSize = 4;

        List<BlockPos> wallBlocks = new ArrayList<>();

        for (int x = centerX - wallSize; x <= centerX + wallSize; x++) {
            for (int y = centerY; y <= centerY + 4; y++) {
                for (int zOffset : new int[]{-wallSize, wallSize}) {
                    BlockPos wallPos = new BlockPos(x, y, centerZ + zOffset);
                    if (this.level().isEmptyBlock(wallPos) || this.level().getBlockState(wallPos).getDestroySpeed(this.level(), wallPos) >= 0) {
                        this.level().setBlockAndUpdate(wallPos, Blocks.STONE.defaultBlockState());
                        wallBlocks.add(wallPos);
                    }
                }
            }
        }

        for (int z = centerZ - wallSize + 1; z <= centerZ + wallSize - 1; z++) {
            for (int y = centerY; y <= centerY + 4; y++) {
                for (int xOffset : new int[]{-wallSize, wallSize}) {
                    BlockPos wallPos = new BlockPos(centerX + xOffset, y, z);
                    if (this.level().isEmptyBlock(wallPos) || this.level().getBlockState(wallPos).getDestroySpeed(this.level(), wallPos) >= 0) {
                        this.level().setBlockAndUpdate(wallPos, Blocks.STONE.defaultBlockState());
                        wallBlocks.add(wallPos);
                    }
                }
            }
        }

        this.level().getServer().execute(() -> {
            Entity lambdaOwnerEntity = this.getOwner();
            LivingEntity explosionOwner = lambdaOwnerEntity instanceof LivingEntity ? (LivingEntity) lambdaOwnerEntity : null;
            Explosion explosion = new Explosion(
                    this.level(),
                    explosionOwner,
                    target.getX(),
                    target.getY() + 1,
                    target.getZ(),
                    4.0f,
                    false,
                    Explosion.BlockInteraction.DESTROY
            );

            explosion.explode();
            explosion.finalizeExplosion(true);

            AABB explosionArea = target.getBoundingBox().inflate(6.0);
            List<Entity> explosionEntities = this.level().getEntities(target, explosionArea);

            for (Entity explosionTarget : explosionEntities) {
                if (explosionTarget instanceof LivingEntity livingExplosionTarget) {
                    double distance = livingExplosionTarget.distanceTo(target);
                    if (distance > 0) {
                        float explosionDamage = (float) (this.getDamage() * 0.7f * (1.0 - distance / 6.0));
                        if (explosionDamage > 0) {
                            livingExplosionTarget.hurt(this.level().damageSources().explosion(explosion), explosionDamage);
                        }
                    }

                    livingExplosionTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                    livingExplosionTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                }
            }
        });

        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 180, 3));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("DragonMagicType")) {
            this.setDragonMagicType(DragonMagicType.values()[compound.getInt("DragonMagicType")]);
        }
        if (compound.contains("Damage")) {
            this.setDamage(compound.getFloat("Damage"));
        }
        if (compound.contains("LifeTicks")) {
            this.lifeTicks = compound.getInt("LifeTicks");
        }
        if (compound.contains("HasCollided")) {
            this.hasCollided = compound.getBoolean("HasCollided");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("DragonMagicType", this.getDragonMagicType().ordinal());
        compound.putFloat("Damage", this.getDamage());
        compound.putInt("LifeTicks", this.lifeTicks);
        compound.putBoolean("HasCollided", this.hasCollided);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "dragon_controller", 0, this::animationPredicate));
    }

    private PlayState animationPredicate(AnimationState<WizardRiderEntity> state) {
        WizardRiderEntity entity = state.getAnimatable();

        switch (entity.getDragonMagicType()) {
            case FlameDragon:
                state.getController().setAnimation(FLAME_DRAGON_ANIMATION);
                break;
            case WaterDragon:
                state.getController().setAnimation(WATER_DRAGON_ANIMATION);
                break;
            case HurricaneDragon:
                state.getController().setAnimation(HURRICANE_DRAGON_ANIMATION);
                break;
            case LandDragon:
                state.getController().setAnimation(LAND_DRAGON_ANIMATION);
                break;
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}