package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ooo;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
// 移除粒子相关导入
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
 * Kamen Rider OOO Geo实体类
 * 用于处理细胞硬币斩的实体效果
 */
public class OOOGeoEntity extends AbstractHurtingProjectile implements GeoEntity {
    
    // 联组类型
    private String coinType = "tatoba";
    
    // 动画实例缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    // 动画常量定义
    private static final RawAnimation SPIN_ANIMATION = RawAnimation.begin().thenPlay("spin");
    
    // 同步数据定义
    private static final EntityDataAccessor<String> COIN_TYPE = SynchedEntityData.defineId(OOOGeoEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(OOOGeoEntity.class, EntityDataSerializers.FLOAT);
    
    // 实体存活时间
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 80; // 4秒，增加追踪时间
    
    // 存储owner的UUID
    private UUID ownerUUID = null;
    
    // 追踪目标相关
    private UUID targetUUID = null;
    private LivingEntity targetEntity = null;
    private boolean isTracking = false;
    private double orbitRadius = 1.0; // 围绕目标旋转的半径
    private double orbitAngle = 0.0; // 当前旋转角度
    private double orbitSpeed = 0.1; // 旋转速度
    
    public OOOGeoEntity(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }
    
    // 私有构造函数，用于生成新的特效实体
    private OOOGeoEntity(Level level, LivingEntity owner, Vec3 direction, float attackDamage, String coinType) {
        super(ModEntityTypes.OOO_GEO_EFFECT.get(), level);
        this.setOwner(owner);
        this.noPhysics = true;
        this.setPos(owner.getEyePosition().add(direction.scale(1.0)));
        this.shoot(direction.x, direction.y, direction.z, 1.5f, 0.0f);
        this.setCoinType(coinType);
        this.entityData.set(DAMAGE, attackDamage);
        this.setOwnerUUID(owner.getUUID());
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
        
        // 初始化随机轨道角度，使硬币分布更加自然
        this.orbitAngle = level.random.nextDouble() * Math.PI * 2;
        
        // 确保禁用任何可能的火焰效果
        this.setVisualFire(false);
        this.setSecondsOnFire(0);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(COIN_TYPE, "tatoba");
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
    
    /**
     * 设置联组类型
     */
    public void setCoinType(String coinType) {
        this.coinType = coinType;
        this.entityData.set(COIN_TYPE, coinType);
    }
    
    /**
     * 获取联组类型
     */
    public String getCoinType() {
        return this.entityData.get(COIN_TYPE);
    }
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::animationPredicate));
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    
    private PlayState animationPredicate(AnimationState<OOOGeoEntity> event) {
        event.getController().setAnimation(SPIN_ANIMATION);
        return PlayState.CONTINUE;
    }
    
    @Override
    public void tick() {
        // 确保实体不会有任何火焰相关的视觉效果
        // 不仅清除火焰状态，还确保不会通过其他方式产生火焰
        this.clearFire(); // 无条件清除火焰状态
        this.setVisualFire(false); // 禁用视觉火焰效果
        
        // 增加存活时间
        lifetime++;
        
        // 如果是恐龙联组且还在追踪范围内，尝试追踪目标
        if ("putotyra".equals(this.getCoinType())) {
            handleTrackingBehavior();
        }
        
        // 超过最大存活时间后移除实体
        if (lifetime >= MAX_LIFETIME) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }
        
