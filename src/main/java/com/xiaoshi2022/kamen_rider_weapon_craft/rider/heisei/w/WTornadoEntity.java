package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.w;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WTornadoEntity extends net.minecraft.world.entity.Entity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation SPIN_ANIMATION = RawAnimation.begin().thenLoop("spin");
    private Vec3 targetDirection = Vec3.ZERO; // 初始化默认值，避免null
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 60; // 3秒（60tick）

    public WTornadoEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public WTornadoEntity(Level level) {
        super(ModEntityTypes.W_TORNADO.get(), level);
    }

    public void setDirection(Vec3 direction) {
        this.targetDirection = direction != null ? direction.normalize() : Vec3.ZERO;
    }

    @Override
    public void tick() {
        super.tick();
        
        // 更新生命周期
        lifetime++;
        if (lifetime >= MAX_LIFETIME) {
            // 龙卷风消失前，确保所有被卷起的实体恢复正常重力
            resetAffectedEntitiesGravity();
            this.remove(RemovalReason.DISCARDED);
            return;
        }

        // 根据目标方向移动
        if (targetDirection != null) {
            double speed = 0.5;
            setDeltaMovement(targetDirection.scale(speed));
        }

        // 对范围内的敌人造成伤害并卷起
        if (!level().isClientSide) {
            double range = 2.0;
            level().getEntitiesOfClass(LivingEntity.class, 
                    getBoundingBox().inflate(range),
                    entity -> !(entity instanceof Player))
                .forEach(entity -> {
                    // 造成伤害并给予缓速效果
                    entity.hurt(level().damageSources().generic(), 5.0f);
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 
                            40, 1));
                    
                    // 卷起敌人：提升到空中并随龙卷风移动
                    // 设置实体在空中的位置，稍微高于龙卷风中心
                    Vec3 moveDirection = (targetDirection != null && targetDirection.lengthSqr() > 0.01) ? targetDirection : Vec3.ZERO;
                    entity.setDeltaMovement(
                        moveDirection.x * 0.5, // 水平方向随龙卷风移动
                        0.4, // 向上的力
                        moveDirection.z * 0.5
                    );
                    entity.fallDistance = 0.0f; // 防止坠落伤害
                    entity.setNoGravity(true); // 暂时禁用重力
                    
                    // 给实体一个短暂的标记，用于后续处理
                    entity.setAirSupply(entity.getMaxAirSupply()); // 重置空气值，作为标记使用
                });
                
            // 检查并恢复离开龙卷风范围的实体重力
            resetOutOfRangeEntitiesGravity(range);
        } else {
            // 客户端：为被卷起的实体添加视觉效果
            level().getEntitiesOfClass(LivingEntity.class, 
                    getBoundingBox().inflate(2.0),
                    entity -> !(entity instanceof Player))
                .forEach(entity -> {
                    // 粒子效果等视觉反馈可以在这里添加
                    // 添加一些风元素粒子效果
                    level().addParticle(
                        net.minecraft.core.particles.ParticleTypes.CLOUD,
                        entity.getX() + (random.nextDouble() - 0.5) * 2.0,
                        entity.getY() + random.nextDouble() * 2.0,
                        entity.getZ() + (random.nextDouble() - 0.5) * 2.0,
                        (random.nextDouble() - 0.5) * 0.5,
                        random.nextDouble() * 0.5,
                        (random.nextDouble() - 0.5) * 0.5
                    );
                });
        }

        // 应用物理运动
        move(MoverType.SELF, getDeltaMovement());
    }

    @Override
    protected void defineSynchedData() {
        // 无需同步特殊数据
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compoundTag) {
        // 不需要保存数据
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compoundTag) {
        // 不需要保存数据
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "spin_controller", 0, state -> 
                state.setAndContinue(SPIN_ANIMATION)));
    }

    // 静态方法用于生成龙卷风 - 修改为支持所有LivingEntity
    public static void trySpawnTornado(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) { // 只在服务器端生成实体
            WTornadoEntity tornado = new WTornadoEntity(level);
            tornado.setDirection(direction);
            // 使用getEyePosition方法，适用于所有LivingEntity
            tornado.setPos(shooter.getEyePosition().add(direction.scale(1.0)));
            level.addFreshEntity(tornado);
        }
    }
    
    // 保持向后兼容性的重载方法
    public static void trySpawnTornado(Level level, Player player, Vec3 direction) {
        trySpawnTornado(level, (LivingEntity) player, direction);
    }
    
    /**
     * 重置离开龙卷风范围的实体重力
     */
    private void resetOutOfRangeEntitiesGravity(double range) {
        // 获取一定范围内但不在龙卷风作用范围内的实体
        double searchRange = range * 3.0; // 更大的搜索范围
        level().getEntitiesOfClass(LivingEntity.class, 
                getBoundingBox().inflate(searchRange),
                entity -> !(entity instanceof Player) && entity.isNoGravity() && 
                !getBoundingBox().inflate(range).contains(entity.position()))
            .forEach(entity -> {
                // 检查是否是最近被龙卷风卷起的实体（通过空气值标记）
                if (entity.getAirSupply() > entity.getMaxAirSupply() * 0.8) {
                    // 让实体逐渐恢复重力，而不是立即掉落
                    entity.setNoGravity(false);
                    // 给予一个小小的上浮力，让下落更自然
                    Vec3 motion = entity.getDeltaMovement();
                    entity.setDeltaMovement(motion.x, 0.1, motion.z);
                }
            });
    }
    
    /**
     * 龙卷风消失时，重置所有受影响实体的重力
     */
    private void resetAffectedEntitiesGravity() {
        double searchRange = 5.0;
        level().getEntitiesOfClass(LivingEntity.class, 
                getBoundingBox().inflate(searchRange),
                entity -> !(entity instanceof Player) && entity.isNoGravity())
            .forEach(entity -> {
                entity.setNoGravity(false);
                // 给予一个小小的上浮力，让下落更自然
                Vec3 motion = entity.getDeltaMovement();
                entity.setDeltaMovement(motion.x, 0.1, motion.z);
            });
    }
}