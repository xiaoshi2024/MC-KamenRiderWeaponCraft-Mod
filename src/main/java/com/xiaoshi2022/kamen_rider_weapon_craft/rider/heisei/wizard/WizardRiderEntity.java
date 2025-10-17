package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.wizard;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
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

import java.util.ArrayList;
import java.util.List;

public class WizardRiderEntity extends Projectile implements GeoEntity {
    // 元素魔龙力量类型
    public enum DragonMagicType {
        FlameDragon,
        WaterDragon,
        HurricaneDragon,
        LandDragon
    }

    // 同步数据定义
    private static final EntityDataAccessor<Integer> DRAGON_MAGIC_TYPE = SynchedEntityData.defineId(WizardRiderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(WizardRiderEntity.class, EntityDataSerializers.FLOAT);

    // 动画常量定义，确保一出现就播放对应动画
    private static final RawAnimation FLAME_DRAGON_ANIMATION = RawAnimation.begin().thenPlay("flamedragon");
    private static final RawAnimation WATER_DRAGON_ANIMATION = RawAnimation.begin().thenPlay("waterdragon");
    private static final RawAnimation HURRICANE_DRAGON_ANIMATION = RawAnimation.begin().thenPlay("hurricanedragon");
    private static final RawAnimation LAND_DRAGON_ANIMATION = RawAnimation.begin().thenPlay("landdragon");

    private int lifeTicks = 80; // 生命周期延长
    private boolean hasCollided = false; // 防止多次碰撞检测
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // 获取/设置同步属性的便捷方法
    public DragonMagicType getDragonMagicType() {
        return DragonMagicType.values()[this.getEntityData().get(DRAGON_MAGIC_TYPE)];
    }

    public void setDragonMagicType(DragonMagicType type) {
        this.getEntityData().set(DRAGON_MAGIC_TYPE, type.ordinal());
    }

    public float getDamage() {
        return this.getEntityData().get(DAMAGE);
    }

    public void setDamage(float value) {
        this.getEntityData().set(DAMAGE, value);
    }

    public WizardRiderEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.noCulling = true; // 确保渲染不会因为视锥体剔除而消失
    }

    // 静态工厂方法，用于生成特效实体
    public static void trySpawnEffect(Level level, LivingEntity shooter, Vec3 direction, float damage, DragonMagicType dragonMagicType) {
        if (level.isClientSide) return;

        WizardRiderEntity entity = new WizardRiderEntity(ModEntityTypes.WIZARD_EFFECT.get(), level);
        entity.setOwner(shooter);
        entity.setDragonMagicType(dragonMagicType);
        entity.setDamage(damage);
        entity.setPos(shooter.getEyePosition().add(direction.normalize().scale(1.5)));
        entity.shoot(direction.x, direction.y, direction.z, 1.0f, 0); // 降低速度，更符合魔龙体型
        level.addFreshEntity(entity);
    }

