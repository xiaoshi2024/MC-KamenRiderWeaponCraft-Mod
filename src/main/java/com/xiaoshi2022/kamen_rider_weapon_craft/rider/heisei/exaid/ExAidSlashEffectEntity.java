package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

import static software.bernie.geckolib.core.animation.Animation.LoopType.PLAY_ONCE;

public class ExAidSlashEffectEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation SLASH_ANIMATION = RawAnimation.begin().then("animation.exaid_slash_effect.slash", PLAY_ONCE);
    private Entity owner;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 120;
    private Entity hitEntity = null;
    private int followTicks = 0;
    private Entity targetEntity = null;

    public void setOwner(Entity owner) {
        this.owner = owner;
    }

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
        super(ModEntityTypes.EXAID_SLASH_EFFECT.get(), level);
        this.setPos(position);
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yRot = (float)Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float xRot = (float)Math.toDegrees(Math.atan2(direction.y, horizontalDistance));
        this.setYRot(yRot);
        this.setXRot(xRot);
        this.owner = owner;
        this.noPhysics = true;
        this.setInvulnerable(true);
    }

    @Override
    public void tick() {
        super.tick();
        
        // 检查特效是否靠近释放者，如果是则立即移除
        checkNearbyPlayers();

        lifetime++;
        if (lifetime > MAX_LIFETIME) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        // 在客户端，根据同步的数据更新实体引用
        if (this.level().isClientSide) {
            updateHitEntityFromSyncedData();
            updateTargetEntityFromSyncedData();
        }

        // 如果有明确的目标实体，并且还没有击中任何实体，优先跟踪目标实体
        // 确保不会跟踪到释放者
        if (targetEntity != null && hitEntity == null && !targetEntity.isRemoved() && targetEntity != owner) {
            // 跟踪目标实体，即使没有直接碰撞
            double randomOffsetX = (this.level().random.nextDouble() - 0.5) * 1.0;
            double randomOffsetY = (this.level().random.nextDouble() - 0.5) * 0.8;
            double randomOffsetZ = (this.level().random.nextDouble() - 0.5) * 1.0;

            this.setPos(targetEntity.getX() + randomOffsetX,
                    targetEntity.getY() + targetEntity.getBbHeight() * 0.5 + randomOffsetY,
                    targetEntity.getZ() + randomOffsetZ);
            followTicks++;

            // 在跟踪期间持续生成粒子效果
            if (followTicks % 3 == 0) {
                spawnHitParticles(targetEntity);
            }

            // 跟随一段时间后停止跟随，让特效自由漂浮
            if (followTicks > 60) {
                followTicks = 0;
                double randomMotionX = (this.level().random.nextDouble() - 0.5) * 0.3;
                double randomMotionY = 0.1 + this.level().random.nextDouble() * 0.3;
                double randomMotionZ = (this.level().random.nextDouble() - 0.5) * 0.3;
                this.setDeltaMovement(randomMotionX, randomMotionY, randomMotionZ);
            }
        } else if (hitEntity != null && !hitEntity.isRemoved()) {
            // 设置特效实体的位置为被击中实体的位置
            // 确保不会跟踪到释放者
            if (hitEntity != owner) {
                this.setPos(hitEntity.getX(), hitEntity.getY() + hitEntity.getBbHeight() * 0.5, hitEntity.getZ());
            } else {
                // 如果hitEntity是释放者，立即移除特效
                this.remove(RemovalReason.DISCARDED);
                return;
            }
            followTicks++;

            // 在跟随期间持续生成粒子效果
            if (followTicks % 3 == 0) {
                spawnHitParticles(hitEntity);
            }

            // 跟随一段时间后停止跟随
            if (followTicks > 30) {
                hitEntity = null;
                followTicks = 0;
            }
        } else {
            // 每刻轻微移动，给特效一个流动感
            Vec3 motion = getDeltaMovement().add(0, 0.01, 0);
            this.setDeltaMovement(motion);

            // 检查是否击中实体
            checkEntityCollision();
        }
    }

    // 设置特效应该跟踪的目标实体
    public void setTargetEntity(Entity targetEntity) {
        // 确保不会将释放者设置为目标
        if (targetEntity != null && targetEntity != owner) {
            this.targetEntity = targetEntity;
            // 在服务器端同步目标实体信息
            if (!this.level().isClientSide) {
                syncTargetEntity(targetEntity);
            }
        } else {
            // 如果尝试设置释放者为目标，则不设置任何目标
            this.targetEntity = null;
            if (!this.level().isClientSide) {
                syncTargetEntity(null);
            }
        }
    }

    // 同步目标实体信息到客户端
    private void syncTargetEntity(Entity entity) {
        if (entity != null) {
            this.entityData.set(TARGET_ENTITY_ID, entity.getId());
            this.entityData.set(HAS_TARGET_ENTITY, true);
        } else {
            this.entityData.set(TARGET_ENTITY_ID, 0);
            this.entityData.set(HAS_TARGET_ENTITY, false);
        }
    }

    // 在客户端根据同步的数据更新targetEntity引用
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

    // 检查是否击中实体
    private void checkEntityCollision() {
        if (hitEntity == null) {
            // 扩大检测范围，确保能够检测到实体
            List<Entity> nearbyEntities = this.level().getEntities(this, this.getBoundingBox().inflate(2.0D),
                    entity -> entity instanceof net.minecraft.world.entity.LivingEntity &&
                            entity != owner &&
                            !entity.isSpectator() &&
                            entity.isAlive() &&
                            !(entity instanceof Player && entity == owner) &&
                            // 确保不会检测到释放者
                            !(entity == owner));

            for (Entity entity : nearbyEntities) {
                if (entity == owner) {
                    continue;
                }

                // 记录被击中的实体，以便跟随它移动
                hitEntity = entity;
                followTicks = 0;

                // 在服务器端，将hitEntity信息同步到客户端
                if (!this.level().isClientSide) {
                    syncHitEntity(hitEntity);
                }

                // 在客户端生成粒子效果
                if (this.level().isClientSide) {
                    spawnHitParticles(entity);
                }

                // 标记为击中，停止自主移动
                this.setDeltaMovement(0, 0, 0);
                
                // 在服务器端对击中的实体造成伤害
                if (!this.level().isClientSide) {
                    if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                        // 创建一个特殊的伤害源，代表Ex-Aid的技能伤害
                        net.minecraft.world.damagesource.DamageSource damageSource = level().damageSources().playerAttack((Player)owner);
                        
                        // 立即造成一次基础伤害
                        livingEntity.hurt(damageSource, 5.0F);
                        
                        // 添加凋零效果作为持续伤害的实现
                        livingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.WITHER,
                                40, 1, false, false
                        ));
                    }
                }

                break;
            }
        }
    }

    // 同步被击中的实体信息到客户端
    private void syncHitEntity(Entity entity) {
        if (entity != null) {
            this.entityData.set(HIT_ENTITY_ID, entity.getId());
            this.entityData.set(HAS_HIT_ENTITY, true);
        } else {
            this.entityData.set(HIT_ENTITY_ID, 0);
            this.entityData.set(HAS_HIT_ENTITY, false);
        }
    }

    // 在客户端根据同步的数据更新hitEntity引用
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

    // 在击中实体时生成粒子效果
    private void spawnHitParticles(Entity entity) {
        if (entity == null || entity.isRemoved()) return;
        
        // 移除爆炸音效，保留粒子效果生成位置
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.hitEntity = null;
        this.owner = null;
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
    protected void defineSynchedData() {
        this.entityData.define(HIT_ENTITY_ID, 0);
        this.entityData.define(HAS_HIT_ENTITY, false);
        this.entityData.define(TARGET_ENTITY_ID, 0);
        this.entityData.define(HAS_TARGET_ENTITY, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.lifetime = nbt.getInt("Lifetime");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Lifetime", this.lifetime);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // 静态方法用于生成特效 - 修复版本
    public static void spawnEffect(Level level, Entity owner, Vec3 direction) {
        // 只在服务器端生成实体，确保所有客户端都能通过数据包同步看到
        if (!level.isClientSide) {
            Vec3 startPos = owner.getEyePosition(1.0F).add(direction.scale(1.0));
            ExAidSlashEffectEntity effect = new ExAidSlashEffectEntity(level, owner, startPos, direction);
            level.addFreshEntity(effect);
        }
    }

    // 新增：直接生成在目标实体上的特效
    public static void spawnEffectOnTarget(Level level, Entity owner, Entity target) {
        if (!level.isClientSide && target != null) {
            // 在目标实体周围生成特效
            double x = target.getX();
            double y = target.getY() + target.getBbHeight() * 0.5;
            double z = target.getZ();

            // 创建随机方向
            Vec3 direction = new Vec3(
                    level.random.nextDouble() - 0.5,
                    level.random.nextDouble() - 0.5,
                    level.random.nextDouble() - 0.5
            ).normalize();

            ExAidSlashEffectEntity effect = new ExAidSlashEffectEntity(level, owner, new Vec3(x, y, z), direction);
            effect.setTargetEntity(target); // 设置目标实体进行跟踪
            level.addFreshEntity(effect);
        }
    }
    
    /**
     * 检查周围是否有释放者，如果特效靠近释放者则消除，避免特效跑到释放者身上
     */
    private void checkNearbyPlayers() {
        // 只检查释放者是否在范围内
        if (owner != null && owner.isAlive()) {
            // 计算特效与释放者的距离
            double distance = this.distanceTo(owner);
            
            // 增加距离检测范围到2格，更早移除靠近释放者的特效
            if (distance < 2.0D) {
                this.remove(RemovalReason.DISCARDED);
                return;
            }
        }
    }
}