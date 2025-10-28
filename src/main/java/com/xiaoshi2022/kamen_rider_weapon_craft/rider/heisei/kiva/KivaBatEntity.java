package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kiva;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
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
 * Kamen Rider Kiva 蝙蝠实体类
 * 用于处理蝙蝠群攻击的实体效果
 */
public class KivaBatEntity extends AbstractHurtingProjectile implements GeoEntity {
    
    // 动画实例缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    // 动画常量定义 - 使用"bats"作为动画名称，与要求一致
    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenPlay("bats");
    
    // 同步数据定义
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(KivaBatEntity.class, EntityDataSerializers.FLOAT);
    
    // 实体存活时间
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 100; // 5秒，增加追踪时间
    
    // 存储owner的UUID
    private UUID ownerUUID = null;
    
    // 追踪目标相关
    private UUID targetUUID = null;
    private LivingEntity targetEntity = null;
    private boolean isTracking = false;
    private double trackingSpeed = 0.8; // 追踪速度
    private double wanderSpeed = 0.3; // 漫游速度
    private double currentSpeed = 0.8; // 当前速度
    
    public KivaBatEntity(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = false; // 蝙蝠需要物理效果以更自然地飞行
    }
    
    // 私有构造函数，用于生成新的特效实体
    private KivaBatEntity(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        super(ModEntityTypes.KIVA_BAT_EFFECT.get(), level);
        this.setOwner(owner);
        this.noPhysics = false;
        this.setPos(owner.getEyePosition().add(direction.scale(1.0)));
        this.shoot(direction.x, direction.y, direction.z, 1.2f, 5.0f); // 增加随机扩散，使蝙蝠群更自然
        this.entityData.set(DAMAGE, attackDamage);
        this.setOwnerUUID(owner.getUUID());
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
        
        // 确保禁用任何可能的火焰效果
        this.setVisualFire(false);
        this.setSecondsOnFire(0);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DAMAGE, 0.0f);
    }
    
    // 获取/设置同步属性的便捷方法
    private float getDamageValue() {
        return this.entityData.get(DAMAGE);
    }
    
    // 获取owner UUID
    public UUID getOwnerUUID() {
        return ownerUUID;
    }
    
    // 设置owner UUID
    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::animationPredicate));
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    
    private PlayState animationPredicate(AnimationState<KivaBatEntity> event) {
        event.getController().setAnimation(FLY_ANIMATION);
        return PlayState.CONTINUE;
    }
    
    @Override
    public void tick() {
        // 确保实体不会有任何火焰相关的视觉效果
        this.clearFire();
        this.setVisualFire(false);
        
        // 增加存活时间
        lifetime++;
        
        // 尝试追踪目标
        handleTrackingBehavior();
        
        // 超过最大存活时间后移除实体
        if (lifetime >= MAX_LIFETIME) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }
        
        // 根据生命周期调整速度，使蝙蝠群的运动更加自然
        if (lifetime < 20) {
            // 初始阶段，加速
            currentSpeed = Math.min(trackingSpeed, currentSpeed + 0.05);
        } else if (lifetime > MAX_LIFETIME - 30) {
            // 结束阶段，减速
            currentSpeed = Math.max(wanderSpeed, currentSpeed - 0.02);
        }
        
        // 执行默认行为，但确保不产生火焰
        super.tick();
        this.clearFire();
        this.setVisualFire(false);
    }
    
    /**
     * 处理蝙蝠的追踪和攻击行为
     */
    private void handleTrackingBehavior() {
        LivingEntity owner = this.getOwner();
        
        // 如果还没有目标，尝试寻找附近的敌对实体
        if (!isTracking && targetEntity == null && owner != null) {
            findAndTrackTarget(owner);
        }
        
        // 如果有目标且目标仍然存活，执行追踪和攻击
        if (isTracking && targetEntity != null && targetEntity.isAlive()) {
            trackTarget();
        } else {
            // 如果目标丢失，尝试重新寻找
            if (owner != null) {
                findAndTrackTarget(owner);
            }
            // 如果没有找到新目标，退出追踪模式
            if (targetEntity == null) {
                isTracking = false;
            }
        }
    }
    
    /**
     * 寻找并开始追踪附近的敌对实体
     */
    private void findAndTrackTarget(LivingEntity owner) {
        // 搜索范围：初始阶段较大，接近目标后减小
        double searchRange = lifetime < 30 ? 15.0 : 8.0;
        
        // 搜索周围的敌对实体
        Vec3 pos = this.position();
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(pos.x - searchRange, pos.y - searchRange, pos.z - searchRange, 
                         pos.x + searchRange, pos.y + searchRange, pos.z + searchRange),
                entity -> entity != owner && entity.isAlive() && owner.canAttack(entity));
        
        // 选择最近的敌对实体作为目标
        if (!nearbyEntities.isEmpty()) {
            LivingEntity nearestTarget = null;
            double nearestDistance = Double.MAX_VALUE;
            
            for (LivingEntity entity : nearbyEntities) {
                double distance = entity.distanceToSqr(this);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestTarget = entity;
                }
            }
            
            if (nearestTarget != null) {
                // 设置追踪目标
                this.targetEntity = nearestTarget;
                this.targetUUID = nearestTarget.getUUID();
                this.isTracking = true;
                
                // 发现目标后加速
                currentSpeed = trackingSpeed;
            }
        }
    }
    
    /**
     * 追踪目标实体
     */
    private void trackTarget() {
        if (targetEntity == null) return;
        
        // 计算到目标的方向
        Vec3 targetPos = targetEntity.position().add(0, targetEntity.getBbHeight() * 0.5, 0);
        Vec3 toTarget = targetPos.subtract(this.position()).normalize();
        
        // 添加一些随机性，使蝙蝠的飞行更加自然
        double randomX = (this.level().random.nextDouble() - 0.5) * 0.1;
        double randomY = (this.level().random.nextDouble() - 0.5) * 0.1;
        double randomZ = (this.level().random.nextDouble() - 0.5) * 0.1;
        
        Vec3 adjustedDirection = new Vec3(
            toTarget.x + randomX,
            toTarget.y + randomY,
            toTarget.z + randomZ
        ).normalize();
        
        // 更新蝙蝠的运动方向
        this.setDeltaMovement(adjustedDirection.scale(currentSpeed));
        
        // 更新旋转角度，使蝙蝠朝向目标
        updateRotationToTarget(toTarget);
    }
    
    /**
     * 更新蝙蝠的旋转角度，使其朝向目标
     */
    private void updateRotationToTarget(Vec3 direction) {
        // 计算水平和垂直旋转角度
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F;
        float pitch = (float) Math.toDegrees(Math.atan2(direction.y, horizontalDistance));
        
        // 设置旋转角度
        this.setYRot(yaw);
        this.setXRot(pitch);
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        if (this.targetUUID != null) {
            compound.putUUID("TargetUUID", this.targetUUID);
        }
        compound.putInt("Lifetime", this.lifetime);
        compound.putFloat("Damage", this.getDamageValue());
        compound.putBoolean("IsTracking", this.isTracking);
        compound.putDouble("CurrentSpeed", this.currentSpeed);
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
        if (compound.hasUUID("TargetUUID")) {
            this.targetUUID = compound.getUUID("TargetUUID");
        }
        this.lifetime = compound.getInt("Lifetime");
        this.entityData.set(DAMAGE, compound.getFloat("Damage"));
        this.isTracking = compound.getBoolean("IsTracking");
        this.currentSpeed = compound.getDouble("CurrentSpeed");
    }
    
    protected DamageSource getDamageSource() {
        LivingEntity owner = this.getOwner();
        if (owner != null) {
            return this.damageSources().mobProjectile(this, owner);
        } else {
            return this.damageSources().magic();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        
        if (result.getEntity() instanceof LivingEntity target) {
            // 对目标造成伤害
            float damage = this.getDamageValue() * 0.3f; // 单只蝙蝠伤害较低
            target.hurt(this.getDamageSource(), damage);
            
            // 蝙蝠击中后移除
            this.remove(RemovalReason.DISCARDED);
        }
    }
    
    @Override
    protected float getInertia() {
        return 0.95F; // 较低的惯性，使蝙蝠更容易改变方向
    }
    
    public boolean isVisualFire() {
        // 始终返回false，禁用视觉火焰效果
        return false;
    }
    
    public void setVisualFire(boolean visualFire) {
        // 覆盖此方法，忽略任何尝试设置视觉火焰的调用
    }
    
    @Override
    public void setSecondsOnFire(int seconds) {
        // 覆盖此方法，忽略任何尝试设置着火时间的调用
    }
    
    @Override
    public boolean isNoGravity() {
        return true; // 蝙蝠不受重力影响，更自由地飞行
    }
    
    // 获取实体的主人
    public LivingEntity getOwner() {
        UUID uuid = this.getOwnerUUID();
        if (uuid == null || this.level() == null) return null;
        
        // 首先尝试查找玩家
        Entity owner = this.level().getPlayerByUUID(uuid);
        if (owner instanceof LivingEntity) {
            return (LivingEntity) owner;
        }
        
        // 然后查找其他非玩家LivingEntity
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, 
                this.getBoundingBox().inflate(20.0D))) {
            if (entity.getUUID().equals(uuid) && entity.isAlive()) {
                return entity;
            }
        }
        
        return null;
    }
    
    /**
     * 尝试生成蝙蝠群特效
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param direction 方向向量
     * @param attackDamage 攻击力
     */
    public static void trySpawnEffect(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        if (!level.isClientSide && ModEntityTypes.KIVA_BAT_EFFECT.get() != null) {
            // 创建特效实体
            KivaBatEntity effect = new KivaBatEntity(level, owner, direction, attackDamage);
            level.addFreshEntity(effect);
        }
    }
}