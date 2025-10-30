package com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kuuga;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import static net.minecraft.world.level.Level.ExplosionInteraction.NONE;

public class KuugaRiderEntity extends Fireball implements GeoEntity {
    
    private float attackDamage = 45.0f;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenLoop("idle");
    
    public KuugaRiderEntity(EntityType<? extends Fireball> type, Level level) {
        super(type, level);
    }
    
    public KuugaRiderEntity(Level level, double x, double y, double z, double dx, double dy, double dz) {
        super(ModEntityTypes.KUUGA_RIDER.get(), x, y, z, dx, dy, dz, level);
    }
    
    public void setAttackDamage(float damage) {
        this.attackDamage = damage;
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // 添加飞行轨迹粒子效果
        if (this.level().isClientSide) {
            Vec3 position = this.position();
            for (int i = 0; i < 3; i++) {
                double dx = (this.random.nextDouble() - 0.5) * 0.3;
                double dy = (this.random.nextDouble() - 0.5) * 0.3;
                double dz = (this.random.nextDouble() - 0.5) * 0.3;
                this.level().addParticle(
                        ParticleTypes.FLAME,
                        position.x, position.y, position.z,
                        dx, dy, dz
                );
                this.level().addParticle(
                        ParticleTypes.ENCHANTED_HIT,
                        position.x, position.y, position.z,
                        dx, dy, dz
                );
            }
        }
    }
    
    @Override
    protected void onHit(net.minecraft.world.phys.HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide) {
            // 执行全能形态爆炸攻击
            executeMightyBlast();
            
            // 销毁实体
            this.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
    }
    
    private void executeMightyBlast() {
        // 全能形态爆炸攻击效果
        Vec3 position = this.position();
        
        // 创建爆炸效果（不会破坏方块）
        this.level().explode(
                this, // 爆炸源
                this.damageSources().fireball(this, this.getOwner()), // 伤害来源
                null, // 伤害修饰器
                position.x, position.y, position.z, // 爆炸位置
                3.0F, // 爆炸半径
                false, // 是否破坏方块
                NONE // 方块交互方式，使用NONE确保在/gamerule mobGriefing false时不会破坏方块
        );
        
        // 对爆炸范围内的敌人造成额外伤害
        this.level().getEntitiesOfClass(LivingEntity.class, 
                this.getBoundingBox().inflate(6.0),
                entity -> {
                    // 基本条件：不是拥有者且存活
                    boolean basicCondition = entity != this.getOwner() && entity.isAlive();
                    
                    // 如果拥有者是非玩家实体（如僵尸），则不应该伤害玩家
                    if (this.getOwner() instanceof LivingEntity && !(this.getOwner() instanceof Player)) {
                        // 非玩家使用时，不伤害玩家
                        return basicCondition && !(entity instanceof Player);
                    }
                    
                    // 玩家使用时，正常伤害所有敌人
                    return basicCondition;
                })
            .forEach(entity -> {
                // 计算伤害衰减（距离越远伤害越低）
                double distance = entity.distanceToSqr(this);
                float damageFactor = 1.0F - (float)Math.min(distance, 16.0) / 16.0F;
                
                entity.hurt(this.damageSources().fireball(this, this.getOwner()), 
                        attackDamage * damageFactor * 1.5F);
                
                // 添加击退效果
                Vec3 knockback = entity.position().subtract(position).normalize().scale(2.0);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            });
        
        // 播放爆炸音效
        this.level().playSound(null, position.x, position.y, position.z,
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.8F + level().random.nextFloat() * 0.4F);
        
        // 添加爆炸粒子效果
        for (int i = 0; i < 100; i++) {
            double dx = (this.random.nextDouble() - 0.5) * 3.0;
            double dy = (this.random.nextDouble() - 0.5) * 3.0;
            double dz = (this.random.nextDouble() - 0.5) * 3.0;
            
            // 混合使用多种粒子效果
            if (this.random.nextBoolean()) {
                this.level().addParticle(
                        ParticleTypes.FLAME,
                        position.x, position.y + 1.0, position.z,
                        dx * 0.5, dy * 0.5, dz * 0.5
                );
            } else if (this.random.nextBoolean()) {
                this.level().addParticle(
                        ParticleTypes.ENCHANTED_HIT,
                        position.x, position.y + 1.0, position.z,
                        dx * 0.5, dy * 0.5, dz * 0.5
                );
            } else {
                this.level().addParticle(
                        ParticleTypes.CRIT,
                        position.x, position.y + 1.0, position.z,
                        dx * 0.5, dy * 0.5, dz * 0.5
                );
            }
        }
    }
    
    // 尝试生成特效实体的静态方法
    public static void trySpawnEffect(Level level, LivingEntity shooter, Vec3 direction, float damage) {
        if (!level.isClientSide) {
            // 创建Kuuga特效实体
            KuugaRiderEntity kuugaEntity = new KuugaRiderEntity(
                    level,
                    shooter.getX(),
                    shooter.getY() + shooter.getEyeHeight(),
                    shooter.getZ(),
                    direction.x * 2.0,  // 增加速度使实体快速扑向目标
                    direction.y * 2.0,
                    direction.z * 2.0
            );
            
            // 设置拥有者
            kuugaEntity.setOwner(shooter);
            
            // 设置伤害
            kuugaEntity.setAttackDamage(damage);
            
            // 添加到世界
            level.addFreshEntity(kuugaEntity);
            
            // 播放音效
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    net.minecraft.sounds.SoundEvents.IRON_GOLEM_HURT,
                    net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
        }
    }
    
    @Override
    protected boolean shouldBurn() {
        return false; // 不燃烧
    }
    
    @Override
    public boolean isPickable() {
        return false; // 不可被玩家攻击
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "fly_controller", 0, state -> 
                state.setAndContinue(FLY_ANIMATION)));
    }
}