package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.drive.DriveRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.drive.DriveRiderEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DriveEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动SpeeDemon攻击，高速移动并对敌人造成伤害
            // 1. 给予玩家极高的速度和抗性效果
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 3));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
            
            // 2. 向前方冲刺并造成伤害
            Vec3 velocity = direction.scale(3.0);
            player.setDeltaMovement(velocity);
            player.fallDistance = 0.0f;
            
            // 3. 对路径上的敌人造成伤害
            level.getServer().execute(() -> {
                try {
                    Thread.sleep(100); // 等待玩家开始移动
                    executeSpeedAttack(level, player, direction);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // 4. 生成车轮特效
            DriveRiderEffect.spawnWheelEffectByOwnerDirection(level, player, getAttackDamage());
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 为非玩家实体（如僵尸）生成Drive特效实体
            DriveRiderEntity.trySpawnEffect(level, shooter, direction, getAttackDamage());
            
            // 添加音效
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.HORSE_GALLOP, SoundSource.HOSTILE, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            
            // 给予实体速度加成效果
            shooter.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2));
        }
    }
    
    private void executeSpeedAttack(Level level, Player player, Vec3 direction) {
        // 对玩家周围的敌人造成伤害
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                player.getBoundingBox().inflate(5.0),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                // 伤害与相对速度有关
                Vec3 relativeVelocity = entity.getDeltaMovement().subtract(player.getDeltaMovement());
                float damageFactor = (float) Math.min(1.0 + relativeVelocity.length() / 5.0, 2.0);
                
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * damageFactor);
            });
    }

    @Override
    public String getRiderName() {
        return "Drive";
    }

    @Override
    public String getActivationSoundName() {
        return "SpeeDemon!";
    }

    @Override
    public float getAttackDamage() {
        return 50.0f; // Drive骑士，伤害提升
    }

    @Override
    public float getEffectRange() {
        return 15.0f;
    }
}
