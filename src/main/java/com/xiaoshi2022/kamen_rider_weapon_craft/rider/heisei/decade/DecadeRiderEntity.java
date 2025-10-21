package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.decade;

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
import net.minecraft.core.BlockPos;
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
 * Kamen Rider Decade 实体类
 * 次元踢特效实体，用于表现Decade骑士的招牌技能Dimension Kick
 */
public class DecadeRiderEntity extends Projectile implements GeoEntity {
    // 实体数据同步器
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(DecadeRiderEntity.class, EntityDataSerializers.FLOAT);
    
    // GeoEntity相关
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation DIMENSION_KICK_ANIMATION = RawAnimation.begin().thenPlay("dcd");
    
    // 特效参数
    private int lifeTicks = 0;
    private static final int MAX_LIFE_TICKS = 40; // 增加特效持续时间
    private boolean hasDealtDamage = false; // 是否已造成伤害
    private static final int DAMAGE_TICK = 10; // 造成伤害的时间点
    private static final int EXPLOSION_TICK = 30; // 爆炸的时间点
    
    // 主人UUID
    private UUID ownerUUID;
    
    public DecadeRiderEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }
    
    public DecadeRiderEntity(Level level, LivingEntity owner, float damage, Vec3 direction) {
        super(ModEntityTypes.DECADE_RIDER.get(), level);
        if (owner != null) {
            this.setOwnerUUID(owner.getUUID());
        }
        this.setDamage(damage);
        this.setDeltaMovement(direction.normalize().scale(2.0));
    }
    
    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_DAMAGE, 52.0f);
    }
    
    // 获取owner UUID
    public UUID getOwnerUUID() {
        return ownerUUID;
    }
    
    // 设置owner UUID
    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }
    
    public void setDamage(float damage) {
        this.entityData.set(DATA_DAMAGE, damage);
    }
    
    public float getDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }
    
    @Override
    public void tick() {
        super.tick();
        lifeTicks++;
        
        // 生成粒子效果
        if (this.level().isClientSide) {
            spawnParticles();
        }
        
        // 非客户端逻辑
        if (!this.level().isClientSide) {
            // 在特定时间点造成伤害
            if (lifeTicks == DAMAGE_TICK && !hasDealtDamage) {
                dealDamageToArea();
                hasDealtDamage = true;
            }
            
            // 在特定时间点触发爆炸
            if (lifeTicks == EXPLOSION_TICK) {
                createExplosion();
            }
        }
        
        // 生命周期结束时销毁
        if (lifeTicks >= MAX_LIFE_TICKS) {
            this.remove(Entity.RemovalReason.DISCARDED);
        }
    }
    
    private void spawnParticles() {
        // 生成次元踢的标志性粒子
        for (int i = 0; i < 5; i++) {
            Vec3 pos = this.position().add(this.random.nextGaussian() * 0.5, this.random.nextGaussian() * 0.5, this.random.nextGaussian() * 0.5);
            this.level().addParticle(ParticleTypes.PORTAL, pos.x, pos.y, pos.z, 0, 0, 0);
        }
    }
    
    private void dealDamageToArea() {
        // 创建一个向前5格的长方体碰撞箱
        net.minecraft.world.phys.AABB damageBox = new net.minecraft.world.phys.AABB(
            this.position().x - 1.5, this.position().y - 1.0, this.position().z - 1.5,
            this.position().x + 1.5, this.position().y + 2.0, this.position().z + 5.0
        );
        
        // 旋转碰撞箱以匹配实体朝向
        damageBox = rotateAABB(damageBox, this.getYRot() * (float)Math.PI / 180.0F);
        
        java.util.List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, damageBox);
        LivingEntity owner = this.getOwner();
        
        for (LivingEntity entity : entities) {
            if (entity != owner && entity.isAlive()) {
                // 造成伤害
                entity.hurt(this.level().damageSources().mobProjectile(this, owner), this.getDamage());
                
                // 添加击退效果，朝着实体前方
                Vec3 lookVector = this.getLookAngle().normalize();
                entity.push(lookVector.x * 1.5, 0.5, lookVector.z * 1.5);
            }
        }
    }
    
    private void createExplosion() {
        // 创建一个中等规模的爆炸，不破坏方块
        this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, Level.ExplosionInteraction.NONE);
    }
    
    // 旋转AABB以匹配实体朝向
    private net.minecraft.world.phys.AABB rotateAABB(net.minecraft.world.phys.AABB box, float radians) {
        double centerX = (box.minX + box.maxX) / 2;
        double centerZ = (box.minZ + box.maxZ) / 2;
        
        double minX = box.minX - centerX;
        double maxX = box.maxX - centerX;
        double minZ = box.minZ - centerZ;
        double maxZ = box.maxZ - centerZ;
        
        // 旋转四个角点
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        
        // 计算旋转后的边界
        double newMinX = Double.MAX_VALUE;
        double newMaxX = Double.MIN_VALUE;
        double newMinZ = Double.MAX_VALUE;
        double newMaxZ = Double.MIN_VALUE;
        
        // 旋转四个角点并找出新的边界
        double[][] corners = {{minX, minZ}, {maxX, minZ}, {minX, maxZ}, {maxX, maxZ}};
        for (double[] corner : corners) {
            double x = corner[0] * cos - corner[1] * sin;
            double z = corner[0] * sin + corner[1] * cos;
            
            newMinX = Math.min(newMinX, x);
            newMaxX = Math.max(newMaxX, x);
            newMinZ = Math.min(newMinZ, z);
            newMaxZ = Math.max(newMaxZ, z);
        }
        
        return new net.minecraft.world.phys.AABB(
            centerX + newMinX, box.minY, centerZ + newMinZ,
            centerX + newMaxX, box.maxY, centerZ + newMaxZ
        );
    }
    
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Damage", this.getDamage());
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
        compound.putInt("LifeTicks", lifeTicks);
        compound.putBoolean("HasDealtDamage", hasDealtDamage);
    }
    
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setDamage(compound.getFloat("Damage"));
        if (compound.hasUUID("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
        lifeTicks = compound.getInt("LifeTicks");
        hasDealtDamage = compound.getBoolean("HasDealtDamage");
    }
    
    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != this.getOwner();
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
    
    // 移除不兼容的onHitEntity方法实现
    
    // GeoEntity相关方法
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "dimension_kick_controller", 0, this::predicate));
    }
    
    private <E extends DecadeRiderEntity> PlayState predicate(AnimationState<E> event) {
        event.getController().setAnimation(DIMENSION_KICK_ANIMATION);
        return PlayState.CONTINUE;
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    
    // 静态方法，用于生成Decade骑士特效
    public static void trySpawnEffect(Level level, LivingEntity owner, Vec3 direction, float damage) {
        if (!level.isClientSide) {
            // 计算在玩家面前5格的位置
            Vec3 lookVector = owner.getLookAngle().normalize();
            Vec3 spawnPos = owner.position().add(lookVector.x * 5, owner.getEyeHeight() * 0.5, lookVector.z * 5);
            
            DecadeRiderEntity entity = new DecadeRiderEntity(level, owner, damage, direction);
            entity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            
            // 设置实体朝向与玩家相同
            entity.setYRot(owner.getYRot());
            entity.setXRot(owner.getXRot());
            
            level.addFreshEntity(entity);
        }
    }
}