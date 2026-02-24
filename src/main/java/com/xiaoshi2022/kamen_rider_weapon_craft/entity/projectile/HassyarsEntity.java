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
 * Hassyars子弹实体类
 * 用于实现特殊的子弹效果
 */
public class HassyarsEntity extends Projectile implements GeoEntity {
    // 动画常量定义
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenPlay("idle");

    // 实体存活时间（刻）
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 100; // 5秒
    private static final int EXPLODE_ANIMATION_LENGTH = 20; // 爆炸动画长度

    // 攻击方向
    private Vec3 attackDirection;
    
    // 追踪目标相关字段
    private LivingEntity trackingTarget = null;
    private int trackingCooldown = 0;

    // 最大追踪距离
    private static final double MAX_TRACKING_DISTANCE = 20.0;
    // 追踪速度
    private float trackingSpeed = 1.0f;
    // 最大追踪速度
    private static final float MAX_TRACKING_SPEED = 2.0f;
    // 加速率
    private static final float ACCELERATION_RATE = 0.05f;
    
    // 反射相关字段
    private int reflectCount = 0;
    private static final int MAX_REFLECT_COUNT = 5; // 最大反射次数
    private LivingEntity lastHitEntity = null; // 上一次击中的实体，避免重复击中

    // 存储owner的UUID
    private UUID ownerUUID = null;

