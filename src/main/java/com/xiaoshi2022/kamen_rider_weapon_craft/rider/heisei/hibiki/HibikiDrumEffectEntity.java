package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.hibiki;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

import java.util.UUID;

import static software.bernie.geckolib.core.animation.Animation.LoopType.LOOP;
import static software.bernie.geckolib.core.animation.Animation.LoopType.PLAY_ONCE;

public class HibikiDrumEffectEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().then("animation.hibiki_drum.idle", LOOP);
    private static final RawAnimation CHARGE_ANIMATION = RawAnimation.begin().then("animation.hibiki_drum.charge", PLAY_ONCE);
    private static final RawAnimation EXPLOSION_ANIMATION = RawAnimation.begin().then("animation.hibiki_drum.explosion", PLAY_ONCE);
    
    private Entity owner;
    private LivingEntity targetEntity;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 200; // 持续时间10秒
    private boolean isCharging = false;
    private int chargeTicks = 0;
    private static final int CHARGE_DURATION = 60; // 蓄力时间3秒
    private boolean hasExploded = false;
    private double damageAmount = 45.0; // 必杀伤害
    
    private static final EntityDataAccessor<Integer> TARGET_ENTITY_ID = SynchedEntityData.defineId(HibikiDrumEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_TARGET = SynchedEntityData.defineId(HibikiDrumEffectEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_CHARGING = SynchedEntityData.defineId(HibikiDrumEffectEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_EXPLODED = SynchedEntityData.defineId(HibikiDrumEffectEntity.class, EntityDataSerializers.BOOLEAN);
    
    public HibikiDrumEffectEntity(EntityType<? extends HibikiDrumEffectEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvulnerable(true);
    }
    
    public HibikiDrumEffectEntity(Level level, Entity owner, LivingEntity target) {
        super(ModEntityTypes.HIBIKI_DRUM_EFFECT.get(), level);
        this.owner = owner;
        this.targetEntity = target;
        this.noPhysics = true;
        this.setInvulnerable(true);
        
        // 在服务器端同步目标实体信息
        if (!level.isClientSide) {
            syncTargetEntity(target);
        }
        
        // 初始化位置在目标肚子前方
        updatePositionToTarget();
    }
    
    @Override
    public void tick() {
        super.tick();
        
        lifetime++;
        
        // 在客户端，根据同步的数据更新实体引用
        if (this.level().isClientSide) {
            updateTargetEntityFromSyncedData();
        }
        
        // 更新状态
        this.entityData.set(IS_CHARGING, isCharging);
        this.entityData.set(HAS_EXPLODED, hasExploded);
        
        // 如果已经爆炸，等待动画播放完成后移除
        if (hasExploded) {
            if (lifetime > MAX_LIFETIME - 20) {
                this.remove(RemovalReason.DISCARDED);
                return;
            }
            return;
        }
        
        // 如果目标存在且没有爆炸，继续追踪目标
        if (targetEntity != null && !targetEntity.isRemoved() && !targetEntity.isDeadOrDying()) {
            // 持续控制目标
            applyControlEffect(targetEntity);
            
            // 更新位置到目标肚子前方
            updatePositionToTarget();
            
            // 开始蓄力
            if (!isCharging && lifetime > 20) { // 延迟20tick开始蓄力
                isCharging = true;
                chargeTicks = 0;
            }
            
            // 蓄力过程
            if (isCharging) {
                chargeTicks++;
                
                // 蓄力完成，触发爆炸
                if (chargeTicks >= CHARGE_DURATION) {
                    triggerExplosion();
                }
            }
        } else {
            // 目标不存在或已死亡，移除特效
            this.remove(RemovalReason.DISCARDED);
            return;
        }
        
        // 超出生命周期，移除特效
        if (lifetime > MAX_LIFETIME) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }
    }
    
    // 更新位置到目标肚子前方
    private void updatePositionToTarget() {
        if (targetEntity != null && !targetEntity.isRemoved()) {
            // 获取目标的朝向
            Vec3 lookVector = targetEntity.getViewVector(1.0F).normalize();
            
            // 计算目标前方的位置，稍微偏移以确保在肚子前方
            double offsetDistance = 1.5; // 距离目标的距离
            double heightOffset = targetEntity.getBbHeight() * 0.4; // 高度偏移到肚子位置
            
            Vec3 offsetPos = targetEntity.position()
                    .add(0, heightOffset, 0)
                    .add(lookVector.scale(offsetDistance));
            
            // 设置位置
            this.setPos(offsetPos.x, offsetPos.y, offsetPos.z);
            
            // 设置朝向与目标相反
            this.setYRot(targetEntity.getYRot() + 180);
            this.setXRot(0);
        }
    }
    
    // 对目标施加控制效果
    private void applyControlEffect(LivingEntity target) {
        if (!level().isClientSide) {
            // 施加束缚效果，阻止目标移动
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false));
            
            // 施加挖掘疲劳，降低攻击速度
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20, 3, false, false));
            
            // 添加发光效果作为视觉反馈
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20, 0, false, false));
            
            // 阻止目标跳跃
            target.setDeltaMovement(target.getDeltaMovement().x, Math.min(target.getDeltaMovement().y, 0), target.getDeltaMovement().z);
        }
    }
    
    // 触发爆炸攻击
    private void triggerExplosion() {
        if (hasExploded || level().isClientSide) return;
        
        hasExploded = true;
        
        // 对目标造成伤害
        if (targetEntity != null && !targetEntity.isRemoved()) {
            // 创建伤害源
            net.minecraft.world.damagesource.DamageSource damageSource;
            if (owner instanceof Player player) {
                damageSource = level().damageSources().playerAttack(player);
            } else if (owner instanceof net.minecraft.world.entity.Mob mob) {
                damageSource = level().damageSources().mobAttack(mob);
            } else {
                damageSource = level().damageSources().magic();
            }
            
            // 造成伤害
            targetEntity.hurt(damageSource, (float) damageAmount);
            
            // 施加击退效果
            Vec3 lookVector = targetEntity.getViewVector(1.0F).normalize();
            targetEntity.setDeltaMovement(lookVector.scale(-2.0));
            
            // 添加燃烧效果
            targetEntity.setSecondsOnFire(5);
        }
        
        // 播放爆炸音效
        level().playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
    }
    
    // 设置目标实体
    public void setTargetEntity(LivingEntity targetEntity) {
        this.targetEntity = targetEntity;
        if (!this.level().isClientSide) {
            syncTargetEntity(targetEntity);
        }
    }
    
    // 同步目标实体信息到客户端
    private void syncTargetEntity(LivingEntity entity) {
        if (entity != null) {
            this.entityData.set(TARGET_ENTITY_ID, entity.getId());
            this.entityData.set(HAS_TARGET, true);
        } else {
            this.entityData.set(TARGET_ENTITY_ID, 0);
            this.entityData.set(HAS_TARGET, false);
        }
    }
    
    // 在客户端根据同步的数据更新targetEntity引用
    private void updateTargetEntityFromSyncedData() {
        if (this.entityData.get(HAS_TARGET)) {
            int entityId = this.entityData.get(TARGET_ENTITY_ID);
            if (entityId > 0) {
                Entity entity = this.level().getEntity(entityId);
                if (entity instanceof LivingEntity livingEntity && !entity.isRemoved()) {
                    this.targetEntity = livingEntity;
                } else {
                    this.entityData.set(HAS_TARGET, false);
                    this.entityData.set(TARGET_ENTITY_ID, 0);
                    this.targetEntity = null;
                }
            }
        }
    }
    
    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.targetEntity = null;
        this.owner = null;
    }
    
    private PlayState animationPredicate(AnimationState<HibikiDrumEffectEntity> event) {
        if (this.entityData.get(HAS_EXPLODED)) {
            event.getController().setAnimation(EXPLOSION_ANIMATION);
        } else if (this.entityData.get(IS_CHARGING)) {
            event.getController().setAnimation(CHARGE_ANIMATION);
        } else {
            event.getController().setAnimation(IDLE_ANIMATION);
        }
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
        this.entityData.define(TARGET_ENTITY_ID, 0);
        this.entityData.define(HAS_TARGET, false);
        this.entityData.define(IS_CHARGING, false);
        this.entityData.define(HAS_EXPLODED, false);
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.lifetime = nbt.getInt("Lifetime");
        this.isCharging = nbt.getBoolean("IsCharging");
        this.chargeTicks = nbt.getInt("ChargeTicks");
        this.hasExploded = nbt.getBoolean("HasExploded");
        this.damageAmount = nbt.getDouble("DamageAmount");
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("Lifetime", this.lifetime);
        nbt.putBoolean("IsCharging", this.isCharging);
        nbt.putInt("ChargeTicks", this.chargeTicks);
        nbt.putBoolean("HasExploded", this.hasExploded);
        nbt.putDouble("DamageAmount", this.damageAmount);
    }
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
    // 静态方法用于生成特效
    public static void spawnEffect(Level level, Entity owner, LivingEntity target) {
        // 只在服务器端生成实体
        if (!level.isClientSide && target != null && !target.isRemoved()) {
            HibikiDrumEffectEntity effect = new HibikiDrumEffectEntity(level, owner, target);
            level.addFreshEntity(effect);
        }
    }
    
    // 用于渲染器获取实体状态的getter方法
    public boolean isCharging() {
        return this.entityData.get(IS_CHARGING);
    }
    
    public boolean hasExploded() {
        return this.entityData.get(HAS_EXPLODED);
    }
    
    public LivingEntity getTargetEntity() {
        return this.targetEntity;
    }
}