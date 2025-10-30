package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.Faiz;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import static software.bernie.geckolib.core.animation.Animation.LoopType.PLAY_ONCE;

public class FaizEmptySetEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation APPEAR_ANIMATION = RawAnimation.begin().then("appear", PLAY_ONCE);
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().then("idle", PLAY_ONCE);
    private static final RawAnimation DISAPPEAR_ANIMATION = RawAnimation.begin().then("disappear", PLAY_ONCE);
    
    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(FaizEmptySetEntity.class, EntityDataSerializers.INT);
    private static final int MAX_LIFETIME = 40; // 实体持续时间（游戏刻）
    
    public FaizEmptySetEntity(EntityType<? extends FaizEmptySetEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true; // 实体不受物理影响
        this.setInvulnerable(true); // 使实体无敌
    }
    
    // 从敌人位置创建空集实体的便捷构造方法
    public FaizEmptySetEntity(Level level, LivingEntity enemy) {
        super(ModEntityTypes.FAIZ_EMPTY_SET.get(), level);
        this.noPhysics = true;
        this.setInvulnerable(true);
        
        // 设置位置在敌人上方稍微一点
        if (enemy != null) {
            this.setPos(enemy.getX(), enemy.getY() + enemy.getBbHeight() * 0.5, enemy.getZ());
        }
    }
    
    @Override
    protected void defineSynchedData() {
        this.entityData.define(LIFETIME, MAX_LIFETIME);
    }
    
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(LIFETIME, compound.getInt("Lifetime"));
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Lifetime", this.entityData.get(LIFETIME));
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 减少生命周期
        int currentLifetime = this.entityData.get(LIFETIME) - 1;
        this.entityData.set(LIFETIME, currentLifetime);
        
        // 生命周期结束时移除实体
        if (currentLifetime <= 0) {
            this.remove(RemovalReason.DISCARDED);
        }
    }
    
    // GeoEntity接口实现方法
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    
    // 动画控制逻辑
    private PlayState predicate(AnimationState<FaizEmptySetEntity> event) {
        int lifetime = this.entityData.get(LIFETIME);
        
        // 根据生命周期选择不同的动画
        if (lifetime > MAX_LIFETIME - 10) {
            // 出现动画
            event.getController().setAnimation(APPEAR_ANIMATION);
        } else if (lifetime > 10) {
            // 空闲动画
            event.getController().setAnimation(IDLE_ANIMATION);
        } else {
            // 消失动画
            event.getController().setAnimation(DISAPPEAR_ANIMATION);
        }
        
        return PlayState.CONTINUE;
    }
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
    // 防止实体被推动
    @Override
    public boolean isPushable() {
        return false;
    }
    
    // 获取剩余生命周期比例，用于渲染
    @OnlyIn(Dist.CLIENT)
    public float getAnimationProgress() {
        return 1.0F - (float)this.entityData.get(LIFETIME) / (float)MAX_LIFETIME;
    }
    
    // 获取剩余生命周期
    public int getRemainingTicks() {
        return this.entityData.get(LIFETIME);
    }
    
    // 获取最大生命周期
    public int getMaxTicks() {
        return MAX_LIFETIME;
    }
    
    // 判断是否正在消失阶段（用于渲染）
    public boolean isDisappearing() {
        return this.entityData.get(LIFETIME) <= 10;
    }
    
    // 判断是否正在出现阶段（用于渲染）
    public boolean isAppearing() {
        return this.entityData.get(LIFETIME) > MAX_LIFETIME - 10;
    }
}