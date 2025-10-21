package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.build;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
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

import java.util.List;
import java.util.UUID;

/**
 * Kamen Rider Build 实体类
 * 气泡兔坦形态的公式踢特效实体
 */
public class BuildRiderEntity extends Projectile implements GeoEntity {
    // 动画常量定义
    private static final RawAnimation MOVE_ANIMATION = RawAnimation.begin().thenPlay("move");
    private static final RawAnimation HIT_ANIMATION = RawAnimation.begin().thenPlay("attack.hit");

    // 动画实例缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 实体存活时间（刻）
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 100; // 约5秒

    // 攻击方向
    private Vec3 attackDirection;

    // 存储owner的UUID
    private UUID ownerUUID = null;

    // 特效触发几率
    private static final float TRIGGER_CHANCE = 0.3f; // 30%几率触发

    // 同步数据定义
    private static final EntityDataAccessor<Boolean> HAS_HIT_ENTITY = SynchedEntityData.defineId(BuildRiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(BuildRiderEntity.class, EntityDataSerializers.FLOAT);

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
    private BuildRiderEntity(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        super(ModEntityTypes.BUILD_RIDER_EFFECT.get(), level);
        this.setOwner(owner);
        this.noPhysics = true;
        this.setPos(owner.getEyePosition().add(direction.scale(1.0)));
        this.attackDirection = direction;
        this.setDamage(attackDamage);
        this.setOwnerUUID(owner.getUUID());
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
    }

    // 公共构造函数，用于注册
    public BuildRiderEntity(EntityType<? extends BuildRiderEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        // 初始化攻击方向，避免空指针异常
        this.attackDirection = Vec3.ZERO;
    }

    // 尝试生成普通特效实体的静态方法
    public static void trySpawnEffect(Level level, LivingEntity owner, Vec3 direction, float attackDamage) {
        // 随机决定是否生成特效
        if (level.random.nextFloat() <= TRIGGER_CHANCE && !level.isClientSide) {
            BuildRiderEntity effect = new BuildRiderEntity(level, owner, direction, attackDamage);
            level.addFreshEntity(effect);
        }
    }

    @Override
    protected void defineSynchedData() {
        // 定义需要同步的数据
        this.getEntityData().define(HAS_HIT_ENTITY, false);
        this.getEntityData().define(DAMAGE, 0.0f);
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        compound.putBoolean("HasHitEntity", this.hasHitEntity());
        compound.putInt("Lifetime", this.lifetime);
        compound.putFloat("Damage", this.getDamage());
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
    private PlayState animationPredicate(AnimationState<BuildRiderEntity> state) {
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
            // 向前移动
                Vec3 currentPos = this.position();
                // 安全检查：确保attackDirection不为null
                if (attackDirection != null) {
                    Vec3 newPos = currentPos.add(attackDirection.scale(0.6));
                    this.setPos(newPos.x, newPos.y, newPos.z);

                    // 检测碰撞
                    this.checkEntityCollision();
                } else {
                    // 如果攻击方向为null，设置为默认方向并继续
                    this.attackDirection = Vec3.ZERO;
                }
        }
        
        // 吸引半径5格内的生物 - 移到移动之后调用，确保移动后仍然吸引周围生物
        this.attractEntities();
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

        // 修复伤害来源创建方式 - 移除模式匹配语法
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

        // 首先尝试查找玩家（玩家是最常见的主人）
        Entity owner = this.level().getPlayerByUUID(uuid);
        if (owner instanceof LivingEntity) {
            return (LivingEntity) owner;
        }

        // 如果不是玩家，通过获取范围内实体的方式来查找
        // 使用 getEntitiesOfClass 方法，这是公共方法
        AABB searchArea = this.getBoundingBox().inflate(32.0D); // 扩大搜索范围，例如32格
        List<Entity> entities = this.level().getEntities(this, searchArea, entity ->
                entity instanceof LivingEntity && entity.getUUID().equals(uuid)
        );

        if (!entities.isEmpty()) {
            return (LivingEntity) entities.get(0);
        }

        return null;
    }
    
    // 吸引半径5格内的生物到特效中心
    private void attractEntities() {
        if (this.level().isClientSide()) return;

        Vec3 center = this.position();
        AABB searchArea = this.getBoundingBox().inflate(5.0D); // 半径5格

        // 获取范围内的所有可移动实体
        List<Entity> entities = this.level().getEntities(this, searchArea, entity ->
                entity instanceof LivingEntity &&
                        entity != this.getOwner() &&
                        entity.isAlive() &&
                        !(entity instanceof Projectile) &&
                        !entity.isSpectator() // 排除旁观者模式玩家
        );

        for (Entity entity : entities) {
            // 计算实体到特效中心的向量
            Vec3 entityPos = entity.position();
            Vec3 toCenter = center.subtract(entityPos);
            double distance = toCenter.length();

            // 如果距离很近，跳过避免过度拉扯
            if (distance < 0.5) continue;

            // 标准化方向向量
            Vec3 direction = toCenter.normalize();

            // 计算引力强度 - 使用平方反比定律，距离越近引力越强
            double maxDistance = 5.0;
            double minForce = 0.1;  // 最小引力
            double maxForce = 0.8;  // 最大引力

            // 引力随距离增加而减小，使用平滑的曲线
            double normalizedDistance = distance / maxDistance;
            double force = maxForce * (1.0 - normalizedDistance * normalizedDistance);

            // 确保引力不小于最小值
            force = Math.max(force, minForce);

            // 应用引力 - 使用更平滑的移动
            Vec3 attraction = direction.scale(force * 0.3); // 乘以系数控制总体强度

            // 对实体应用移动
            if (entity instanceof LivingEntity livingEntity) {
                // 对于生物实体，使用更自然的移动方式
                Vec3 currentMotion = entity.getDeltaMovement();
                Vec3 newMotion = currentMotion.add(attraction);

                // 限制最大速度避免过度加速
                double maxSpeed = 1.5;
                if (newMotion.length() > maxSpeed) {
                    newMotion = newMotion.normalize().scale(maxSpeed);
                }

                entity.setDeltaMovement(newMotion);

                // 设置实体已经移动，避免服务器重置位置
                livingEntity.hurtMarked = true;
            } else {
                // 对于其他实体直接应用推力
                entity.push(attraction.x, attraction.y, attraction.z);
            }

            // 增加粒子效果 - 只在有实际移动时生成
            if (force > minForce * 1.5 && this.level().random.nextFloat() < 0.3) {
                // 使用更合适的粒子类型
                this.level().addParticle(ParticleTypes.ENCHANT,
                        entityPos.x, entityPos.y + entity.getBbHeight() * 0.5, entityPos.z,
                        direction.x * 0.2, direction.y * 0.2, direction.z * 0.2);
            }

            // 偶尔播放吸引音效
            if (this.level().random.nextFloat() < 0.02) {
                this.level().playSound(null, center.x, center.y, center.z,
                        net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
                        SoundSource.NEUTRAL, 0.3F, 1.5F);
            }
        }
    }
    
    // 爆炸效果
    private void explode() {
        if (this.level().isClientSide()) return;
        
        Vec3 center = this.position();
        
        // 创建爆炸效果，不破坏方块
        this.level().explode(this, center.x, center.y, center.z, 
                2.0F, Level.ExplosionInteraction.NONE);
        
        // 对范围内的实体造成额外伤害
        AABB explosionArea = this.getBoundingBox().inflate(3.0D); // 爆炸范围3格
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, explosionArea, 
                entity -> entity != this.getOwner() && entity.isAlive());
        
        for (LivingEntity entity : entities) {
            // 对范围内的敌人造成伤害
            float explosionDamage = this.getDamage() * 0.5F; // 爆炸伤害为原伤害的50%
            
            LivingEntity owner = this.getOwner();
            if (owner != null) {
                entity.hurt(this.damageSources().mobAttack(owner), explosionDamage);
            } else {
                entity.hurt(this.damageSources().magic(), explosionDamage);
            }
        }
    }
}