    @Override
    public void tick() {
        super.tick();

        // 减少生命周期
        if (--this.lifeTicks <= 0) {
            this.discard();
            return;
        }

        // 检测碰撞和伤害
        if (!this.level().isClientSide && !hasCollided) {
            List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(3.0)); // 扩大碰撞范围，符合魔龙体型

            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity && entity != this.getOwner() && !entity.isAlliedTo(this.getOwner())) {
                    this.hasCollided = true; // 标记已碰撞，防止重复处理

                    // 根据不同元素魔龙类型造成不同的伤害效果
                    switch (this.getDragonMagicType()) {
                        case FlameDragon:
                            handleFlameDragonEffect(livingEntity);
                            break;
                        case WaterDragon:
                            handleWaterDragonEffect(livingEntity);
                            break;
                        case HurricaneDragon:
                            handleHurricaneDragonEffect(livingEntity);
                            break;
                        case LandDragon:
                            handleLandDragonEffect(livingEntity);
                            break;
                    }

                    // 让实体继续存在一段时间再消失，而不是立即消失
                    this.lifeTicks = Math.min(this.lifeTicks, 20);
                    break; // 只处理第一个碰撞的实体
                }
            }
        }

        // 移动逻辑
        if (!this.hasCollided) {
            Vec3 deltaMovement = this.getDeltaMovement();
            this.setDeltaMovement(deltaMovement.multiply(0.98, 0.98, 0.98)); // 轻微减速
            this.setPos(this.getX() + deltaMovement.x, this.getY() + deltaMovement.y, this.getZ() + deltaMovement.z);
        }
    }

    private void handleFlameDragonEffect(LivingEntity target) {
        // 对目标造成火焰伤害
        target.hurt(this.level().damageSources().mobProjectile(this, (LivingEntity) this.getOwner()), this.getDamage());
        target.setSecondsOnFire(8); // 设置目标着火8秒

        // 从当前位置发射多个烈焰蛋
        for (int i = 0; i < 3; i++) {
            SmallFireball fireball = new SmallFireball(
                    this.level(),
                    (LivingEntity) this.getOwner(),
                    (this.random.nextDouble() - 0.5) * 0.5, // 随机散布
                    (this.random.nextDouble() - 0.5) * 0.5,
                    (this.random.nextDouble() - 0.5) * 0.5
            );

            fireball.setPos(this.getX(), this.getY() + 1.0, this.getZ());
            fireball.setDeltaMovement(
                    (target.getX() - this.getX() + (this.random.nextDouble() - 0.5) * 2) * 0.2,
                    (target.getY() - this.getY() + (this.random.nextDouble() - 0.5) * 2) * 0.2,
                    (target.getZ() - this.getZ() + (this.random.nextDouble() - 0.5) * 2) * 0.2
            );
            this.level().addFreshEntity(fireball);
        }

        // 对附近的其他敌对实体也造成火焰伤害
        AABB area = this.getBoundingBox().inflate(5.0);
        List<Entity> nearbyEntities = this.level().getEntities(this, area);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity nearbyLiving &&
                    entity != this.getOwner() &&
                    !entity.isAlliedTo(this.getOwner()) &&
                    entity != target) {
                nearbyLiving.hurt(this.level().damageSources().indirectMagic(this, (LivingEntity) this.getOwner()), this.getDamage() * 0.5f);
                nearbyLiving.setSecondsOnFire(4);
            }
        }
    }

    private void handleWaterDragonEffect(LivingEntity target) {
        // 对目标造成伤害
        target.hurt(this.level().damageSources().mobProjectile(this, (LivingEntity) this.getOwner()), this.getDamage() * 1.1f);

        // 检测附近水源并冰冻
        int freezeRadius = 5;
        BlockPos centerPos = this.blockPosition();

        for (int dx = -freezeRadius; dx <= freezeRadius; dx++) {
            for (int dy = -freezeRadius; dy <= freezeRadius; dy++) {
                for (int dz = -freezeRadius; dz <= freezeRadius; dz++) {
                    BlockPos checkPos = centerPos.offset(dx, dy, dz);

                    // 检查是否是水或流动的水
                    if (this.level().getBlockState(checkPos).getBlock() == Blocks.WATER ||
                            this.level().getBlockState(checkPos).getBlock() == Blocks.WATER) {
                        this.level().setBlockAndUpdate(checkPos, Blocks.ICE.defaultBlockState());
                    }
                }
            }
        }

        // 给实体施加减速效果
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));

        // 灭火
        if (target.isOnFire()) {
            target.clearFire();
        }

        // 对附近的其他实体也施加效果
        AABB area = this.getBoundingBox().inflate(4.0);
        List<Entity> nearbyEntities = this.level().getEntities(this, area);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity nearbyLiving &&
                    entity != this.getOwner() &&
                    !entity.isAlliedTo(this.getOwner())) {
                nearbyLiving.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                if (nearbyLiving.isOnFire()) {
                    nearbyLiving.clearFire();
                }
            }
        }
    }

    private void handleHurricaneDragonEffect(LivingEntity target) {
        // 对目标造成伤害
        target.hurt(this.level().damageSources().mobProjectile(this, (LivingEntity) this.getOwner()), this.getDamage());

        // 卷起附近的敌对实体（很高）
        AABB area = this.getBoundingBox().inflate(12.0);
        List<Entity> nearbyEntities = this.level().getEntities(this, area);

        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity livingTarget &&
                    entity != this.getOwner() &&
                    !entity.isAlliedTo(this.getOwner())) {

                // 强力向上卷起
                livingTarget.setDeltaMovement(
                        livingTarget.getDeltaMovement().x() * 0.2,
                        3.0 + this.random.nextDouble() * 2.0,
                        livingTarget.getDeltaMovement().z() * 0.2
                );

                // 添加悬浮效果
                livingTarget.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 2));

                // 添加缓慢降落效果
                livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 1));

                // 对玩家和怪物都添加眩晕效果
                livingTarget.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            }
        }
    }

    private void handleLandDragonEffect(LivingEntity target) {
        // 对目标造成高伤害
        target.hurt(this.level().damageSources().mobProjectile(this, (LivingEntity) this.getOwner()), this.getDamage() * 1.3f);

        // 生成8X8的石头墙把目标围起来
        int centerX = (int) target.getX();
        int centerY = (int) Math.floor(target.getY());
        int centerZ = (int) target.getZ();
        int wallSize = 4; // 8x8范围的一半

        List<BlockPos> wallBlocks = new ArrayList<>();

        // 生成四面墙
        for (int x = centerX - wallSize; x <= centerX + wallSize; x++) {
            for (int y = centerY; y <= centerY + 4; y++) {
                for (int zOffset : new int[]{-wallSize, wallSize}) {
                    BlockPos wallPos = new BlockPos(x, y, centerZ + zOffset);
                    if (this.level().isEmptyBlock(wallPos) || this.level().getBlockState(wallPos).getDestroySpeed(this.level(), wallPos) >= 0) {
                        this.level().setBlockAndUpdate(wallPos, Blocks.STONE.defaultBlockState());
                        wallBlocks.add(wallPos);
                    }
                }
            }
        }

        for (int z = centerZ - wallSize + 1; z <= centerZ + wallSize - 1; z++) {
            for (int y = centerY; y <= centerY + 4; y++) {
                for (int xOffset : new int[]{-wallSize, wallSize}) {
                    BlockPos wallPos = new BlockPos(centerX + xOffset, y, z);
                    if (this.level().isEmptyBlock(wallPos) || this.level().getBlockState(wallPos).getDestroySpeed(this.level(), wallPos) >= 0) {
                        this.level().setBlockAndUpdate(wallPos, Blocks.STONE.defaultBlockState());
                        wallBlocks.add(wallPos);
                    }
                }
            }
        }

        // 延迟爆炸效果
        this.level().getServer().execute(() -> {
            // 创建爆炸
            Explosion explosion = new Explosion(
                    this.level(),
                    (LivingEntity) this.getOwner(),
                    null,
                    null,
                    target.getX(),
                    target.getY() + 1,
                    target.getZ(),
                    4.0f,
                    false,
                    Explosion.BlockInteraction.DESTROY
            );

            // 执行爆炸
            explosion.explode();
            explosion.finalizeExplosion(true);

            // 对范围内的实体造成额外伤害和效果
            AABB explosionArea = target.getBoundingBox().inflate(6.0);
            List<Entity> explosionEntities = this.level().getEntities(target, explosionArea);

            for (Entity explosionTarget : explosionEntities) {
                if (explosionTarget instanceof LivingEntity livingExplosionTarget) {
                    double distance = livingExplosionTarget.distanceTo(target);
                    if (distance > 0) {
                        float explosionDamage = (float) (this.getDamage() * 0.7f * (1.0 - distance / 6.0));
                        if (explosionDamage > 0) {
                            livingExplosionTarget.hurt(this.level().damageSources().explosion(explosion), explosionDamage);
                        }
                    }

                    livingExplosionTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                    livingExplosionTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                }
            }
        });

        // 给目标添加减速和虚弱效果
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 180, 3));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
    }

    @Override
    protected void defineSynchedData() {
        // 定义需要同步的数据
        this.getEntityData().define(DRAGON_MAGIC_TYPE, DragonMagicType.FlameDragon.ordinal());
        this.getEntityData().define(DAMAGE, 0.0f);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        // 读取保存的数据
        if (compound.contains("DragonMagicType")) {
            this.setDragonMagicType(DragonMagicType.values()[compound.getInt("DragonMagicType")]);
        }
        if (compound.contains("Damage")) {
            this.setDamage(compound.getFloat("Damage"));
        }
        if (compound.contains("LifeTicks")) {
            this.lifeTicks = compound.getInt("LifeTicks");
        }
        if (compound.contains("HasCollided")) {
            this.hasCollided = compound.getBoolean("HasCollided");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        // 保存数据
        compound.putInt("DragonMagicType", this.getDragonMagicType().ordinal());
        compound.putFloat("Damage", this.getDamage());
        compound.putInt("LifeTicks", this.lifeTicks);
        compound.putBoolean("HasCollided", this.hasCollided);
    }

    // 动画控制器实现，根据不同元素魔龙类型播放不同动画
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "dragon_controller",
                0, this::animationPredicate));
    }

    // 动画状态谓词，参考DriveRiderEntity的实现方式
    private PlayState animationPredicate(AnimationState<WizardRiderEntity> state) {
        WizardRiderEntity entity = state.getAnimatable();

        // 根据不同的元素魔龙类型立即播放对应的动画
        switch (entity.getDragonMagicType()) {
            case FlameDragon:
                state.getController().setAnimation(FLAME_DRAGON_ANIMATION);
                break;
            case WaterDragon:
                state.getController().setAnimation(WATER_DRAGON_ANIMATION);
                break;
            case HurricaneDragon:
                state.getController().setAnimation(HURRICANE_DRAGON_ANIMATION);
                break;
            case LandDragon:
                state.getController().setAnimation(LAND_DRAGON_ANIMATION);
                break;
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}