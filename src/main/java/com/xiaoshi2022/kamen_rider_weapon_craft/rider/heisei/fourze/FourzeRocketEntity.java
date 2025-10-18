package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.fourze;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleOptions;
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
 * Kamen Rider Fourze 火箭炮实体类
 * 用于实现Fourze的火箭追踪特效，发射3枚火箭炮追踪敌人并爆炸
 */
public class FourzeRocketEntity extends Projectile implements GeoEntity {
    // 动画常量定义
    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenPlay("fly");
    private static final RawAnimation EXPLODE_ANIMATION = RawAnimation.begin().thenPlay("explode");

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

    // 存储owner的UUID
    private UUID ownerUUID = null;

    // 同步数据定义
    private static final EntityDataAccessor<Boolean> IS_EXPLODING = SynchedEntityData.defineId(FourzeRocketEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(FourzeRocketEntity.class, EntityDataSerializers.FLOAT);

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

    // 私有构造函数，用于生成新的火箭实体
    private FourzeRocketEntity(Level level, LivingEntity owner, Vec3 position, Vec3 initialDirection, float damage) {
        super(ModEntityTypes.FOURZE_ROCKET.get(), level);
        this.setOwner(owner);
        this.setOwnerUUID(owner.getUUID());
        this.setPos(position);
        this.attackDirection = initialDirection.normalize();
        this.setDeltaMovement(initialDirection.normalize().scale(trackingSpeed));
        this.setDamage(damage);
        this.noPhysics = false;
    }

    // 公共构造函数，用于注册
    public FourzeRocketEntity(EntityType<? extends FourzeRocketEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    // 生成3枚火箭的静态方法
    public static void spawnRockets(Level level, LivingEntity owner, Vec3 direction, float damage) {
        if (level.isClientSide) return;

        // 发射3枚火箭，稍微分散发射角度
        for (int i = 0; i < 3; i++) {
            // 计算略微分散的发射方向
            float spreadAngle = (i - 1) * 5.0f; // 3枚火箭分别向左、中、右偏5度
            Vec3 spreadDirection = rotateVectorAroundY(direction, spreadAngle);
            
            // 计算发射位置（略微分散）
            Vec3 offset = new Vec3(
                (i - 1) * 0.5, // 左右偏移
                0.3,           // 向上偏移一点
                (i - 1) * 0.5  // 前后偏移
            );
            Vec3 spawnPos = owner.getEyePosition().add(direction.normalize().scale(1.0)).add(offset);
            
            // 创建火箭实体，使用注册的实体类型
            FourzeRocketEntity rocket = new FourzeRocketEntity(level, owner, spawnPos, spreadDirection, damage);
            level.addFreshEntity(rocket);
        }
    }

    // 绕Y轴旋转向量
    private static Vec3 rotateVectorAroundY(Vec3 vector, float degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        
        double x = vector.x * cos + vector.z * sin;
        double z = vector.z * cos - vector.x * sin;
        
        return new Vec3(x, vector.y, z).normalize();
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
    private PlayState animationPredicate(AnimationState<FourzeRocketEntity> state) {
        if (this.isExploding()) {
            // 爆炸时播放爆炸动画
            state.getController().setAnimation(EXPLODE_ANIMATION);
        } else {
            // 默认播放飞行动画
            state.getController().setAnimation(FLY_ANIMATION);
        }
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
            // 更新追踪目标 - 参考GhostHeroicSoulEntity的机制
            updateTrackingTarget();
            
            // 追踪移动 - 增强追踪能力
            enhancedTrackingMovement();
            
            // 检测碰撞
            checkCollision();
        }
    }
    
    // 更新追踪目标 - 参考GhostHeroicSoulEntity的实现
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
            // 追踪目标移动 - 参考GhostHeroicSoulEntity的方法
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

    // 生成火箭尾焰粒子
    private void spawnExhaustParticles() {
        // 获取火箭尾部位置（根据当前方向计算）
        Vec3 motion = this.getDeltaMovement().normalize();
        Vec3 exhaustPos = this.position().subtract(motion.scale(0.5));
        
        // 生成火焰粒子
        for (int i = 0; i < 3; i++) {
            double offsetX = (this.random.nextDouble() - 0.5) * 0.3;
            double offsetY = (this.random.nextDouble() - 0.5) * 0.3;
            double offsetZ = (this.random.nextDouble() - 0.5) * 0.3;
            
            this.level().addParticle(
                ParticleTypes.FLAME,
                exhaustPos.x + offsetX,
                exhaustPos.y + offsetY,
                exhaustPos.z + offsetZ,
                -motion.x * 0.5,
                -motion.y * 0.5,
                -motion.z * 0.5
            );
            
            // 烟雾粒子
            if (this.random.nextBoolean()) {
                this.level().addParticle(
                    ParticleTypes.SMOKE,
                    exhaustPos.x + offsetX,
                    exhaustPos.y + offsetY,
                    exhaustPos.z + offsetZ,
                    -motion.x * 0.3,
                    -motion.y * 0.3,
                    -motion.z * 0.3
                );
            }
        }
    }





    // 预测目标位置（增强的线性预测）


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
                explode();
                return;
            }
        }
        
        // 检测与方块的碰撞
        if (this.level().getBlockState(this.blockPosition()).isSolid()) {
            explode();
            return;
        }
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
        float explosionDamage = this.getDamage() * 1.2f; // 提升爆炸伤害，使火箭更具威力
        
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
            
            // 火焰粒子
            if (this.random.nextBoolean()) {
                this.level().addParticle(
                    ParticleTypes.FLAME,
                    center.x + offsetX,
                    center.y + offsetY,
                    center.z + offsetZ,
                    0,
                    0,
                    0
                );
            }
        }
    }

    // 检查是否可以攻击目标
    private boolean canAttack(LivingEntity target) {
        LivingEntity owner = getOwner();
        if (owner == null) return true;
        
        // 不能攻击自己或主人
        if (target == owner) return false;
        
        // 检查敌对关系
        return owner.canAttack(target);
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
        
        // 如果不是玩家，通过获取范围内实体的方式来查找
        AABB searchArea = this.getBoundingBox().inflate(32.0D);
        List<Entity> entities = this.level().getEntities(this, searchArea, entity ->
            entity instanceof LivingEntity && entity.getUUID().equals(uuid)
        );
        
        if (!entities.isEmpty()) {
            return (LivingEntity) entities.get(0);
        }
        
        return null;
    }
}