        // 如果不在追踪模式，执行默认行为，但确保不产生火焰
        if (!isTracking) {
            super.tick();
            // 再次清除火焰，确保基类tick后不会重新添加
            this.clearFire();
            this.setVisualFire(false);
        }
    }
    
    /**
     * 处理细胞硬币的追踪和包裹行为
     */
    private void handleTrackingBehavior() {
        LivingEntity owner = this.getOwner();
        
        // 如果还没有目标，尝试寻找附近的敌对实体
        if (!isTracking && targetEntity == null && owner != null) {
            findAndTrackTarget(owner);
        }
        
        // 如果有目标且目标仍然存活，执行追踪和包裹
        if (isTracking && targetEntity != null && targetEntity.isAlive()) {
            orbitAroundTarget();
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
        double searchRange = lifetime < 20 ? 15.0 : 8.0;
        
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
                
                // 调整轨道半径（基于目标大小）
                this.orbitRadius = 0.8 + nearestTarget.getBbWidth() * 0.5;
                
                // 根据生命周期调整轨道速度（越接近消失速度越快）
                this.orbitSpeed = 0.1 + (Math.min(lifetime, 60) / 60.0) * 0.1;
            }
        }
    }
    
    /**
     * 围绕目标旋转，形成包裹效果
     */
    private void orbitAroundTarget() {
        if (targetEntity == null) return;
        
        // 更新轨道角度
        this.orbitAngle += this.orbitSpeed;
        if (this.orbitAngle > Math.PI * 2) {
            this.orbitAngle -= Math.PI * 2;
        }
        
        // 计算轨道位置
        // 使用正弦和余弦函数计算圆形轨道
        double offsetX = Math.cos(this.orbitAngle) * this.orbitRadius;
        double offsetY = Math.sin(this.orbitAngle * 0.5) * 0.3; // 添加垂直方向的小波动
        double offsetZ = Math.sin(this.orbitAngle) * this.orbitRadius;
        
        // 目标中心位置
        Vec3 targetCenter = targetEntity.position().add(0, targetEntity.getBbHeight() * 0.5, 0);
        
        // 计算硬币新位置
        Vec3 newPos = targetCenter.add(offsetX, offsetY, offsetZ);
        
        // 设置硬币位置
        this.setPos(newPos.x, newPos.y, newPos.z);
        
        // 增加一些随机性，使多个硬币的运动更加自然
        this.orbitSpeed += (this.level().random.nextDouble() - 0.5) * 0.01;
        this.orbitSpeed = Math.max(0.05, Math.min(this.orbitSpeed, 0.2));
        
        // 生命周期过半后，逐渐缩小轨道半径，营造吞噬的感觉
        if (lifetime > MAX_LIFETIME / 2) {
            this.orbitRadius *= 0.99;
            this.orbitRadius = Math.max(this.orbitRadius, 0.3);
        }
        
        // 检查是否应该对目标造成伤害（周期性伤害）
        if (lifetime % 10 == 0) { // 每5个tick造成一次伤害
            if (targetEntity.isAlive()) {
                targetEntity.hurt(this.getDamageSource(), this.getDamageValue() * 0.2f); // 持续伤害较低
            }
        }
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
        compound.putString("CoinType", this.getCoinType());
        compound.putInt("Lifetime", this.lifetime);
        compound.putFloat("Damage", this.getDamageValue());
        compound.putBoolean("IsTracking", this.isTracking);
        compound.putDouble("OrbitRadius", this.orbitRadius);
        compound.putDouble("OrbitAngle", this.orbitAngle);
        compound.putDouble("OrbitSpeed", this.orbitSpeed);
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
        this.setCoinType(compound.getString("CoinType"));
        this.lifetime = compound.getInt("Lifetime");
        this.entityData.set(DAMAGE, compound.getFloat("Damage"));
        this.isTracking = compound.getBoolean("IsTracking");
        this.orbitRadius = compound.getDouble("OrbitRadius");
        this.orbitAngle = compound.getDouble("OrbitAngle");
        this.orbitSpeed = compound.getDouble("OrbitSpeed");
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
            float damage = this.getDamageValue();
            
            // 根据不同联组应用不同的伤害系数
            if ("latorartar".equals(this.getCoinType())) {
                damage *= 1.3f; // 力量型联组伤害更高
            } else if ("shauta".equals(this.getCoinType())) {
                damage *= 1.1f; // 水属性联组
            } else if ("sagohzo".equals(this.getCoinType())) {
                damage *= 1.0f; // 防御型联组
            } else if ("putotyra".equals(this.getCoinType())) {
                damage *= 1.5f; // 全能型联组伤害最高
            } else {
                damage *= 1.2f; // 基本联组
            }
            
            target.hurt(this.getDamageSource(), damage);
        }
        
        // 击中后移除实体
        this.remove(RemovalReason.DISCARDED);
    }
    
    @Override
    protected float getInertia() {
        // 根据不同联组设置不同的惯性
        switch (this.getCoinType()) {
            case "latorartar":
                return 0.95F;
            case "shauta":
                return 0.98F;
            case "sagohzo":
                return 0.92F;
            case "putotyra":
                return 0.90F;
            case "tatoba":
            default:
                return 0.96F;
        }
    }
    
    public boolean isVisualFire() {
        // 始终返回false，禁用视觉火焰效果
        return false;
    }
    
    public void setVisualFire(boolean visualFire) {
        // 覆盖此方法，忽略任何尝试设置视觉火焰的调用
        // 不执行任何操作，确保不会显示火焰
    }
    
    @Override
    public void setSecondsOnFire(int seconds) {
        // 覆盖此方法，忽略任何尝试设置着火时间的调用
        // 不执行任何操作，确保实体不会着火
    }
    
    @Override
    public boolean isNoGravity() {
        // 根据不同联组设置是否有重力
        return "shauta".equals(this.getCoinType()) || "tatoba".equals(this.getCoinType());
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
        
        return null;
    }
    
    // 获取追踪目标
    public LivingEntity getTarget() {
        // 如果目标实体已存在且存活，直接返回
        if (targetEntity != null && targetEntity.isAlive()) {
            return targetEntity;
        }
        
        // 否则尝试从UUID重新获取
        if (targetUUID != null && this.level() != null) {
            // 使用公共方法 getEntitiesOfClass 替代 protected 的 getEntities()
            for (Entity entity : this.level().getEntitiesOfClass(Entity.class, this.getBoundingBox().inflate(20.0D))) {
                if (entity.getUUID().equals(targetUUID) && entity instanceof LivingEntity && entity.isAlive()) {
                    this.targetEntity = (LivingEntity) entity;
                    return this.targetEntity;
                }
            }
        }
        
        // 目标不存在或已死亡
        this.targetEntity = null;
        this.targetUUID = null;
        this.isTracking = false;
        return null;
    }
    
    /**
     * 尝试生成细胞硬币吞噬特效
     * @param level 世界对象
     * @param owner 拥有者实体
     * @param direction 方向向量
     * @param attackDamage 攻击力
     * @param coinType 硬币类型
     */
    public static void trySpawnEffect(Level level, LivingEntity owner, Vec3 direction, float attackDamage, String coinType) {
        if (!level.isClientSide && ModEntityTypes.OOO_GEO_EFFECT.get() != null) {
            // 创建特效实体
            OOOGeoEntity effect = new OOOGeoEntity(level, owner, direction, attackDamage, coinType);
            level.addFreshEntity(effect);
        }
    }
}