package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.drive;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Kamen Rider Drive 实体类
 * 车轮特效实体，用于表现Drive骑士的高速移动和攻击效果
 * 现在实现了飞镖式的来回回旋效果和三种不同类型的车轮变种
 */
public class DriveRiderEntity extends Projectile implements GeoEntity {
    // 车轮类型枚举
    public enum WheelType {
        ENGINEER(0, "engineer", "绿色工程车轮", 1.0f, 1.2f, ParticleTypes.ITEM_SLIME),
        FIRE(1, "fire", "黄色火焰车轮", 1.2f, 1.0f, ParticleTypes.FLAME),
        NINJA(2, "ninja", "紫色忍者手里刀车轮", 0.9f, 1.5f, ParticleTypes.ENCHANTED_HIT);
        
        private final int id;
        private final String name;
        private final String displayName;
        private final float damageMultiplier;
        private final float speedMultiplier;
        private final ParticleOptions particleType;
        
        WheelType(int id, String name, String displayName, float damageMultiplier, float speedMultiplier, ParticleOptions particleType) {
            this.id = id;
            this.name = name;
            this.displayName = displayName;
            this.damageMultiplier = damageMultiplier;
            this.speedMultiplier = speedMultiplier;
            this.particleType = particleType;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public float getDamageMultiplier() { return damageMultiplier; }
        public float getSpeedMultiplier() { return speedMultiplier; }
        public ParticleOptions getParticleType() { return particleType; }
        
        // 通过ID获取类型
        public static WheelType fromId(int id) {
            for (WheelType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return ENGINEER; // 默认返回工程车轮
        }
        
        // 获取下一个类型（循环）
        public WheelType getNextType() {
            int nextId = (this.id + 1) % values().length;
            return fromId(nextId);
        }
    }
    
    // 存储玩家当前使用的车轮类型的映射
    private static final Map<UUID, WheelType> PLAYER_WHEEL_TYPE_MAP = new HashMap<>();
    // 动画常量定义
    private static final RawAnimation MOVE_ANIMATION = RawAnimation.begin().thenPlay("move");
    private static final RawAnimation HIT_ANIMATION = RawAnimation.begin().thenPlay("attack.hit");

    // 动画实例缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 实体存活时间（刻）
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 200; // 增加到约10秒，以支持来回回旋效果

    // 攻击方向
    private Vec3 attackDirection;
    // 初始发射位置
    private Vec3 initialPosition;
    // 回旋阶段标志
    private int boomerangPhase = 0; // 0: 向外飞行, 1: 开始转向, 2: 向内飞行
    // 最大飞行距离
    private static final double MAX_FLIGHT_DISTANCE = 20.0; // 飞镖最大飞行距离
    // 转向点标志
    private boolean hasReachedTurningPoint = false;
    // 速度
    private float speed = 1.0f; // 飞镖飞行速度

    // 存储owner的UUID
    private UUID ownerUUID = null;

    // 特效触发几率
    private static final float TRIGGER_CHANCE = 1.0f; // 100%几率触发

    // 同步数据定义
    private static final EntityDataAccessor<Boolean> HAS_HIT_ENTITY = SynchedEntityData.defineId(DriveRiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(DriveRiderEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> WHEEL_TYPE = SynchedEntityData.defineId(DriveRiderEntity.class, EntityDataSerializers.INT);

    // 获取/设置同步属性的便捷方法
    private boolean hasHitEntity() {
        return this.getEntityData().get(HAS_HIT_ENTITY);
    }

    private void setHasHitEntity(boolean value) {
        this.getEntityData().set(HAS_HIT_ENTITY, value);
    }

    private float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    private void setDamage(float value) {
        this.getEntityData().set(DAMAGE, value);
    }
    
    // 获取当前车轮类型
    public WheelType getWheelType() {
        return WheelType.fromId(this.getEntityData().get(WHEEL_TYPE));
    }
    
    // 设置车轮类型
    private void setWheelType(WheelType type) {
        this.getEntityData().set(WHEEL_TYPE, type.getId());
    }

    // 获取owner UUID
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    // 设置owner UUID
    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    // 提供公共访问方法
    public boolean isHitEntity() {
        return this.hasHitEntity();
    }

    // 私有构造函数，用于生成新的特效实体
    private DriveRiderEntity(Level level, LivingEntity owner, Vec3 direction, float attackDamage, WheelType wheelType) {
        super(ModEntityTypes.DRIVE_RIDER_EFFECT.get(), level);
        this.setOwner(owner);
        this.noPhysics = true;
        this.initialPosition = owner.getEyePosition();
        this.setPos(this.initialPosition.add(direction.scale(1.0)));
        this.attackDirection = direction.normalize();
        this.setDamage(attackDamage * wheelType.getDamageMultiplier()); // 应用伤害倍率
        this.setOwnerUUID(owner.getUUID());
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
        this.speed = 1.0f * wheelType.getSpeedMultiplier(); // 应用速度倍率
        this.setWheelType(wheelType);
    }

    // 公共构造函数，用于注册
    public DriveRiderEntity(EntityType<? extends DriveRiderEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        // 初始化攻击方向，避免空指针异常
        this.attackDirection = Vec3.ZERO;
        this.initialPosition = Vec3.ZERO;
    }

    // 尝试生成普通特效实体的静态方法
    public static void trySpawnEffect(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        // 随机决定是否生成特效
        if (level.random.nextFloat() <= TRIGGER_CHANCE && !level.isClientSide) {
            // 获取并更新玩家当前的车轮类型
            UUID ownerUUID = owner.getUUID();
            WheelType currentType = PLAYER_WHEEL_TYPE_MAP.getOrDefault(ownerUUID, WheelType.ENGINEER);
            
            // 创建特效实体
            DriveRiderEntity effect = new DriveRiderEntity(level, owner, direction, attackDamage, currentType);
            level.addFreshEntity(effect);
            
            // 更新玩家的下一个车轮类型
            PLAYER_WHEEL_TYPE_MAP.put(ownerUUID, currentType.getNextType());
        }
    }

    @Override
    protected void defineSynchedData() {
        // 定义需要同步的数据
        this.getEntityData().define(HAS_HIT_ENTITY, false);
        this.getEntityData().define(DAMAGE, 0.0f);
        this.getEntityData().define(WHEEL_TYPE, WheelType.ENGINEER.getId());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
        this.setHasHitEntity(compound.getBoolean("HasHitEntity"));
        this.lifetime = compound.getInt("Lifetime");
        if (compound.contains("Damage")) {
            this.setDamage(compound.getFloat("Damage"));
        }
        // 读取车轮类型
        if (compound.contains("WheelType")) {
            this.setWheelType(WheelType.fromId(compound.getInt("WheelType")));
        }
        // 读取回旋相关数据
        if (compound.contains("BoomerangPhase")) {
            this.boomerangPhase = compound.getInt("BoomerangPhase");
        }
        if (compound.contains("HasReachedTurningPoint")) {
            this.hasReachedTurningPoint = compound.getBoolean("HasReachedTurningPoint");
        }
        if (compound.contains("Speed")) {
            this.speed = compound.getFloat("Speed");
        }
        // 读取初始位置
        if (compound.contains("InitialX")) {
            double x = compound.getDouble("InitialX");
            double y = compound.getDouble("InitialY");
            double z = compound.getDouble("InitialZ");
            this.initialPosition = new Vec3(x, y, z);
        }
        // 读取攻击方向
        if (compound.contains("DirectionX")) {
            double x = compound.getDouble("DirectionX");
            double y = compound.getDouble("DirectionY");
            double z = compound.getDouble("DirectionZ");
            this.attackDirection = new Vec3(x, y, z);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        compound.putBoolean("HasHitEntity", this.hasHitEntity());
        compound.putInt("Lifetime", this.lifetime);
        compound.putFloat("Damage", this.getDamage());
        // 保存车轮类型
        compound.putInt("WheelType", this.getWheelType().getId());
        // 保存回旋相关数据
        compound.putInt("BoomerangPhase", this.boomerangPhase);
        compound.putBoolean("HasReachedTurningPoint", this.hasReachedTurningPoint);
        compound.putFloat("Speed", this.speed);
        // 保存初始位置
        compound.putDouble("InitialX", this.initialPosition.x);
        compound.putDouble("InitialY", this.initialPosition.y);
        compound.putDouble("InitialZ", this.initialPosition.z);
        // 保存攻击方向
        compound.putDouble("DirectionX", this.attackDirection.x);
        compound.putDouble("DirectionY", this.attackDirection.y);
        compound.putDouble("DirectionZ", this.attackDirection.z);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::animationPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // 动画状态谓词
    private PlayState animationPredicate(AnimationState<DriveRiderEntity> state) {
        if (this.hasHitEntity()) {
            // 击中实体后播放击中动画
            state.getController().setAnimation(HIT_ANIMATION);
        } else {
            // 默认播放移动动画
            state.getController().setAnimation(MOVE_ANIMATION);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void tick() {
        super.tick();

        // 增加存活时间
        lifetime++;

        // 如果已经击中实体，检查动画是否播放完毕
        if (this.hasHitEntity()) {
            // 简单判断：如果存活时间超过动画所需时间，就移除实体
            if (lifetime > 40) { // 假设击中动画需要2秒
                // 爆炸效果
                this.explode();
                this.discard();
            }
            return;
        }

        // 如果超过最大存活时间，移除实体
        if (lifetime > MAX_LIFETIME) {
            // 爆炸效果
            this.explode();
            this.discard();
            return;
        }

        // 如果在服务器端，处理实体移动和碰撞检测
        if (!this.level().isClientSide()) {
            this.handleBoomerangMovement();
            // 检测碰撞
            this.checkEntityCollision();
        }
        
        // 吸引半径5格内的生物 - 移到移动之后调用，确保移动后仍然吸引周围生物
        this.attractEntities();
    }

    // 处理飞镖式的来回回旋移动
    private void handleBoomerangMovement() {
        Vec3 currentPos = this.position();
        
        // 安全检查：确保attackDirection不为null
        if (attackDirection == null) {
            this.attackDirection = Vec3.ZERO;
        }
        
        // 计算当前距离初始位置的距离
        double distanceFromStart = currentPos.distanceTo(this.initialPosition);
        
        // 飞镖式移动逻辑
        switch (boomerangPhase) {
            case 0: // 向外飞行阶段
                // 检查是否达到最大飞行距离或需要开始转向
                if (distanceFromStart >= MAX_FLIGHT_DISTANCE || hasReachedTurningPoint) {
                    boomerangPhase = 1; // 进入转向阶段
                    hasReachedTurningPoint = true;
                }
                break;
            
            case 1: // 转向阶段
                // 计算返回初始位置的方向
                LivingEntity owner = this.getOwner();
                Vec3 targetPos = (owner != null) ? owner.position().add(0, 1, 0) : this.initialPosition;
                Vec3 returnDirection = targetPos.subtract(currentPos).normalize();
                
                // 平滑过渡到返回方向
                this.attackDirection = this.attackDirection.scale(0.9).add(returnDirection.scale(0.1)).normalize();
                
                // 增加速度，使返回更快
                this.speed = Math.min(this.speed + 0.01f, 1.5f);
                
                // 检查是否已经接近返回方向，进入向内飞行阶段
                if (this.attackDirection.dot(returnDirection) > 0.95) {
                    boomerangPhase = 2; // 进入向内飞行阶段
                }
                break;
            
            case 2: // 向内飞行阶段
                // 检查是否接近初始位置或主人位置
                LivingEntity owner2 = this.getOwner();
                Vec3 returnTarget = (owner2 != null) ? owner2.position().add(0, 1, 0) : this.initialPosition;
                double distanceToTarget = currentPos.distanceTo(returnTarget);
                
                // 如果接近目标，准备消失
                if (distanceToTarget < 2.0) {
                    this.explode();
                    this.discard();
                    return;
                }
                
                // 持续调整方向朝向目标
                Vec3 finalReturnDirection = returnTarget.subtract(currentPos).normalize();
                this.attackDirection = this.attackDirection.scale(0.8).add(finalReturnDirection.scale(0.2)).normalize();
                
                // 保持较高的返回速度
                this.speed = Math.min(this.speed + 0.01f, 1.8f);
                break;
        }
        
        // 应用移动
        Vec3 newPos = currentPos.add(attackDirection.scale(this.speed));
        this.setPos(newPos.x, newPos.y, newPos.z);
        
        // 更新旋转角度以匹配移动方向
        this.updateRotation();
    }
    
    // 更新实体旋转角度以匹配移动方向
    public void updateRotation() {
        if (attackDirection == null || attackDirection.length() < 0.01) {
            return;
        }
        
        // 计算水平旋转角度（yaw）
        double horizontalDistance = Math.sqrt(attackDirection.x * attackDirection.x + attackDirection.z * attackDirection.z);
        float yaw = (float) Math.toDegrees(Math.atan2(-attackDirection.x, attackDirection.z));
        
        // 计算垂直旋转角度（pitch）
        float pitch = (float) Math.toDegrees(Math.atan2(-attackDirection.y, horizontalDistance));
        
        // 设置旋转角度
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    // 检查实体碰撞
    private void checkEntityCollision() {
        // 安全检查：确保attackDirection不为null
        if (attackDirection == null) {
            this.attackDirection = Vec3.ZERO;
        }
        
        // 获取攻击范围内的所有实体
        AABB boundingBox = this.getBoundingBox().expandTowards(attackDirection.scale(1.0)).inflate(1.0);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, boundingBox,
                entity -> entity != this.getOwner() && entity.isAlive() && this.canAttack(entity));

        for (LivingEntity entity : entities) {
            // 处理击中的实体
            this.onHitEntity(entity);
            break; // 只击中第一个实体
        }
    }

    // 击中实体时的处理
    private void onHitEntity(LivingEntity target) {
        if (this.hasHitEntity()) return;

        this.setHasHitEntity(true);

        // 对目标造成伤害
        float finalDamage = this.getDamage();

        // 修复伤害来源创建方式
        LivingEntity owner = this.getOwner();
        if (owner != null) {
            target.hurt(this.damageSources().mobProjectile(this, owner), finalDamage);
        } else {
            target.hurt(this.damageSources().magic(), finalDamage);
        }

        // 重置存活时间，开始播放击中动画
        lifetime = 0;
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

        // 首先尝试查找所有实体，不限于玩家类型
        AABB searchArea = this.getBoundingBox().inflate(32.0D); // 扩大搜索范围，例如32格
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
    
    // 吸引半径5格内的生物到特效中心
    private void attractEntities() {
        if (this.level().isClientSide()) return;
        
        Vec3 center = this.position();
        AABB searchArea = this.getBoundingBox().inflate(5.0D); // 半径5格
        WheelType currentType = this.getWheelType();
        
        // 获取范围内的所有可移动实体 - 包括其他玩家，只排除主人
        List<Entity> entities = this.level().getEntities(this, searchArea, entity -> 
                entity instanceof LivingEntity && 
                entity != this.getOwner() && 
                entity.isAlive() && 
                !(entity instanceof Projectile)
        );
        
        for (Entity entity : entities) {
            // 计算实体到特效中心的向量
            Vec3 entityPos = entity.position();
            Vec3 direction = center.subtract(entityPos).normalize();
            
            // 计算距离，距离越近，吸引力越大
            double distance = entityPos.distanceTo(center);
            if (distance > 0.3) { // 调整最小距离，让实体可以更靠近
                // 根据车轮类型调整吸引力
                double baseForce = 0.3;
                if (currentType == WheelType.ENGINEER) {
                    baseForce = 0.4; // 工程车轮有更强的吸引力
                } else if (currentType == WheelType.NINJA) {
                    baseForce = 0.2; // 忍者车轮吸引力较弱，但速度更快
                }
                
                double force = baseForce * (1.0 - distance / 5.0); // 吸引力随距离增加而减小
                Vec3 pushVector = direction.scale(force);
                
                // 应用推力
                entity.push(pushVector.x, pushVector.y, pushVector.z);
                
                // 增加粒子效果生成几率，从20%增加到40%
                if (this.level().random.nextFloat() < 0.4) {
                    // 使用对应类型的粒子效果
                    this.level().addParticle(currentType.getParticleType(), 
                            entityPos.x, entityPos.y + 0.5, entityPos.z, 
                            direction.x * 0.5, direction.y * 0.5, direction.z * 0.5);
                }
            }
            
            // 火焰车轮额外效果：对范围内敌人造成持续伤害
            if (currentType == WheelType.FIRE && entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                if (this.level().random.nextFloat() < 0.2) { // 20%几率造成燃烧
                    livingEntity.setSecondsOnFire(2);
                }
            }
        }
    }
    
    // 爆炸效果
    private void explode() {
        if (this.level().isClientSide()) return;
        
        Vec3 center = this.position();
        WheelType currentType = this.getWheelType();
        
        // 根据车轮类型调整爆炸范围和效果
        float explosionRadius = 2.0F;
        if (currentType == WheelType.ENGINEER) {
            explosionRadius = 2.5F; // 工程车轮爆炸范围更大
        } else if (currentType == WheelType.FIRE) {
            explosionRadius = 1.8F; // 火焰车轮爆炸范围稍小，但有燃烧效果
        }
        
        // 创建爆炸效果，不破坏方块
        this.level().explode(this, center.x, center.y, center.z, 
                explosionRadius, Level.ExplosionInteraction.NONE);
        
        // 对范围内的实体造成额外伤害
        AABB explosionArea = this.getBoundingBox().inflate(3.0D); // 爆炸范围3格
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, explosionArea, 
                entity -> entity != this.getOwner() && entity.isAlive());
        
        for (LivingEntity entity : entities) {
            // 对范围内的敌人造成伤害
            float damageMultiplier = 0.5F;
            if (currentType == WheelType.NINJA) {
                damageMultiplier = 0.7F; // 忍者车轮爆炸伤害更高
            }
            
            float explosionDamage = this.getDamage() * damageMultiplier;
            
            LivingEntity owner = this.getOwner();
            if (owner != null) {
                entity.hurt(this.damageSources().mobAttack(owner), explosionDamage);
            } else {
                entity.hurt(this.damageSources().magic(), explosionDamage);
            }
            
            // 各类型车轮的特殊效果
            switch (currentType) {
                case FIRE:
                    // 火焰车轮：造成燃烧效果
                    entity.setSecondsOnFire(3);
                    break;
                case ENGINEER:
                    // 工程车轮：击退效果更强
                    Vec3 knockbackDir = entity.position().subtract(center).normalize().scale(0.5);
                    entity.push(knockbackDir.x, 0.3, knockbackDir.z);
                    break;
                case NINJA:
                    // 忍者车轮：有几率使敌人虚弱
                    if (this.level().random.nextFloat() < 0.3) {
                        // 这里可以添加虚弱效果，需要使用Minecraft的效果系统
                    }
                    break;
            }
        }
        
        // 生成对应类型的粒子效果
        for (int i = 0; i < 20; i++) {
            double offsetX = (this.level().random.nextDouble() - 0.5) * explosionRadius * 2;
            double offsetY = (this.level().random.nextDouble() - 0.5) * explosionRadius * 2;
            double offsetZ = (this.level().random.nextDouble() - 0.5) * explosionRadius * 2;
            
            this.level().addParticle(currentType.getParticleType(),
                    center.x + offsetX, center.y + offsetY, center.z + offsetZ,
                    0, 0, 0);
        }
    }
}