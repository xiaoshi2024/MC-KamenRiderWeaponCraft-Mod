package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.ghost;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class GhostHeroicSoulEntity extends AbstractHurtingProjectile implements GeoEntity {
    private static final RawAnimation MOVE_ANIMATION = RawAnimation.begin().thenPlay("move");
    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay("attack");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int lifetime = 0;
    private static final int MAX_LIFETIME = 100;

    private Vec3 attackDirection;
    private int soulColor;
    private float damage;
    private boolean isFireDamage;

    private int attackCooldown = 0;
    private static final int DEFAULT_ATTACK_COOLDOWN = 10;
    private int health = 20;
    private static final int MAX_HEALTH = 20;
    private int postAttackLifetime = -1;
    private static final int DEFAULT_POST_ATTACK_LIFETIME = 40;

    private UUID ownerUUID = null;

    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(GhostHeroicSoulEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ATTACKED = SynchedEntityData.defineId(GhostHeroicSoulEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> SOUL_TYPE = SynchedEntityData.defineId(GhostHeroicSoulEntity.class, EntityDataSerializers.STRING);

    public GhostHeroicSoulEntity(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    private GhostHeroicSoulEntity(Level level, LivingEntity owner, Vec3 direction, int color, float damage, boolean isFireDamage) {
        super(EntityRegister.GHOST_HEROIC_SOUL.get(), level);
        this.setOwner(owner);
        this.noPhysics = true;
        this.setPos(owner.getEyePosition().add(direction.scale(1.0)));
        this.shoot(direction.x, direction.y, direction.z, 0.6f, 0.0f);
        this.attackDirection = direction;
        this.soulColor = color;
        this.damage = damage * 0.75F;
        this.isFireDamage = isFireDamage;
        this.setOwnerUUID(owner.getUUID());
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
        this.entityData.set(COLOR, color);
        this.entityData.set(ATTACKED, false);
        this.setSoulType("MUSASHI");
        this.health = MAX_HEALTH;
        this.setVisualFire(false);
        this.setRemainingFireTicks(0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COLOR, 0xFFFFFF);
        builder.define(ATTACKED, false);
        builder.define(SOUL_TYPE, "MUSASHI");
    }

    public int getSoulColor() {
        return this.entityData.get(COLOR);
    }

    public void setSoulColor(int color) {
        this.entityData.set(COLOR, color);
        this.soulColor = color;
    }

    public boolean hasAttacked() {
        return this.entityData.get(ATTACKED);
    }

    public void setAttacked(boolean value) {
        this.entityData.set(ATTACKED, value);
    }

    public String getSoulType() {
        return this.entityData.get(SOUL_TYPE);
    }

    public void setSoulType(String type) {
        this.entityData.set(SOUL_TYPE, type);
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    public static void trySpawnEffect(Level level, LivingEntity owner, Vec3 direction, int color, float damage, boolean isFireDamage, String soulType) {
        if (!level.isClientSide) {
            GhostHeroicSoulEntity soulEntity = new GhostHeroicSoulEntity(level, owner, direction, color, damage, isFireDamage);
            soulEntity.setSoulType(soulType);
            level.addFreshEntity(soulEntity);
        }
    }

    @Override
    public void tick() {
        this.clearFire();
        this.setVisualFire(false);

        lifetime++;

        if (trackingCooldown > 0) {
            trackingCooldown--;
        }

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        if (hasAttacked() && postAttackLifetime == -1) {
            postAttackLifetime = DEFAULT_POST_ATTACK_LIFETIME;
        }

        if (postAttackLifetime > 0) {
            postAttackLifetime--;
        }

        updateTrackingTarget();

        if (health <= 0) {
            this.discard();
            return;
        }

        if (postAttackLifetime == 0) {
            this.discard();
            return;
        }

        if (!hasAttacked() && lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (trackingTarget != null && trackingTarget.isAlive()) {
            Vec3 targetPos = trackingTarget.position().add(0, trackingTarget.getBbHeight() / 2, 0);
            Vec3 entityPos = this.position();
            Vec3 direction = targetPos.subtract(entityPos).normalize();
            this.setDeltaMovement(direction.scale(0.4));
            this.lookAt(targetPos);
        } else if (attackDirection != null) {
            this.setDeltaMovement(attackDirection.scale(0.4));
        } else {
            this.setDeltaMovement(new Vec3(0, 0.02, 0));
        }

        super.tick();
        this.clearFire();
        this.setVisualFire(false);

        if (this.level().isClientSide) {
            if (lifetime % 2 == 0) {
                double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetY = (this.random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;

                float r = ((soulColor >> 16) & 0xFF) / 255.0F;
                float g = ((soulColor >> 8) & 0xFF) / 255.0F;
                float b = (soulColor & 0xFF) / 255.0F;
                ParticleOptions particleType = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, r, g, b);
                this.level().addParticle(particleType,
                        this.getX() + offsetX,
                        this.getY() + 0.5 + offsetY,
                        this.getZ() + offsetZ,
                        0, 0, 0);
            }
        } else {
            checkAndAttackEntities();

            if (lifetime % 20 == 0) {
                makeHostilesAttackThisEntity();
            }
        }
    }

    private void lookAt(Vec3 targetPos) {
        Vec3 entityPos = this.position();
        double dx = targetPos.x - entityPos.x;
        double dz = targetPos.z - entityPos.z;
        double yaw = Math.atan2(dz, dx) * (180 / Math.PI) - 90.0;
        this.setYRot((float) yaw);
        this.setYBodyRot((float) yaw);
        this.setYHeadRot((float) yaw);
    }

    private LivingEntity trackingTarget = null;
    private int trackingCooldown = 0;

    private void updateTrackingTarget() {
        if (trackingCooldown > 0) {
            if (trackingTarget != null && (!trackingTarget.isAlive() ||
                    trackingTarget.distanceToSqr(this) > 25.0)) {
                trackingTarget = null;
            }
            trackingCooldown--;
            return;
        }

        double searchRange = 10.0;
        Entity owner = getOwner();

        LivingEntity nearestTarget = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(searchRange),
                e -> owner != null && e != owner && e.isAlive() && isHostileTarget(e, owner))
                .stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);

        if (nearestTarget != null && nearestTarget != trackingTarget) {
            trackingTarget = nearestTarget;
            trackingCooldown = 20;
        }
    }

    private void checkAndAttackEntities() {
        if (attackCooldown > 0) {
            return;
        }

        double range = 2.0;
        Entity owner = getOwner();

        if (owner != null && trackingTarget != null && trackingTarget.isAlive() && this.distanceToSqr(trackingTarget) <= range * range) {
            DamageSource damageSource;
            if (owner instanceof Player player) {
                damageSource = this.level().damageSources().playerAttack(player);
            } else if (owner instanceof Mob mob) {
                damageSource = this.level().damageSources().mobAttack(mob);
            } else {
                damageSource = this.level().damageSources().generic();
            }
            trackingTarget.hurt(damageSource, damage);

            if (isFireDamage) {
                trackingTarget.setRemainingFireTicks(100);
            }

            attackCooldown = DEFAULT_ATTACK_COOLDOWN;
            setAttacked(true);
            return;
        }

        this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(range),
                e -> e != owner && e.isAlive() && isHostileTarget(e, owner))
                .stream().findFirst().ifPresent(entity -> {
                    DamageSource damageSource;
                    if (owner instanceof Player player) {
                        damageSource = this.level().damageSources().playerAttack(player);
                    } else if (owner instanceof Mob mob) {
                        damageSource = this.level().damageSources().mobAttack(mob);
                    } else {
                        damageSource = this.level().damageSources().generic();
                    }
                    entity.hurt(damageSource, damage);

                    if (isFireDamage) {
                        entity.setRemainingFireTicks(100);
                    }

                    trackingTarget = entity;
                    attackCooldown = DEFAULT_ATTACK_COOLDOWN;
                    setAttacked(true);
                });
    }

    private double armor = 12.0D;

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() != null && source.getEntity() instanceof LivingEntity) {
            float damageAfterArmor = calculateDamageAfterArmor(amount);
            this.health -= damageAfterArmor;

            if (this.health <= 0) {
                this.discard();
                return true;
            }

            if (this.level().isClientSide) {
                for (int i = 0; i < 5; i++) {
                    this.level().addParticle(ParticleTypes.CRIT,
                            this.getX() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                            this.getY() + this.random.nextDouble() * this.getBbHeight(),
                            this.getZ() + (this.random.nextDouble() - 0.5) * this.getBbWidth(),
                            0, 0, 0);
                }
            }

            return true;
        }
        return super.hurt(source, amount);
    }

    private float calculateDamageAfterArmor(float damage) {
        float reduction = (float) Math.min(0.8F, armor * 0.04F);
        float effectiveDamage = damage * (1.0F - reduction);
        return effectiveDamage;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    private void makeHostilesAttackThisEntity() {
        double range = 15.0;
        final UUID thisUUID = this.getUUID();
        Entity owner = this.getOwner();

        this.level().getEntitiesOfClass(Mob.class,
                this.getBoundingBox().inflate(range),
                e -> e.isAlive())
                .stream()
                .forEach(mob -> {
                    if (thisUUID.equals(mob.getUUID())) {
                        return;
                    }

                    if (mob.getNavigation() != null) {
                        mob.getNavigation().moveTo(this.getX(), this.getY(), this.getZ(), 0.8);
                    }
                });

        this.level().getEntitiesOfClass(Monster.class,
                this.getBoundingBox().inflate(range),
                e -> e.isAlive())
                .stream()
                .forEach(monster -> {
                    if (thisUUID.equals(monster.getUUID())) {
                        return;
                    }

                    if (monster.getNavigation() != null) {
                        monster.getNavigation().moveTo(this.getX(), this.getY(), this.getZ(), 0.8);
                    }
                });
    }

    private boolean isHostileTarget(Entity target, Entity owner) {
        if (target instanceof Player player && owner instanceof Player) {
            return !player.getUUID().equals(owner.getUUID());
        }
        if (target instanceof Monster) {
            return true;
        }
        if (target instanceof net.minecraft.world.entity.npc.Villager) {
            return true;
        }
        if (target instanceof LivingEntity) {
            EntityType<?> type = target.getType();
            return type != EntityType.ENDER_DRAGON && type != EntityType.WITHER;
        }
        return false;
    }

    private PlayState movePredicate(AnimationState<GhostHeroicSoulEntity> state) {
        if (hasAttacked()) {
            state.getController().setAnimation(ATTACK_ANIMATION);
        } else {
            state.getController().setAnimation(MOVE_ANIMATION);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "moveController", 0, this::movePredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        this.discard();
    }

    @Override
    protected float getInertia() {
        return 0.95F;
    }

    public boolean isVisualFire() {
        return false;
    }

    public void setVisualFire(boolean visualFire) {
    }

    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("Lifetime", this.lifetime);
        nbt.putInt("Health", this.health);
        nbt.putInt("SoulColor", this.soulColor);
        nbt.putBoolean("IsFireDamage", this.isFireDamage);
        nbt.putFloat("Damage", this.damage);
        nbt.putString("SoulType", getSoulType());
        if (this.ownerUUID != null) {
            nbt.putUUID("OwnerUUID", this.ownerUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.lifetime = nbt.getInt("Lifetime");
        this.health = nbt.getInt("Health");
        this.soulColor = nbt.getInt("SoulColor");
        this.isFireDamage = nbt.getBoolean("IsFireDamage");
        this.damage = nbt.getFloat("Damage");
        if (nbt.contains("SoulType")) {
            this.setSoulType(nbt.getString("SoulType"));
        }
        if (nbt.hasUUID("OwnerUUID")) {
            this.ownerUUID = nbt.getUUID("OwnerUUID");
        }
    }

    public LivingEntity getOwner() {
        UUID uuid = this.getOwnerUUID();
        if (uuid == null || this.level() == null) return null;

        Entity owner = this.level().getPlayerByUUID(uuid);
        if (owner instanceof LivingEntity) {
            return (LivingEntity) owner;
        }

        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(20.0D))) {
            if (entity.getUUID().equals(uuid) && entity.isAlive()) {
                return entity;
            }
        }

        return null;
    }
}
