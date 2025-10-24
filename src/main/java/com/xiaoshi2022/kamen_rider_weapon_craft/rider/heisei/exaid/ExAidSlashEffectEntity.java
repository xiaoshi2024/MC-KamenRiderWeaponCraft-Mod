package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animatable.processing.AnimationTest;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class ExAidSlashEffectEntity extends Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation SLASH_ANIMATION = RawAnimation.begin().thenPlay("animation.exaid_slash_effect.slash");
    private Entity owner;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 120;
    private Entity hitEntity = null;
    private int followTicks = 0;
    private Entity targetEntity = null;

    public ExAidSlashEffectEntity(EntityType<?> entityEntityType, World world) {
        super((EntityType<ExAidSlashEffectEntity>) entityEntityType, world);
        this.setNoGravity(true);
    }

    public void setOwner(Entity owner) {
        this.owner = owner;
    }

    public ExAidSlashEffectEntity(World world, Entity owner, Vec3d position, Vec3d direction) {
        super(ModEntityTypes.EXAID_SLASH_EFFECT, world);
        this.setPosition(position);
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yRot = (float)Math.toDegrees(Math.atan2(-direction.x, direction.z));
        float xRot = (float)Math.toDegrees(Math.atan2(direction.y, horizontalDistance));
        this.setYaw(yRot);
        this.setPitch(xRot);
        this.owner = owner;
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();

        // 检查特效是否靠近释放者，如果是则立即移除
        checkNearbyPlayers();

        lifetime++;
        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        // 如果有明确的目标实体，并且还没有击中任何实体，优先跟踪目标实体
        // 确保不会跟踪到释放者
        if (targetEntity != null && hitEntity == null && !targetEntity.isRemoved() && targetEntity != owner) {
            // 跟踪目标实体，即使没有直接碰撞
            double randomOffsetX = (this.getWorld().random.nextDouble() - 0.5) * 1.0;
            double randomOffsetY = (this.getWorld().random.nextDouble() - 0.5) * 0.8;
            double randomOffsetZ = (this.getWorld().random.nextDouble() - 0.5) * 1.0;

            this.setPosition(targetEntity.getX() + randomOffsetX,
                    targetEntity.getY() + targetEntity.getHeight() * 0.5 + randomOffsetY,
                    targetEntity.getZ() + randomOffsetZ);
            followTicks++;

            // 在跟踪期间持续生成粒子效果
            if (followTicks % 3 == 0) {
                spawnHitParticles(targetEntity);
            }

            // 跟随一段时间后停止跟随，让特效自由漂浮
            if (followTicks > 60) {
                followTicks = 0;
                // 设置随机移动，但避免移动回释放者方向
                Vec3d motionAwayFromOwner = getMotionAwayFromOwner();
                this.setVelocity(motionAwayFromOwner);
            }
        } else if (hitEntity != null && !hitEntity.isRemoved()) {
            // 设置特效实体的位置为被击中实体的位置
            // 确保不会跟踪到释放者
            if (hitEntity != owner) {
                this.setPosition(hitEntity.getX(), hitEntity.getY() + hitEntity.getHeight() * 0.5, hitEntity.getZ());
            } else {
                // 如果hitEntity是释放者，立即移除特效
                this.discard();
                return;
            }
            followTicks++;

            // 在跟随期间持续生成粒子效果
            if (followTicks % 3 == 0) {
                spawnHitParticles(hitEntity);
            }

            // 跟随一段时间后停止跟随
            if (followTicks > 30) {
                hitEntity = null;
                followTicks = 0;
                // 设置随机移动，但避免移动回释放者方向
                Vec3d motionAwayFromOwner = getMotionAwayFromOwner();
                this.setVelocity(motionAwayFromOwner);
            }
        } else {
            // 保持初始方向移动，减少随机性，防止特效返回
            Vec3d currentMotion = getVelocity();
            // 确保特效有足够的速度
            if (currentMotion.length() < 0.5) {
                // 如果速度太小，使用远离释放者的方向
                Vec3d motionAwayFromOwner = getMotionAwayFromOwner();
                this.setVelocity(motionAwayFromOwner);
            } else {
                // 轻微调整方向，保持移动感但不会轻易返回
                this.setVelocity(currentMotion.normalize().multiply(0.8).add(
                        (this.getWorld().random.nextDouble() - 0.5) * 0.1,
                        0.02 + this.getWorld().random.nextDouble() * 0.05,
                        (this.getWorld().random.nextDouble() - 0.5) * 0.1
                ));
            }

            // 检查是否击中实体
            checkEntityCollision();
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readCustomData(ReadView view) {

    }

    @Override
    protected void writeCustomData(WriteView view) {

    }

    // 获取远离释放者的运动向量
    private Vec3d getMotionAwayFromOwner() {
        if (owner != null && !owner.isRemoved()) {
            // 计算从释放者到特效的向量，确保特效远离释放者
            Vec3d ownerToEffect = this.getPos().subtract(owner.getPos());
            if (ownerToEffect.length() > 0.1) {
                // 远离释放者的方向，增加一些随机性
                return ownerToEffect.normalize().multiply(0.5).add(
                        (this.getWorld().random.nextDouble() - 0.5) * 0.3,
                        0.1 + this.getWorld().random.nextDouble() * 0.3,
                        (this.getWorld().random.nextDouble() - 0.5) * 0.3
                );
            }
        }
        // 如果无法计算远离释放者的方向，使用随机方向
        return new Vec3d(
                (this.getWorld().random.nextDouble() - 0.5) * 0.5,
                0.1 + this.getWorld().random.nextDouble() * 0.3,
                (this.getWorld().random.nextDouble() - 0.5) * 0.5
        );
    }

    // 设置特效应该跟踪的目标实体
    public void setTargetEntity(Entity targetEntity) {
        // 确保不会将释放者设置为目标
        if (targetEntity != null && targetEntity != owner) {
            this.targetEntity = targetEntity;
        } else {
            // 如果尝试设置释放者为目标，则不设置任何目标
            this.targetEntity = null;
        }
    }

    // 检查是否击中实体
    private void checkEntityCollision() {
        if (hitEntity == null) {
            // 确保特效已经远离释放者足够距离才允许检测实体
            if (owner != null && this.distanceTo(owner) < 3.0D) {
                return; // 太靠近释放者，不进行实体检测
            }

            // 扩大检测范围，确保能够检测到实体
            List<Entity> nearbyEntities = this.getWorld().getOtherEntities(this, this.getBoundingBox().expand(2.0D),
                    entity -> entity instanceof LivingEntity &&
                            entity != owner &&  // 确保不是释放者
                            !entity.isSpectator() &&
                            entity.isAlive());

            for (Entity entity : nearbyEntities) {
                // 再次确认不是释放者
                if (entity == owner) {
                    continue;
                }

                // 记录被击中的实体，以便跟随它移动
                hitEntity = entity;
                followTicks = 0;

                // 在客户端生成粒子效果
                if (this.getWorld().isClient()) {
                    spawnHitParticles(entity);
                }

                // 标记为击中，停止自主移动
                this.setVelocity(0, 0, 0);

                // 在服务器端对击中的实体造成伤害
                if (!this.getWorld().isClient() && owner != null) { // 确保owner不为null
                    if (entity instanceof LivingEntity livingEntity) {
                        // 确保不会对释放者造成伤害或效果，但允许对其他玩家（包括敌对玩家）造成伤害
                        if (livingEntity != owner) {
                            try {
                                // 创建一个特殊的伤害源，代表Ex-Aid的技能伤害

                                // 立即造成一次基础伤害
                                livingEntity.damage(
                                        (ServerWorld)getWorld(), getWorld().getDamageSources().playerAttack((PlayerEntity)owner), 5.0F);

                                // 添加凋零效果作为持续伤害的实现
                                livingEntity.addStatusEffect(new StatusEffectInstance(
                                        StatusEffects.WITHER,
                                        40, 1, false, false
                                ));
                            } catch (ClassCastException e) {
                                // 防止owner不是Player类型时出现异常
                                // 如果owner不是PlayerEntity，使用魔法伤害源
                                livingEntity.damage(
                                        (ServerWorld)getWorld(), getWorld().getDamageSources().magic(), 5.0F);
                            }
                        }
                    }
                }

                break;
            }
        }
    }

    // 在击中实体时生成粒子效果
    private void spawnHitParticles(Entity entity) {
        if (entity == null || entity.isRemoved()) return;

        // 生成粒子效果
        if (this.getWorld().isClient()) {
            // 客户端粒子效果生成逻辑
            for (int i = 0; i < 5; i++) {
                double offsetX = (this.getWorld().random.nextDouble() - 0.5) * 0.5;
                double offsetY = (this.getWorld().random.nextDouble() - 0.5) * 0.5;
                double offsetZ = (this.getWorld().random.nextDouble() - 0.5) * 0.5;

                // 这里可以添加具体的粒子生成代码
                // getWorld().addParticle(ParticleTypes.CRIT,
                //     entity.getX() + offsetX,
                //     entity.getY() + entity.getHeight() * 0.5 + offsetY,
                //     entity.getZ() + offsetZ,
                //     0, 0, 0);
            }
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {

    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.hitEntity = null;
        this.owner = null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("controller", 0, this::animationPredicate));
    }

    private PlayState animationPredicate(AnimationTest<GeoAnimatable> geoAnimatableAnimationTest) {
        geoAnimatableAnimationTest.controller().setAnimation(SLASH_ANIMATION);
        return PlayState.CONTINUE;
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    // 静态方法用于生成特效 - 修复版本
    public static void spawnEffect(World world, Entity owner, Vec3d direction) {
        // 只在服务器端生成实体，确保所有客户端都能通过数据包同步看到
        if (!world.isClient()) {
            // 增加特效起始距离到2.5单位，避免太靠近释放者
            Vec3d startPos = owner.getEyePos().add(direction.multiply(2.5));
            ExAidSlashEffectEntity effect = new ExAidSlashEffectEntity(world, owner, startPos, direction);

            // 设置初始速度，让特效直接飞向指定方向而不是漂浮
            effect.setVelocity(direction.multiply(1.2));

            world.spawnEntity(effect);
        }
    }

    // 新增：直接生成在目标实体上的特效
    public static void spawnEffectOnTarget(World world, Entity owner, Entity target) {
        if (!world.isClient() && target != null) {
            // 在目标实体周围生成特效
            double x = target.getX();
            double y = target.getY() + target.getHeight() * 0.5;
            double z = target.getZ();

            // 创建随机方向
            Vec3d direction = new Vec3d(
                    world.random.nextDouble() - 0.5,
                    world.random.nextDouble() - 0.5,
                    world.random.nextDouble() - 0.5
            ).normalize();

            ExAidSlashEffectEntity effect = new ExAidSlashEffectEntity(world, owner, new Vec3d(x, y, z), direction);
            effect.setTargetEntity(target); // 设置目标实体进行跟踪
            world.spawnEntity(effect);
        }
    }

    /**
     * 检查周围是否有释放者，如果特效靠近释放者则消除，避免特效跑到释放者身上
     */
    private void checkNearbyPlayers() {
        // 只检查释放者是否在范围内
        if (owner != null && owner.isAlive()) {
            // 计算特效与释放者的距离
            double distance = this.distanceTo(owner);

            // 增加距离检测范围到2格，更早移除靠近释放者的特效
            if (distance < 2.0D) {
                this.discard();
                return;
            }
        }
    }
}