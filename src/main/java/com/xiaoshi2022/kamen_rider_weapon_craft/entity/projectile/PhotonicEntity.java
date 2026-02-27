package com.xiaoshi2022.kamen_rider_weapon_craft.entity.projectile;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

/**
 * Photonic子弹实体类
 * 用于实现特殊的光子子弹效果
 */
public class PhotonicEntity extends Projectile implements GeoEntity {
    // 动画常量定义
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenPlay("idle");

    // 实体存活时间（刻）
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 100; // 5秒
    private static final int EXPLODE_ANIMATION_LENGTH = 20; // 爆炸动画长度

    // 攻击方向
    private Vec3 attackDirection;
    
    // 飞行速度
    private float flightSpeed = 1.0f;
    // 最大飞行速度
    private static final float MAX_FLIGHT_SPEED = 2.0f;
    // 加速率
    private static final float ACCELERATION_RATE = 0.05f;
    
    // 追踪目标相关字段
    private LivingEntity trackingTarget = null;
    private int trackingCooldown = 0;
    private boolean isTrackingEnabled = false;
    
    // 最大追踪距离
    private static final double MAX_TRACKING_DISTANCE = 20.0;
    // 追踪速度
    private float trackingSpeed = 1.0f;
    // 最大追踪速度
    private static final float MAX_TRACKING_SPEED = 2.0f;
    
    // 地面光弹相关字段
    public boolean isGroundBased = false; // 是否为留在地上的光弹
    private int groundLifeTime = 0; // 地面光弹的生命周期
    private static final int MAX_GROUND_LIFETIME = 40; // 地面光弹最大生命周期（2秒）
    private static final float GROUND_SCALE = 3.0f; // 地面光弹的缩放比例

    // 存储owner的UUID
    private UUID ownerUUID = null;

    // 同步数据定义
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(PhotonicEntity.class, EntityDataSerializers.FLOAT);

    // 动画实例缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 获取/设置同步属性的便捷方法
    private float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    private void setDamage(float value) {
        this.getEntityData().set(DAMAGE, value);
    }

    // 获取owner UUID
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    // 设置owner UUID
    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    // 私有构造函数，用于生成新的子弹实体
    private PhotonicEntity(Level level, LivingEntity owner, Vec3 position, Vec3 initialDirection, float damage) {
        super(ModEntityTypes.PHOTONIC.get(), level);
        this.setOwner(owner);
        this.setOwnerUUID(owner.getUUID());
        this.setPos(position);
        this.attackDirection = initialDirection.normalize();
        this.setDeltaMovement(initialDirection.normalize().scale(flightSpeed));
        this.setDamage(damage);
        this.noPhysics = false;
        
        // 随机启用追踪目标（50%的几率）
        this.isTrackingEnabled = this.random.nextBoolean();
    }

