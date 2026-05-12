package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.gaim;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class GaimLockSeedEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation MOVE_ANIMATION = RawAnimation.begin().then("move", Animation.LoopType.LOOP);
    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().then("attack", Animation.LoopType.PLAY_ONCE);

    private Entity owner;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 100;
    private Vec3 attackDirection;
    private float damage;
    private boolean hasAttacked = false;
    private int removeDelay = 0;

    private static final EntityDataAccessor<String> LOCK_SEED_TYPE =
            SynchedEntityData.defineId(GaimLockSeedEntity.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<Boolean> ATTACKED =
            SynchedEntityData.defineId(GaimLockSeedEntity.class, EntityDataSerializers.BOOLEAN);

    public GaimLockSeedEntity(EntityType<? extends GaimLockSeedEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvulnerable(true);
        this.attackDirection = new Vec3(0, 0, 1);
    }

    public GaimLockSeedEntity(Level level, LivingEntity owner, Vec3 position, Vec3 direction, String lockSeedType, float damage) {
        super(EntityRegister.GAIM_LOCK_SEED.get(), level);
        this.setPos(position);
        this.owner = owner;
        this.attackDirection = direction.normalize();
        this.setLockSeedType(lockSeedType);
        this.damage = damage * 0.75F;
        this.noPhysics = true;
        this.setInvulnerable(true);

        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yRot = (float)Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float xRot = (float)Math.toDegrees(Math.atan2(direction.y, horizontalDistance));
        this.setYRot(yRot);
        this.setXRot(xRot);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LOCK_SEED_TYPE, "ORANGE");
        builder.define(ATTACKED, false);
    }

    public String getLockSeedType() {
        return this.entityData.get(LOCK_SEED_TYPE);
    }

    public void setLockSeedType(String type) {
        this.entityData.set(LOCK_SEED_TYPE, type);
    }

    public boolean hasAttacked() {
        return this.entityData.get(ATTACKED);
    }

    public void setAttacked(boolean attacked) {
        this.entityData.set(ATTACKED, attacked);
        this.hasAttacked = attacked;
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;

        if (removeDelay > 0) {
            removeDelay--;
            if (removeDelay <= 0) {
                this.discard();
                return;
            }
        } else if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (attackDirection != null) {
            this.move(MoverType.SELF, attackDirection.scale(0.4));
        }

        checkCollisions();

        if (this.getLockSeedType().equals("ORANGE")) {
            cutEntitiesOnCollision();
        }
    }

    private void cutEntitiesOnCollision() {
        if (level().isClientSide || owner == null) {
            return;
        }

        AABB collisionBox = this.getBoundingBox().inflate(0.3);

        for (Entity entity : level().getEntities(this, collisionBox)) {
            if (entity instanceof LivingEntity livingEntity && entity != owner) {
                if (isHostileTarget(livingEntity)) {
                    float cutDamage = damage * 0.3f;

                    DamageSource damageSource;
                    if (owner instanceof Player player) {
                        damageSource = level().damageSources().playerAttack(player);
                    } else if (owner instanceof net.minecraft.world.entity.Mob mob) {
                        damageSource = level().damageSources().mobAttack(mob);
                    } else {
                        damageSource = level().damageSources().magic();
                    }

                    livingEntity.hurt(damageSource, cutDamage);
                }
            }
        }
    }

    private boolean isHostileTarget(LivingEntity entity) {
        if (owner instanceof Player player) {
            if (entity instanceof Player targetPlayer) {
                if (player.canHarmPlayer(targetPlayer)) {
                    return true;
                }
                net.minecraft.world.scores.Team playerTeam = player.getTeam();
                net.minecraft.world.scores.Team targetTeam = targetPlayer.getTeam();
                if (playerTeam != null && targetTeam != null && !playerTeam.equals(targetTeam)) {
                    return true;
                }
            }

            if (entity.getType().getCategory().isFriendly() == false && entity.isAlive()) {
                return true;
            }
        } else if (owner instanceof net.minecraft.world.entity.Mob mob) {
            return mob.canAttack(entity) && entity.isAlive();
        }
        return entity.isAlive();
    }

    private void checkCollisions() {
        if (hasAttacked()) {
            return;
        }

        BlockPos currentPos = this.blockPosition();

        if (!level().isClientSide) {
            BlockState currentState = level().getBlockState(currentPos);
            if (!currentState.isAir() && currentState.getCollisionShape(level(), currentPos).isEmpty() == false) {
                onCollision();
                return;
            }

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos adjacentPos = currentPos.offset(x, y, z);
                        BlockState adjacentState = level().getBlockState(adjacentPos);
                        if (!adjacentState.isAir() &&
                                adjacentState.getCollisionShape(level(), adjacentPos).isEmpty() == false &&
                                adjacentPos.distSqr(currentPos) <= 2.0) {
                            onCollision();
                            return;
                        }
                    }
                }
            }
        }

        AABB boundingBox = this.getBoundingBox().inflate(0.2, 0.2, 0.2);
        for (Entity entity : this.level().getEntities(this, boundingBox)) {
            if (entity instanceof LivingEntity && entity != owner) {
                onCollision();
                return;
            }
        }
    }

    private void onCollision() {
        this.setAttacked(true);

        if (this.getLockSeedType().equals("PINEAPPLE")) {
            if (!level().isClientSide) {
                float explosionPower = damage / 3.0f;
                level().explode(this, this.getX(), this.getY(), this.getZ(),
                        explosionPower, Level.ExplosionInteraction.MOB);
            }

            this.removeDelay = 5;
        } else if (this.getLockSeedType().equals("ORANGE")) {
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.setLockSeedType(compound.getString("LockSeedType"));
        this.setAttacked(compound.getBoolean("Attacked"));

        if (compound.contains("Damage")) {
            this.damage = compound.getFloat("Damage");
        }

        if (compound.hasUUID("OwnerUUID")) {
            UUID ownerUUID = compound.getUUID("OwnerUUID");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putString("LockSeedType", this.getLockSeedType());
        compound.putBoolean("Attacked", this.hasAttacked());

        compound.putFloat("Damage", this.damage);

        if (this.owner != null) {
            compound.putUUID("OwnerUUID", this.owner.getUUID());
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<GaimLockSeedEntity> event) {
        if (this.hasAttacked()) {
            event.getController().setAnimation(ATTACK_ANIMATION);
        } else {
            event.getController().setAnimation(MOVE_ANIMATION);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static void spawnLockSeed(Level level, LivingEntity owner, Vec3 direction, String lockSeedType, float damage) {
        if (!level.isClientSide) {
            Vec3 spawnPos = owner.getEyePosition().add(direction.scale(1.5));

            GaimLockSeedEntity lockSeed = new GaimLockSeedEntity(level, owner, spawnPos, direction, lockSeedType, damage);

            level.addFreshEntity(lockSeed);
        }
    }

    public static void spawnLockSeed(Level level, Player player, Vec3 direction, String lockSeedType, float damage) {
        spawnLockSeed(level, (LivingEntity) player, direction, lockSeedType, damage);
    }
}