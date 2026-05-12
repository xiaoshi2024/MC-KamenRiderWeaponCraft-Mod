package com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.kuuga;

import com.xiaoshi2022.kamenriderweaponcraft.register.EntityRegister;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KuugaRiderEntity extends Fireball implements GeoEntity {
    
    private float attackDamage = 45.0f;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation FLY_ANIMATION = RawAnimation.begin().thenLoop("idle");
    
    public KuugaRiderEntity(EntityType<? extends Fireball> type, Level level) {
        super(type, level);
    }
    
    public KuugaRiderEntity(Level level, double x, double y, double z, double dx, double dy, double dz) {
        super(EntityRegister.KUUGA_RIDER.get(), level);
        this.setPos(x, y, z);
        this.setDeltaMovement(dx, dy, dz);
    }
    
    public void setAttackDamage(float damage) {
        this.attackDamage = damage;
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (this.level().isClientSide) {
            Vec3 position = this.position();
            for (int i = 0; i < 3; i++) {
                double dx = (this.random.nextDouble() - 0.5) * 0.3;
                double dy = (this.random.nextDouble() - 0.5) * 0.3;
                double dz = (this.random.nextDouble() - 0.5) * 0.3;
                this.level().addParticle(ParticleTypes.FLAME, position.x, position.y, position.z, dx, dy, dz);
                this.level().addParticle(ParticleTypes.ENCHANTED_HIT, position.x, position.y, position.z, dx, dy, dz);
            }
        }
    }
    
    @Override
    protected void onHit(net.minecraft.world.phys.HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide) {
            executeMightyBlast();
            this.discard();
        }
    }
    
    private void executeMightyBlast() {
        Vec3 position = this.position();
        
        this.level().explode(
                this,
                this.damageSources().fireball(this, this.getOwner()),
                null,
                position.x, position.y, position.z,
                3.0F,
                false,
                Level.ExplosionInteraction.NONE
        );
        
        this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(6.0),
                entity -> {
                    boolean basicCondition = entity != this.getOwner() && entity.isAlive();
                    
                    if (this.getOwner() instanceof LivingEntity && !(this.getOwner() instanceof Player)) {
                        return basicCondition && !(entity instanceof Player);
                    }
                    
                    return basicCondition;
                })
            .forEach(entity -> {
                double distance = entity.distanceToSqr(this);
                float damageFactor = 1.0F - (float)Math.min(distance, 16.0) / 16.0F;
                
                entity.hurt(this.damageSources().fireball(this, this.getOwner()), attackDamage * damageFactor * 1.5F);
                
                Vec3 knockback = entity.position().subtract(position).normalize().scale(2.0);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            });
        
        this.level().playSound(null, position.x, position.y, position.z,
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,
                net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.8F + level().random.nextFloat() * 0.4F);
        
        for (int i = 0; i < 100; i++) {
            double dx = (this.random.nextDouble() - 0.5) * 3.0;
            double dy = (this.random.nextDouble() - 0.5) * 3.0;
            double dz = (this.random.nextDouble() - 0.5) * 3.0;
            
            if (this.random.nextBoolean()) {
                this.level().addParticle(ParticleTypes.FLAME, position.x, position.y + 1.0, position.z, dx * 0.5, dy * 0.5, dz * 0.5);
            } else if (this.random.nextBoolean()) {
                this.level().addParticle(ParticleTypes.ENCHANTED_HIT, position.x, position.y + 1.0, position.z, dx * 0.5, dy * 0.5, dz * 0.5);
            } else {
                this.level().addParticle(ParticleTypes.CRIT, position.x, position.y + 1.0, position.z, dx * 0.5, dy * 0.5, dz * 0.5);
            }
        }
    }
    
    public static void trySpawnEffect(Level level, LivingEntity shooter, Vec3 direction, float damage) {
        if (!level.isClientSide) {
            KuugaRiderEntity kuugaEntity = new KuugaRiderEntity(
                    level,
                    shooter.getX(),
                    shooter.getY() + shooter.getEyeHeight(),
                    shooter.getZ(),
                    direction.x * 2.0,
                    direction.y * 2.0,
                    direction.z * 2.0
            );
            
            kuugaEntity.setOwner(shooter);
            kuugaEntity.setAttackDamage(damage);
            
            level.addFreshEntity(kuugaEntity);
            
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(),
                    net.minecraft.sounds.SoundEvents.IRON_GOLEM_HURT,
                    net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
        }
    }
    
    @Override
    protected boolean shouldBurn() {
        return false;
    }
    
    @Override
    public boolean isPickable() {
        return false;
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