    // 公共构造函数，用于注册
    public PhotonicEntity(EntityType<? extends PhotonicEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    // 生成子弹的静态方法
    public static void spawnPhotonic(Level level, LivingEntity owner, Vec3 direction, float damage) {
        if (level.isClientSide) return;

        // 计算发射位置
        Vec3 spawnPos = owner.getEyePosition().add(direction.normalize().scale(1.0));
        
        // 创建子弹实体
        PhotonicEntity photonic = new PhotonicEntity(level, owner, spawnPos, direction, damage);
        level.addFreshEntity(photonic);
    }

    @Override
    protected void defineSynchedData() {
        // 定义需要同步的数据
        this.getEntityData().define(DAMAGE, 0.0f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
        this.lifetime = compound.getInt("Lifetime");
        this.flightSpeed = compound.getFloat("FlightSpeed");
        if (compound.contains("Damage")) {
            this.setDamage(compound.getFloat("Damage"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        compound.putInt("Lifetime", this.lifetime);
        compound.putFloat("FlightSpeed", this.flightSpeed);
        compound.putFloat("Damage", this.getDamage());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::animationPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // 动画状态谓词
    private PlayState animationPredicate(AnimationState<PhotonicEntity> state) {
        // 始终播放idle动画，因为动画文件中只定义了idle
        state.getController().setAnimation(IDLE_ANIMATION);
        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {
        super.tick();

        // 增加存活时间
        lifetime++;

        // 检查是否为地面光弹
        if (isGroundBased) {
            // 地面光弹逻辑
            groundLifeTime++;
            
            // 如果超过地面光弹最大生命周期，消失
            if (groundLifeTime > MAX_GROUND_LIFETIME) {
                this.discard();
                return;
            }
            
            // 地面光弹不移动，只播放粒子效果
            if (this.level().isClientSide()) {
                spawnExhaustParticles();
            }
            return;
        }

        // 如果超过最大存活时间，自然消失
        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        // 粒子效果
        if (this.level().isClientSide()) {
            spawnExhaustParticles();
        }

        // 服务器端处理
        if (!this.level().isClientSide()) {
            // 如果启用了追踪，更新追踪目标
            if (isTrackingEnabled) {
                updateTrackingTarget();
                enhancedTrackingMovement();
            } else {
                // 否则，使用直线飞行移动
                straightFlightMovement();
            }
            
            // 检测碰撞并给予伤害
            checkCollisionAndDamage();
        }
    }
    
    // 直线飞行移动逻辑
    private void straightFlightMovement() {
        // 加速
        flightSpeed = Math.min(flightSpeed + ACCELERATION_RATE, MAX_FLIGHT_SPEED);
        
        Vec3 moveVec;
        
        if (attackDirection != null) {
            // 使用攻击方向继续直线前进
            moveVec = attackDirection.scale(flightSpeed);
        } else {
            // 如果攻击方向为null，使用当前运动方向
            moveVec = this.getDeltaMovement();
        }
        
        // 设置新的运动方向
        this.setDeltaMovement(moveVec);
        
        // 应用物理移动
        this.move(MoverType.SELF, this.getDeltaMovement());
        
        // 更新旋转角度以匹配移动方向
        updateRotation();
    }
    
    // 更新追踪目标
    private void updateTrackingTarget() {
        // 如果冷却中，不更新目标
        if (trackingCooldown > 0) {
            // 检查当前目标是否仍然有效
            if (trackingTarget != null && (!trackingTarget.isAlive() || 
                    trackingTarget.distanceToSqr(this) > MAX_TRACKING_DISTANCE * MAX_TRACKING_DISTANCE)) {
                trackingTarget = null;
            }
            trackingCooldown--;
            return;
        }
        
        LivingEntity owner = getOwner();
        
        // 查找所有附近的敌对生物或敌对玩家
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, 
                this.getBoundingBox().inflate(MAX_TRACKING_DISTANCE),
                e -> e != owner && e.isAlive() && canAttack(e));
        
        // 如果有多个目标，随机选择一个，实现分头追踪
        if (!nearbyEntities.isEmpty()) {
            // 随机选择一个目标，而不是总是选择最近的
            int randomIndex = this.random.nextInt(nearbyEntities.size());
            trackingTarget = nearbyEntities.get(randomIndex);
            trackingCooldown = 10; // 10tick冷却（0.5秒）
        }
    }
    
    // 增强的追踪移动逻辑
    private void enhancedTrackingMovement() {
        // 加速
        trackingSpeed = Math.min(trackingSpeed + ACCELERATION_RATE, MAX_TRACKING_SPEED);
        
        Vec3 moveVec;
        
        if (trackingTarget != null && trackingTarget.isAlive()) {
            // 追踪目标移动
            Vec3 targetPos = trackingTarget.position().add(0, trackingTarget.getBbHeight() / 2, 0);
            Vec3 entityPos = this.position();
            
            // 计算朝向目标的方向向量并归一化
            Vec3 direction = targetPos.subtract(entityPos).normalize();
            
            // 应用追踪移动
            moveVec = direction.scale(trackingSpeed);
            
            // 旋转实体朝向目标
            lookAt(targetPos);
        } else if (attackDirection != null) {
            // 如果没有追踪目标，使用攻击方向继续前进
            moveVec = attackDirection.scale(trackingSpeed);
        } else {
            // 如果攻击方向为null，使用当前运动方向
            moveVec = this.getDeltaMovement();
        }
        
        // 设置新的运动方向
        this.setDeltaMovement(moveVec);
        
        // 应用物理移动
        this.move(MoverType.SELF, this.getDeltaMovement());
        
        // 更新旋转角度以匹配移动方向
        updateRotation();
    }
    
    // 让实体朝向目标位置
    private void lookAt(Vec3 targetPos) {
        Vec3 entityPos = this.position();
        double dx = targetPos.x - entityPos.x;
        double dy = targetPos.y - entityPos.y;
        double dz = targetPos.z - entityPos.z;
        
        // 计算水平方向的角度
        double yaw = Math.atan2(dz, dx) * (180 / Math.PI) - 90.0;
        
        // 计算垂直方向的角度
        double distance = Math.sqrt(dx * dx + dz * dz);
        double pitch = Math.atan2(dy, distance) * (180 / Math.PI);
        
        // 设置实体的旋转角度
        this.setYRot((float) yaw);
        this.setXRot((float) pitch);
        this.setYBodyRot((float) yaw);
        this.setYHeadRot((float) yaw);
    }

    // 生成光子粒子效果
    private void spawnExhaustParticles() {
        // 获取子弹位置
        Vec3 position = this.position();
        
        // 生成粒子
        for (int i = 0; i < 3; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (this.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.3;
            
            this.level().addParticle(
                ParticleTypes.ELECTRIC_SPARK,
                position.x + offsetX,
                position.y + offsetY,
                position.z + offsetZ,
                0,
                0,
                0
            );
        }
    }

    // 更新实体旋转角度以匹配移动方向
    public void updateRotation() {
        Vec3 motion = this.getDeltaMovement();
        if (motion.length() < 0.01) return;
        
        // 计算水平旋转角度（yaw）
        double horizontalDistance = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        float yaw = (float) Math.toDegrees(Math.atan2(-motion.x, motion.z));
        
        // 计算垂直旋转角度（pitch）
        float pitch = (float) Math.toDegrees(Math.atan2(-motion.y, horizontalDistance));
        
        // 设置旋转角度
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    // 检查碰撞并给予伤害
    private void checkCollisionAndDamage() {
        // 检测与实体的碰撞并给予伤害
        for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(0.5))) {
            if (entity instanceof LivingEntity livingEntity && livingEntity != getOwner()) {
                if (this.getBoundingBox().intersects(livingEntity.getBoundingBox())) {
                    // 给予伤害
                    LivingEntity owner = getOwner();
                    if (owner != null) {
                        livingEntity.hurt(this.damageSources().mobAttack(owner), this.getDamage());
                    } else {
                        livingEntity.hurt(this.damageSources().magic(), this.getDamage());
                    }
                    
                    // 生成1-3个地面光弹
                    spawnGroundPhotonic(livingEntity);
                    
                    // 击中实体后立即消失
                    this.discard();
                    return;
                }
            }
        }
    }
    
    // 生成地面光弹
    private void spawnGroundPhotonic(LivingEntity hitEntity) {
        // 随机生成1-3个地面光弹
        int count = this.random.nextInt(3) + 1;
        
        for (int i = 0; i < count; i++) {
            // 计算地面光弹的生成位置（在击中实体周围随机位置）
            double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
            double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;
            Vec3 spawnPos = hitEntity.position().add(offsetX, 0.1, offsetZ);
            
            // 创建地面光弹
            PhotonicEntity groundPhotonic = new PhotonicEntity(this.level(), this.getOwner(), spawnPos, new Vec3(0, 0, 0), 0.0f);
            groundPhotonic.isGroundBased = true;
            this.level().addFreshEntity(groundPhotonic);
        }
    }



    // 检查是否可以攻击目标
    private boolean canAttack(LivingEntity target) {
        LivingEntity owner = getOwner();
        
        // 不能攻击自己或主人
        if (owner != null && target == owner) return false;
        
        return true;
    }

    // 获取实体的主人
    public LivingEntity getOwner() {
        UUID uuid = this.getOwnerUUID();
        if (uuid == null || this.level() == null) return null;
        
        // 首先尝试查找所有实体，不限于玩家类型
        AABB searchArea = this.getBoundingBox().inflate(32.0D);
        List<Entity> entities = this.level().getEntities(this, searchArea, entity ->
            entity instanceof LivingEntity && entity.getUUID().equals(uuid)
        );
        
        if (!entities.isEmpty()) {
            return (LivingEntity) entities.get(0);
        }
        
        // 如果没有找到，再尝试查找玩家（作为备选）
        Entity owner = this.level().getPlayerByUUID(uuid);
        if (owner instanceof LivingEntity) {
            return (LivingEntity) owner;
        }
        
        return null;
    }
}
