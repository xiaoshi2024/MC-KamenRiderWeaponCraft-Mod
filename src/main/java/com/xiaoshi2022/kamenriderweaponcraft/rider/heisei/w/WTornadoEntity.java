package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.w;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WTornadoEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation SPIN_ANIMATION = RawAnimation.begin().then("spin", Animation.LoopType.LOOP);
    private Vec3 targetDirection = Vec3.ZERO;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 60;
    private LivingEntity owner = null;

    public WTornadoEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public WTornadoEntity(Level level) {
        super(EntityRegister.W_TORNADO.get(), level);
    }

    public void setDirection(Vec3 direction) {
        this.targetDirection = direction != null ? direction.normalize() : Vec3.ZERO;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;
        if (lifetime >= MAX_LIFETIME) {
            resetAffectedEntitiesGravity();
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        if (targetDirection != null) {
            double speed = 0.5;
            setDeltaMovement(targetDirection.scale(speed));
        }

        if (!level().isClientSide) {
            double range = 2.0;
            level().getEntitiesOfClass(LivingEntity.class,
                            getBoundingBox().inflate(range),
                            entity -> entity != owner)
                    .forEach(entity -> {
                        entity.hurt(level().damageSources().generic(), 5.0f);
                        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                                40, 1));

                        Vec3 moveDirection = (targetDirection != null && targetDirection.lengthSqr() > 0.01) ? targetDirection : Vec3.ZERO;
                        entity.setDeltaMovement(
                                moveDirection.x * 0.5,
                                0.4,
                                moveDirection.z * 0.5
                        );
                        entity.fallDistance = 0.0f;
                        entity.setNoGravity(true);

                        entity.setAirSupply(entity.getMaxAirSupply());
                    });

            resetOutOfRangeEntitiesGravity(range);
        } else {
            level().getEntitiesOfClass(LivingEntity.class,
                            getBoundingBox().inflate(2.0),
                            entity -> entity != owner)
                    .forEach(entity -> {
                        level().addParticle(
                                net.minecraft.core.particles.ParticleTypes.CLOUD,
                                entity.getX() + (random.nextDouble() - 0.5) * 2.0,
                                entity.getY() + random.nextDouble() * 2.0,
                                entity.getZ() + (random.nextDouble() - 0.5) * 2.0,
                                (random.nextDouble() - 0.5) * 0.5,
                                random.nextDouble() * 0.5,
                                (random.nextDouble() - 0.5) * 0.5
                        );
                    });
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compoundTag) {
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compoundTag) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "spin_controller", 0, state ->
                state.setAndContinue(SPIN_ANIMATION)));
    }

    public static void trySpawnTornado(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            WTornadoEntity tornado = new WTornadoEntity(level);
            tornado.setDirection(direction);
            tornado.setOwner(shooter);
            tornado.setPos(shooter.getEyePosition().add(direction.scale(1.0)));
            level.addFreshEntity(tornado);
        }
    }

    public static void trySpawnTornado(Level level, Player player, Vec3 direction) {
        trySpawnTornado(level, (LivingEntity) player, direction);
    }

    private void resetOutOfRangeEntitiesGravity(double range) {
        double searchRange = range * 3.0;
        level().getEntitiesOfClass(LivingEntity.class,
                        getBoundingBox().inflate(searchRange),
                        entity -> entity != owner && entity.isNoGravity() &&
                                !getBoundingBox().inflate(range).contains(entity.position()))
                .forEach(entity -> {
                    if (entity.getAirSupply() > entity.getMaxAirSupply() * 0.8) {
                        entity.setNoGravity(false);
                        Vec3 motion = entity.getDeltaMovement();
                        entity.setDeltaMovement(motion.x, 0.1, motion.z);
                    }
                });
    }

    private void resetAffectedEntitiesGravity() {
        double searchRange = 5.0;
        level().getEntitiesOfClass(LivingEntity.class,
                        getBoundingBox().inflate(searchRange),
                        entity -> entity != owner && entity.isNoGravity())
                .forEach(entity -> {
                    entity.setNoGravity(false);
                    Vec3 motion = entity.getDeltaMovement();
                    entity.setDeltaMovement(motion.x, 0.1, motion.z);
                });
    }
}