package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.builds;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Kamen Rider Build 实体类 - Fabric 1.21.8 版本
 * 气泡兔坦形态的公式踢特效实体
 */
public class BuildRiderEntity extends Entity implements GeoEntity {
    // 动画常量定义
    private static final RawAnimation MOVE_ANIMATION = RawAnimation.begin().thenPlay("move");
    private static final RawAnimation HIT_ANIMATION = RawAnimation.begin().thenPlay("attack.hit");

    // 跟踪数据
    private static final TrackedData<Float> DAMAGE = DataTracker.registerData(BuildRiderEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> LIFETIME = DataTracker.registerData(BuildRiderEntity.class, TrackedDataHandlerRegistry.INTEGER);

    // 动画实例缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final int MAX_LIFETIME = 100; // 约5秒
    private LivingEntity owner;

    // 主要构造函数
    public BuildRiderEntity(EntityType<?> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
    }

    // 便捷构造函数
    public BuildRiderEntity(World world, LivingEntity owner, Vec3d direction, float damage) {
        this(ModEntityTypes.BUILD_RIDER_EFFECT, world);
        this.owner = owner;
        this.setPosition(owner.getEyePos().add(direction.multiply(1.0)));
        this.setVelocity(direction);
        this.getDataTracker().set(DAMAGE, damage);
        this.getDataTracker().set(LIFETIME, 0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(DAMAGE, 10.0f);
        builder.add(LIFETIME, 0);
    }


    public float getDamage() {
        return this.getDataTracker().get(DAMAGE);
    }

    public int getLifetime() {
        return this.getDataTracker().get(LIFETIME);
    }

    @Override
    public void tick() {
        super.tick();

        // 更新生命周期
        int currentLifetime = getLifetime() + 1;
        this.getDataTracker().set(LIFETIME, currentLifetime);

        // 如果超过最大存活时间，移除实体
        if (currentLifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        // 移动实体
        Vec3d velocity = this.getVelocity();
        Vec3d pos = this.getPos();
        this.setPosition(pos.add(velocity));

        // 检查碰撞
        if (!this.getWorld().isClient()) {
            HitResult hitResult = ProjectileUtil.getCollision(this, entity ->
                    !entity.isSpectator() && entity.isAlive() && entity != this.owner
            );

            if (hitResult.getType() != HitResult.Type.MISS) {
                onCollision(hitResult);
            }
        }

        // 更新速度（添加阻力）
        this.setVelocity(velocity.multiply(0.98));
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

    protected void onCollision(HitResult hitResult) {
        if (!this.getWorld().isClient()) {
            // 对碰撞到的实体造成伤害
            if (hitResult instanceof EntityHitResult entityHit) {
                if (entityHit.getEntity() instanceof LivingEntity living && living != this.owner) {
                    try {
                        DamageSource damageSource;
                        if (this.owner != null) {
                            damageSource = this.getDamageSources().mobProjectile(this, this.owner);
                        } else {
                            damageSource = this.getDamageSources().magic();
                        }
                        // 按照Minecraft 1.21.8的正确方法签名调用
                        if (this.getWorld() instanceof ServerWorld) {
                            living.damage((ServerWorld) this.getWorld(), damageSource, getDamage());
                        }
                    } catch (Exception e) {
                        System.err.println("Build实体伤害处理失败: " + e.getMessage());
                    }
                }
            }

            // 创建爆炸效果
            explode();
        }
    }

    private void explode() {
        // 创建爆炸粒子效果
        if (this.getWorld() instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            for (int i = 0; i < 10; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetY = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;

                // 使用正确的服务器端粒子生成API
                serverWorld.spawnParticles(
                        ParticleTypes.EXPLOSION,
                        this.getX(), this.getY(), this.getZ(),
                        1, offsetX * 0.1, offsetY * 0.1, offsetZ * 0.1, 0.0
                );
            }
        }
        // 确保实体被移除
        this.discard();
    }

    // ========== GeckoLib 动画相关 ==========

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("main", 5, state -> {
            if (this.getLifetime() > MAX_LIFETIME - 20) {
                // 即将消失时播放击中动画
                state.setAnimation(HIT_ANIMATION);
            } else {
                // 默认播放移动动画
                state.setAnimation(MOVE_ANIMATION);
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}