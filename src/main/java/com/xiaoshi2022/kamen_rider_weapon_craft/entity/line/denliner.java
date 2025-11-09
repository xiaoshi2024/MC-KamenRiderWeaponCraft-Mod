package com.xiaoshi2022.kamen_rider_weapon_craft.entity.line;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModSounds;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.NetworkHandler;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.SoundStopPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.List;

public class denliner extends LivingEntity implements GeoEntity {

    // 属性创建方法
    public static AttributeSupplier createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 0.0D).build();
    }

    // 构造函数
    public denliner(@NotNull EntityType<denliner> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = false;
        this.setNoGravity(true);
        // 移除 setInvulnerable(true) 以允许被杀死

        // 设置基础生命值
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return false;
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        this.checkInsideBlocks();
    }

    // 静态创建方法
    public static denliner create(Level level, double x, double y, double z, Player player) {
        denliner denliner = new denliner(ModEntityTypes.DENLINER.get(), level);
        denliner.setPos(x, y, z);
        denliner.setRider(player);
        denliner.startStandbySound();
        return denliner;
    }

    // 实体数据同步器
    private static final EntityDataAccessor<Boolean> IS_PLAYING_SOUND = SynchedEntityData.defineId(denliner.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Long> SOUND_START_TIME = SynchedEntityData.defineId(denliner.class, EntityDataSerializers.LONG);

    // 待机音持续时间（5秒 = 100刻）
    private static final int STANDBY_DURATION = 5 * 20;

    // 玩家引用
    private Player rider;

    // Geo 动画相关
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation RUN_ANIMATION = RawAnimation.begin().thenPlay("idle");

    // 用于从电假面剑创建电车的静态方法
    public static denliner create(ServerLevel level, Player player) {
        denliner train = new denliner(ModEntityTypes.DENLINER.get(), level);
        train.setPos(player.getX(), player.getY(), player.getZ());
        train.setRider(player);
        train.startStandbySound();
        level.addFreshEntity(train);
        return train;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_PLAYING_SOUND, false);
        this.entityData.define(SOUND_START_TIME, 0L);
    }

    // Geo 动画相关方法
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 20, this::predicate));
    }

    private <E extends Entity & GeoEntity> PlayState predicate(AnimationState<E> event) {
        if (this.isMoving()) {
            event.getController().setAnimation(RUN_ANIMATION);
            event.getController().setAnimationSpeed(1.0);
        } else {
            event.getController().setAnimation(IDLE_ANIMATION);
            event.getController().setAnimationSpeed(0.5);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // 检查实体是否在移动
    private boolean isMoving() {
        return this.getDeltaMovement().lengthSqr() > 0.001D;
    }

    // 设置骑乘者
    public void setRider(Player player) {
        if (player != null) {
            this.rider = player;
            if (!player.isPassengerOfSameVehicle(this)) {
                player.startRiding(this, true);
            }
        }
    }

    @Override
    public boolean canRiderInteract() {
        return true;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    // 关键修复：允许被特定伤害源杀死
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        // 简单直接地允许被任何伤害源伤害，包括/kill命令
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 允许所有伤害并执行清理
        this.cleanupBeforeRemoval();
        return super.hurt(source, amount);
    }

    // 关键修复：重写kill方法确保正确清理
    @Override
    public void kill() {
        this.cleanupBeforeRemoval();
        super.kill();
    }

    // 关键修复：重写die方法
    @Override
    public void die(DamageSource damageSource) {
        this.cleanupBeforeRemoval();
        super.die(damageSource);
    }

    // 关键修复：重写remove方法
    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.KILLED || reason == RemovalReason.DISCARDED) {
            this.cleanupBeforeRemoval();
        }
        super.remove(reason);
    }

    // 清理资源的方法
    private void cleanupBeforeRemoval() {
        this.stopStandbySound();

        // 使用网络数据包停止声音
        if (!this.level().isClientSide && this.rider != null) {
            // 创建音效资源位置
            ResourceLocation soundLoc = new ResourceLocation(
                    "kamen_rider_weapon_craft",
                    "den_o_lines"
            );
            
            // 发送到所有跟踪该玩家的客户端
            com.xiaoshi2022.kamen_rider_weapon_craft.network.NetworkHandler.sendToAllTracking(
                    new com.xiaoshi2022.kamen_rider_weapon_craft.network.SoundStopPacket(rider.getId(), soundLoc),
                    rider
            );
            
            // 同时在服务器端执行停止音效命令作为备份
            CommandSourceStack source = ((ServerLevel)this.level()).getServer().createCommandSourceStack()
                    .withSuppressedOutput()
                    .withPermission(2);

            try {
                // 使用@a选择器确保在多人服务器中向所有玩家发送停止音效命令
                // 移除distance限制以确保在大型服务器中所有玩家都能听到音效停止
                String command = String.format(
                        "/stopsound @a players %s",
                        soundLoc.toString()
                );
                ((ServerLevel)this.level()).getServer().getCommands().performPrefixedCommand(source, command);
            } catch (Exception e) {
                // 命令执行出错，继续执行，不影响其他清理操作
            }
        }

        // 释放骑士
        if (this.rider != null) {
            this.rider.stopRiding();
            this.rider = null;
        }
    }

    // 开始播放待机音
    public void startStandbySound() {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            this.entityData.set(IS_PLAYING_SOUND, true);
            this.entityData.set(SOUND_START_TIME, this.level().getGameTime());

            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.DEN_O_LINES.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    // 停止播放待机音
    public void stopStandbySound() {
        if (this.entityData.get(IS_PLAYING_SOUND)) {
            this.entityData.set(IS_PLAYING_SOUND, false);
        }
    }

    // 当玩家攻击敌人时调用此方法来停止音效并移除电车
    public void onEnemyAttacked() {
        this.cleanupBeforeRemoval();
        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(IS_PLAYING_SOUND, tag.getBoolean("isPlayingSound"));
        this.entityData.set(SOUND_START_TIME, tag.getLong("soundStartTime"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("isPlayingSound", this.entityData.get(IS_PLAYING_SOUND));
        tag.putLong("soundStartTime", this.entityData.get(SOUND_START_TIME));
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(2.0F, 1.0F);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(1.0D, 0.5D, 1.0D);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return List.of();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        // 不做任何事情
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 1.2D;
    }

    @Override
    public void tick() {
        super.tick();

        // 检查是否应该停止待机音
        if (this.entityData.get(IS_PLAYING_SOUND)) {
            long startTime = this.entityData.get(SOUND_START_TIME);
            long currentTime = this.level().getGameTime();

            if (currentTime - startTime > STANDBY_DURATION || (rider != null && !rider.isPassengerOfSameVehicle(this))) {
                this.cleanupBeforeRemoval();
                this.remove(RemovalReason.DISCARDED);
                return;
            }
        }

        // 对电车前方9格范围内的实体造成伤害
        this.damageEntitiesInFront();

        // 跟随骑手移动和转向
        if (this.rider != null && this.rider.isPassengerOfSameVehicle(this)) {
            // 只在服务器端更新位置和旋转
            if (!this.level().isClientSide) {
                this.updatePositionAndRotation();
            }
        }
    }

    // 对电车前方9格范围内的实体造成伤害
    private void damageEntitiesInFront() {
        // 只在服务器端执行伤害逻辑
        if (this.level().isClientSide) {
            return;
        }

        // 获取电车的旋转角度
        float rotationYaw = this.getYRot() * ((float) Math.PI / 180F);

        // 计算前方方向向量
        double frontX = Math.sin(-rotationYaw);
        double frontZ = Math.cos(-rotationYaw);

        // 伤害范围：前方9格，半径2格
        double range = 9.0D;
        double radius = 2.0D;

        // 计算检测区域的中心点（电车前方9格处）
        double centerX = this.getX() + frontX * range;
        double centerY = this.getY();
        double centerZ = this.getZ() + frontZ * range;

        // 创建检测区域的AABB
        AABB area = new AABB(
                centerX - radius, centerY - 1.0D, centerZ - radius,
                centerX + radius, centerY + 2.0D, centerZ + radius
        );

        // 获取区域内的所有实体
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, area, entity -> {
            // 排除电车本身和骑乘者
            return entity != this && entity != this.rider &&
                    !(entity instanceof Player && this.rider != null && ((Player)entity).isAlliedTo(this.rider));
        });

        // 对每个实体造成伤害
        for (LivingEntity entity : entities) {
            // 检查实体是否在电车的前方
            Vec3 entityPos = entity.position();
            Vec3 directionToEntity = entityPos.subtract(this.position());

            // 计算实体到电车的向量与电车前方的夹角
            double dotProduct = frontX * directionToEntity.x + frontZ * directionToEntity.z;

            // 只有夹角小于90度的实体才会受到伤害
            if (dotProduct > 0) {
                // 创建伤害源
                DamageSource damageSource = this.rider != null ?
                        this.rider.damageSources().playerAttack(this.rider) :
                        this.damageSources().mobAttack(this);

                // 对实体造成伤害
                entity.hurt(damageSource, 10.0F);

                // 添加击退效果
                double knockbackX = frontX * 2.0D;
                double knockbackZ = frontZ * 2.0D;
                entity.push(knockbackX, 0.5D, knockbackZ);
            }
        }
    }

    // 专门处理位置和旋转更新的方法
    private void updatePositionAndRotation() {
        if (this.rider == null) return;

        // 平滑的位置更新
        double targetX = this.rider.getX();
        double targetY = this.rider.getY() - 1.0D;
        double targetZ = this.rider.getZ();

        // 检查目标位置是否有方块碰撞
        if (!this.level().getBlockState(this.blockPosition().below()).isAir()) {
            targetY = Math.max(targetY, this.blockPosition().getY() + 0.1D);
        }

        // 使用平滑移动
        double currentX = this.getX();
        double currentY = this.getY();
        double currentZ = this.getZ();

        // 线性插值平滑移动
        double smoothX = currentX + (targetX - currentX) * 0.3;
        double smoothY = currentY + (targetY - currentY) * 0.3;
        double smoothZ = currentZ + (targetZ - currentZ) * 0.3;

        this.setPos(smoothX, smoothY, smoothZ);

        // 旋转同步
        this.updateRotationSmoothly();
    }

    // 平滑旋转更新
    private void updateRotationSmoothly() {
        if (this.rider == null) return;

        // 获取目标旋转
        float targetYRot = this.rider.getYRot();
        float targetXRot = this.rider.getXRot() * 0.5F;

        // 当前旋转
        float currentYRot = this.getYRot();
        float currentXRot = this.getXRot();

        // 角度差值计算
        float yRotDiff = targetYRot - currentYRot;
        float xRotDiff = targetXRot - currentXRot;

        // 规范化角度差值
        while (yRotDiff < -180.0F) yRotDiff += 360.0F;
        while (yRotDiff >= 180.0F) yRotDiff -= 360.0F;
        while (xRotDiff < -180.0F) xRotDiff += 360.0F;
        while (xRotDiff >= 180.0F) xRotDiff -= 360.0F;

        // 使用平滑插值
        float smoothYRot = currentYRot + yRotDiff * 0.3F;
        float smoothXRot = currentXRot + xRotDiff * 0.3F;

        // 设置旋转
        this.setYRot(smoothYRot);
        this.setXRot(smoothXRot);

        // 同步其他旋转值
        this.setYBodyRot(smoothYRot);
        this.setYHeadRot(smoothYRot);

        // 更新上一tick的旋转值
        this.yRotO = smoothYRot;
        this.xRotO = smoothXRot;
        this.yBodyRotO = smoothYRot;
        this.yHeadRotO = smoothYRot;
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return true;
    }

    @Override
    public void onPassengerTurned(Entity passenger) {
        if (passenger == this.rider) {
            this.updateRotationSmoothly();
        }
    }

    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            double yOffset = this.getPassengersRidingOffset() + passenger.getMyRidingOffset();

            Vec3 riderPos = new Vec3(0, yOffset, 0)
                    .yRot(-this.getYRot() * ((float)Math.PI / 180F));

            moveFunction.accept(passenger,
                    this.getX() + riderPos.x,
                    this.getY() + riderPos.y,
                    this.getZ() + riderPos.z
            );

            if (passenger instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) passenger;
                living.setYBodyRot(this.getYRot());
                living.setYHeadRot(this.getYRot());
            }
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide) {
            super.aiStep();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (this.level().isClientSide && this.rider != null) {
            this.updateRotationSmoothly();
        }
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    public boolean isPersistent() {
        return true;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isVehicle()) {
            if (this.getControllingPassenger() != null) {
                LivingEntity controller = this.getControllingPassenger();
                this.setYRot(controller.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(controller.getXRot() * 0.5F);
                this.setRot(this.getYRot(), this.getXRot());
            }
        }
        super.travel(Vec3.ZERO);
    }

    @Override
    public void setDeltaMovement(Vec3 deltaMovement) {
        super.setDeltaMovement(deltaMovement.scale(0.1));
    }

    @Override
    public void push(double x, double y, double z) {
        // 禁用推动
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return this.rider;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (passenger instanceof Player) {
            this.rider = (Player) passenger;
            this.updateRotationSmoothly();
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (passenger == this.rider) {
            this.rider = null;
        }
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    @Override
    public boolean isSleeping() {
        return false;
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public boolean mayInteract(Level level, net.minecraft.core.BlockPos pos) {
        return false;
    }
}