    // 同步数据定义
    private static final EntityDataAccessor<Boolean> IS_EXPLODING = SynchedEntityData.defineId(HassyarsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(HassyarsEntity.class, EntityDataSerializers.FLOAT);

    // 动画实例缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 获取/设置同步属性的便捷方法
    private boolean isExploding() {
        return this.getEntityData().get(IS_EXPLODING);
    }

    private void setExploding(boolean value) {
        this.getEntityData().set(IS_EXPLODING, value);
    }

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
    private HassyarsEntity(Level level, LivingEntity owner, Vec3 position, Vec3 initialDirection, float damage) {
        super(ModEntityTypes.HASSYARS.get(), level);
        this.setOwner(owner);
        this.setOwnerUUID(owner.getUUID());
        this.setPos(position);
        this.attackDirection = initialDirection.normalize();
        this.setDeltaMovement(initialDirection.normalize().scale(trackingSpeed));
        this.setDamage(damage);
        this.noPhysics = false;
    }

    // 公共构造函数，用于注册
    public HassyarsEntity(EntityType<? extends HassyarsEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    // 生成子弹的静态方法
    public static void spawnHassyars(Level level, LivingEntity owner, Vec3 direction, float damage) {
        if (level.isClientSide) return;

        // 计算发射位置
        Vec3 spawnPos = owner.getEyePosition().add(direction.normalize().scale(1.0));
        
        // 创建子弹实体
        HassyarsEntity hassyars = new HassyarsEntity(level, owner, spawnPos, direction, damage);
        level.addFreshEntity(hassyars);
    }

    @Override
    protected void defineSynchedData() {
        // 定义需要同步的数据
        this.getEntityData().define(IS_EXPLODING, false);
        this.getEntityData().define(DAMAGE, 0.0f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
        this.setExploding(compound.getBoolean("IsExploding"));
        this.lifetime = compound.getInt("Lifetime");
        this.trackingSpeed = compound.getFloat("TrackingSpeed");
        this.reflectCount = compound.getInt("ReflectCount");
        if (compound.contains("Damage")) {
            this.setDamage(compound.getFloat("Damage"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        compound.putBoolean("IsExploding", this.isExploding());
        compound.putInt("Lifetime", this.lifetime);
        compound.putFloat("TrackingSpeed", this.trackingSpeed);
        compound.putInt("ReflectCount", this.reflectCount);
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
    private PlayState animationPredicate(AnimationState<HassyarsEntity> state) {
        // 始终播放idle动画，因为动画文件中只定义了idle
        state.getController().setAnimation(IDLE_ANIMATION);
        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {
        super.tick();

        // 增加存活时间
        lifetime++;

        // 如果正在爆炸，检查动画是否播放完毕
        if (this.isExploding()) {
            if (lifetime > EXPLODE_ANIMATION_LENGTH) {
                this.discard();
            }
            return;
        }

        // 如果超过最大存活时间，自动爆炸
        if (lifetime > MAX_LIFETIME) {
            this.explode();
            return;
        }

        // 粒子效果
        if (this.level().isClientSide()) {
            spawnExhaustParticles();
        }

        // 服务器端处理
        if (!this.level().isClientSide()) {
            // 更新追踪目标
            updateTrackingTarget();
            
            // 追踪移动
            enhancedTrackingMovement();
            
            // 检测碰撞
            checkCollision();
        }
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
        
        // 查找最近的敌对生物或敌对玩家
        LivingEntity nearestTarget = this.level().getEntitiesOfClass(LivingEntity.class, 
                this.getBoundingBox().inflate(MAX_TRACKING_DISTANCE),
                e -> e != owner && e.isAlive() && canAttack(e))
                .stream()
                .min(java.util.Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
        
        // 如果找到新目标，设置并重置冷却
        if (nearestTarget != null) {
            trackingTarget = nearestTarget;
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

    // 生成粒子效果
    private void spawnExhaustParticles() {
        // 获取子弹位置
        Vec3 position = this.position();
        
        // 生成粒子
        for (int i = 0; i < 3; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (this.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.3;
            
            this.level().addParticle(
                ParticleTypes.FLAME,
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

    // 检查碰撞
    private void checkCollision() {
        // 检测与追踪目标实体的碰撞
        if (trackingTarget != null && trackingTarget.isAlive()) {
            if (this.getBoundingBox().intersects(trackingTarget.getBoundingBox())) {
                handleCollision(trackingTarget);
                return;
            }
        }
        
        // 检测与其他实体的碰撞
        for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(0.5))) {
            if (entity instanceof LivingEntity livingEntity && livingEntity != getOwner() && livingEntity != lastHitEntity) {
                if (this.getBoundingBox().intersects(livingEntity.getBoundingBox())) {
                    handleCollision(livingEntity);
                    return;
                }
            }
        }
        
        // 检测与方块的碰撞
        if (this.level().getBlockState(this.blockPosition()).isSolid()) {
            explode();
            return;
        }
    }
    
    // 处理碰撞，实现反射逻辑
    private void handleCollision(LivingEntity hitEntity) {
        if (this.isExploding() || this.level().isClientSide()) return;
        
        // 增加反射次数
        reflectCount++;
        
        // 记录上一次击中的实体，避免重复击中
        lastHitEntity = hitEntity;
        
        // 对击中的实体造成伤害
        LivingEntity owner = getOwner();
        if (owner != null) {
            hitEntity.hurt(this.damageSources().mobAttack(owner), this.getDamage());
        } else {
            hitEntity.hurt(this.damageSources().magic(), this.getDamage());
        }
        
        // 检查是否达到最大反射次数
        if (reflectCount >= MAX_REFLECT_COUNT) {
            // 达到最大反射次数，爆炸
            explode();
            return;
        }
        
        // 查找下一个反射目标
        LivingEntity nextTarget = findNextReflectTarget(hitEntity);
        
        if (nextTarget != null) {
            // 计算反射方向
            Vec3 hitPos = hitEntity.position().add(0, hitEntity.getBbHeight() / 2, 0);
            Vec3 nextPos = nextTarget.position().add(0, nextTarget.getBbHeight() / 2, 0);
            Vec3 reflectDirection = nextPos.subtract(hitPos).normalize();
            
            // 更新攻击方向和追踪目标
            attackDirection = reflectDirection;
            trackingTarget = nextTarget;
            
            // 重新设置运动方向
            this.setDeltaMovement(reflectDirection.scale(trackingSpeed));
            
            // 旋转实体朝向新目标
            lookAt(nextPos);
        } else {
            // 没有找到下一个目标，爆炸
            explode();
        }
    }
    
    // 查找下一个反射目标
    private LivingEntity findNextReflectTarget(LivingEntity currentEntity) {
        LivingEntity owner = getOwner();
        
        // 查找附近的实体，排除当前实体和主人
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, 
                this.getBoundingBox().inflate(MAX_TRACKING_DISTANCE),
                e -> e != owner && e != currentEntity && e != lastHitEntity && e.isAlive() && canAttack(e));
        
        if (nearbyEntities.isEmpty()) {
            return null;
        }
        
        // 选择最近的实体作为下一个目标
        return nearbyEntities.stream()
                .min(java.util.Comparator.comparingDouble(currentEntity::distanceToSqr))
                .orElse(null);
    }

    // 爆炸效果
    private void explode() {
        if (this.isExploding() || this.level().isClientSide()) return;
        
        this.setExploding(true);
        this.lifetime = 0;
        
        // 创建爆炸效果，不破坏地形
        Vec3 center = this.position();
        float explosionRadius = 2.0F;
        
        this.level().explode(
            this,
            center.x,
            center.y,
            center.z,
            explosionRadius,
            Level.ExplosionInteraction.NONE // 不破坏地形
        );
        
        // 对范围内的实体造成伤害
        AABB explosionArea = this.getBoundingBox().inflate(explosionRadius);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, explosionArea, 
            entity -> entity != this.getOwner() && entity.isAlive());
        
        LivingEntity owner = this.getOwner();
        float explosionDamage = this.getDamage() * 1.2f; // 提升爆炸伤害
        
        for (LivingEntity entity : entities) {
            // 计算距离衰减
            double distance = entity.distanceTo(this);
            float finalDamage = (float) (explosionDamage * (1.0 - distance / (explosionRadius * 2.0)));
            
            if (finalDamage > 0) {
                if (owner != null) {
                    entity.hurt(this.damageSources().mobAttack(owner), finalDamage);
                } else {
                    entity.hurt(this.damageSources().magic(), finalDamage);
                }
                
                // 击退效果
                Vec3 knockbackDir = entity.position().subtract(center).normalize();
                entity.push(knockbackDir.x * 0.5, 0.3, knockbackDir.z * 0.5);
            }
        }
        
        // 生成爆炸粒子
        for (int i = 0; i < 30; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * explosionRadius * 2;
            double offsetY = (this.random.nextDouble() - 0.5) * explosionRadius * 2;
            double offsetZ = (this.random.nextDouble() - 0.5) * explosionRadius * 2;
            
            this.level().addParticle(
                ParticleTypes.EXPLOSION,
                center.x + offsetX,
                center.y + offsetY,
                center.z + offsetZ,
                0,
                0,
                0
            );
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
