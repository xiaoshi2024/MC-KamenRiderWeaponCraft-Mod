package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.gaim;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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

import static software.bernie.geckolib.core.animation.Animation.LoopType.PLAY_ONCE;

/**
 * 铠武锁种实体类
 * 用于渲染铠武骑士的平成驾驭剑特效
 */
public class GaimLockSeedEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation MOVE_ANIMATION = RawAnimation.begin().thenPlay("move");
    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().then("attack", PLAY_ONCE);
    
    private Entity owner;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 100; // 约5秒
    private Vec3 attackDirection;
    private float damage;
    private boolean hasAttacked = false;
    private int removeDelay = 0; // 用于延迟移除实体的计数器
    
    // 锁种类型
    private static final EntityDataAccessor<String> LOCK_SEED_TYPE = 
            SynchedEntityData.defineId(GaimLockSeedEntity.class, EntityDataSerializers.STRING);
    
    // 攻击状态
    private static final EntityDataAccessor<Boolean> ATTACKED = 
            SynchedEntityData.defineId(GaimLockSeedEntity.class, EntityDataSerializers.BOOLEAN);

    // 移除属性相关的静态方法

    public GaimLockSeedEntity(EntityType<? extends GaimLockSeedEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvulnerable(true);
        // 初始化attackDirection，避免null指针异常
        this.attackDirection = new Vec3(0, 0, 1);
    }

    public GaimLockSeedEntity(Level level, Player owner, Vec3 position, Vec3 direction, String lockSeedType, float damage) {
        super(ModEntityTypes.GAIM_LOCK_SEED.get(), level);
        this.setPos(position);
        this.owner = owner;
        this.attackDirection = direction.normalize();
        this.setLockSeedType(lockSeedType);
        this.damage = damage * 0.75F; // 降低25%的伤害以平衡游戏
        this.noPhysics = true;
        this.setInvulnerable(true);
        
        // 设置正确的朝向
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yRot = (float)Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float xRot = (float)Math.toDegrees(Math.atan2(direction.y, horizontalDistance));
        this.setYRot(yRot);
        this.setXRot(xRot);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(LOCK_SEED_TYPE, "ORANGE");
        this.entityData.define(ATTACKED, false);
    }

    public String getLockSeedType() {
        return this.entityData.get(LOCK_SEED_TYPE);
    }

    public void setLockSeedType(String type) {
        this.entityData.set(LOCK_SEED_TYPE, type);
    }

    public boolean hasAttacked() {
        return this.entityData.get(ATTACKED);
    }

    public void setAttacked(boolean attacked) {
        this.entityData.set(ATTACKED, attacked);
        this.hasAttacked = attacked;
    }

    @Override
    public void tick() {
        super.tick();

        lifetime++;
        
        // 检查是否需要延迟移除
        if (removeDelay > 0) {
            removeDelay--;
            if (removeDelay <= 0) {
                this.remove(RemovalReason.DISCARDED);
                return;
            }
        } else if (lifetime > MAX_LIFETIME) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        // 更新位置 - 添加空指针检查
        if (attackDirection != null) {
            this.move(MoverType.SELF, attackDirection.scale(0.4));
        }
        
        // 检测碰撞
        checkCollisions();
        
        // 对于橙子锁种，执行碰撞切割目标的逻辑
        if (this.getLockSeedType().equals("ORANGE")) {
            cutEntitiesOnCollision();
        }
    }
    
    /**
     * 橙子锁种的碰撞切割逻辑
     * 切割碰到的所有敌对玩家和敌对生物
     */
    private void cutEntitiesOnCollision() {
        if (level().isClientSide || owner == null) {
            return;
        }
        
        // 获取实体周围的生物和玩家
        AABB collisionBox = this.getBoundingBox().inflate(0.3); // 稍微扩大碰撞范围
        
        for (Entity entity : level().getEntities(this, collisionBox)) {
            // 确保实体是LivingEntity且不是自己
            if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity && entity != owner) {
                // 判断是否为敌对目标
                if (isHostileTarget(livingEntity)) {
                    // 对目标造成伤害
                    float cutDamage = damage * 0.3f; // 切割伤害为基础伤害的30%
                    livingEntity.hurt(level().damageSources().playerAttack((net.minecraft.world.entity.player.Player) owner), cutDamage);
                    
                    // 添加视觉效果，例如粒子
                    if (level().isClientSide) {
                        // 客户端粒子效果可以在这里添加
                    }
                }
            }
        }
    }
    
    /**
     * 判断目标是否为敌对目标
     */
    private boolean isHostileTarget(net.minecraft.world.entity.LivingEntity entity) {
        if (owner instanceof net.minecraft.world.entity.player.Player player) {
            // 敌对玩家
            if (entity instanceof net.minecraft.world.entity.player.Player && 
                !player.getTeam().equals(((net.minecraft.world.entity.player.Player) entity).getTeam()) &&
                player.canHarmPlayer((net.minecraft.world.entity.player.Player) entity)) {
                return true;
            }
            
            // 敌对生物
            if (entity.getType().getCategory().isFriendly() == false && entity.isAlive()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查实体碰撞
     */
    private void checkCollisions() {
        // 检查是否已经爆炸过
        if (hasAttacked()) {
            return;
        }
        
        // 获取当前位置和周围方块位置
        BlockPos currentPos = this.blockPosition();
        
        // 检查当前位置和相邻位置的方块是否有碰撞
        if (!level().isClientSide) {
            // 检查当前位置的方块
            BlockState currentState = level().getBlockState(currentPos);
            if (!currentState.isAir() && currentState.getCollisionShape(level(), currentPos).isEmpty() == false) {
                onCollision();
                return;
            }
            
            // 检查相邻位置的方块
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos adjacentPos = currentPos.offset(x, y, z);
                        BlockState adjacentState = level().getBlockState(adjacentPos);
                        if (!adjacentState.isAir() && 
                            adjacentState.getCollisionShape(level(), adjacentPos).isEmpty() == false &&
                            adjacentPos.distSqr(currentPos) <= 2.0) { // 只检查一定范围内的相邻方块
                            onCollision();
                            return;
                        }
                    }
                }
            }
        }
        
        // 检查与实体的碰撞
        AABB boundingBox = this.getBoundingBox().inflate(0.2, 0.2, 0.2);
        for (Entity entity : this.level().getEntities(this, boundingBox)) {
            if (entity instanceof LivingEntity && entity != owner) {
                // 检测到实体碰撞
                onCollision();
                return;
            }
        }
    }
    
    /**
     * 处理碰撞事件
     */
    private void onCollision() {
        // 标记为已攻击
        this.setAttacked(true);
        
        if (this.getLockSeedType().equals("PINEAPPLE")) {
            // 对于菠萝锁种，触发爆炸效果
            // 服务器端才执行爆炸效果
            if (!level().isClientSide) {
                float explosionPower = damage / 3.0f; // 根据伤害调整爆炸威力
                level().explode(this, this.getX(), this.getY(), this.getZ(), 
                        explosionPower, Level.ExplosionInteraction.MOB);
            }
            
            // 设置延迟移除计数器，让爆炸动画播放
            this.removeDelay = 5; // 延迟5个tick后移除实体
        } else if (this.getLockSeedType().equals("ORANGE")) {
            // 对于橙子锁种，不立即移除，而是继续存在并切割目标
            // 橙子锁种的碰撞伤害在tick方法中处理，这里只标记为已攻击
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        // 读取保存的数据
        this.setLockSeedType(compound.getString("LockSeedType"));
        this.setAttacked(compound.getBoolean("Attacked"));
        
        // 读取伤害值
        if (compound.contains("Damage")) {
            this.damage = compound.getFloat("Damage");
        }
        
        // 读取owner的UUID
        if (compound.hasUUID("OwnerUUID")) {
            UUID ownerUUID = compound.getUUID("OwnerUUID");
            // 查找owner实体（在实体加载时处理，这里只保存UUID）
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        // 保存数据
        compound.putString("LockSeedType", this.getLockSeedType());
        compound.putBoolean("Attacked", this.hasAttacked());
        
        // 保存伤害值
        compound.putFloat("Damage", this.damage);
        
        // 保存owner的UUID
        if (this.owner != null) {
            compound.putUUID("OwnerUUID", this.owner.getUUID());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<GaimLockSeedEntity> event) {
        if (this.hasAttacked()) {
            event.getController().setAnimation(ATTACK_ANIMATION);
        } else {
            event.getController().setAnimation(MOVE_ANIMATION);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}