package com.xiaoshi2022.kamenriderweaponcraft.rider.effect.impl;

import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.heisei.kiva.KivaBatEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class KivaEffect extends AbstractHeiseiRiderEffect {

    // Kiva的不同形态
    private enum KivaForm {
        KivaForm, 
        GaruluForm, 
        BasshaaForm, 
        DoggaForm,
        BatSwarm
    }

    @Override
    public void executeSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Darkness Moon Break攻击，使用不同的形态能力
            // 提高蝙蝠群技能的触发概率到60%
            KivaForm selectedForm;
            float randomValue = level.random.nextFloat();
            
            if (randomValue < 0.6) {
                // 60%概率触发蝙蝠群技能
                selectedForm = KivaForm.BatSwarm;
            } else {
                // 40%概率随机选择其他形态
                int otherFormIndex = level.random.nextInt(4);
                selectedForm = KivaForm.values()[otherFormIndex];
            }
            
            if (shooter instanceof Player player) {
                switch (selectedForm) {
                    case KivaForm:
                        executeKivaFormAttack(level, player, direction);
                        break;
                    case GaruluForm:
                        executeGaruluFormAttack(level, player, direction);
                        break;
                    case BasshaaForm:
                        executeBasshaaFormAttack(level, player, direction);
                        break;
                    case DoggaForm:
                        executeDoggaFormAttack(level, player, direction);
                        break;
                    case BatSwarm:
                        executeBatSwarmAttack(level, player, direction);
                        break;
                }
            } else {
                // 非玩家实体也使用相同的形态选择逻辑
                switch (selectedForm) {
                    case KivaForm:
                        executeKivaFormAttackForNonPlayer(level, shooter, direction);
                        break;
                    case GaruluForm:
                        executeGaruluFormAttackForNonPlayer(level, shooter, direction);
                        break;
                    case BasshaaForm:
                        executeBasshaaFormAttackForNonPlayer(level, shooter, direction);
                        break;
                    case DoggaForm:
                        executeDoggaFormAttackForNonPlayer(level, shooter, direction);
                        break;
                    case BatSwarm:
                        executeBatSwarmAttackForNonPlayer(level, shooter, direction);
                        break;
                }
            }
            
            // 给予夜视效果，模拟Kiva的蝙蝠视力
            shooter.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
        }
    }
    
    private void executeKivaFormAttack(Level level, Player player, Vec3 direction) {
        // 基本形态：释放黑暗能量，吸取敌人生命
        double reach = 8.0;
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(reach));
        
        level.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(start, end).inflate(1.0),
                entity -> entity != player)
            .forEach(entity -> {
                entity.hurt(level.damageSources().playerAttack(player), getAttackDamage());
                // 吸取生命
                float healAmount = Math.min(getAttackDamage() * 0.3f, player.getMaxHealth() - player.getHealth());
                player.heal(healAmount);
            });
    }
    
    private void executeGaruluFormAttack(Level level, Player player, Vec3 direction) {
        // 狼人形态：快速的爪击
        for (int i = 0; i < 3; i++) {
            double angleOffset = (i - 1) * Math.PI / 6;
            Vec3 clawDir = new Vec3(
                Math.cos(angleOffset) * direction.x - Math.sin(angleOffset) * direction.z,
                direction.y,
                Math.sin(angleOffset) * direction.x + Math.cos(angleOffset) * direction.z
            ).normalize();
            
            Vec3 targetPos = player.getEyePosition(1.0f).add(clawDir.scale(2.0));
            level.getEntitiesOfClass(LivingEntity.class,
                    new net.minecraft.world.phys.AABB(targetPos, targetPos).inflate(1.2),
                    entity -> entity != player)
                .forEach(entity -> {
                    entity.hurt(level.damageSources().playerAttack(player), getAttackDamage() * 0.6f);
                });
        }
    }
    
    private void executeBasshaaFormAttack(Level level, Player player, Vec3 direction) {
        // 鱼人形态：水弹攻击
        Vec3 targetPos = player.getEyePosition(1.0f).add(direction.scale(6.0));
        level.explode(player, targetPos.x, targetPos.y, targetPos.z,
            getAttackDamage() / 5, Level.ExplosionInteraction.MOB);
    }
    
    private void executeDoggaFormAttack(Level level, Player player, Vec3 direction) {
        // 魔马形态：强大的音波攻击
        Vec3 playerPos = player.getEyePosition(1.0f);
        
        level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(8.0),
                entity -> entity != player && isInFront(player, entity, direction, Math.PI / 3))
            .forEach(entity -> {
                entity.hurt(level.damageSources().playerAttack(player), getAttackDamage() * 0.7f);
                Vec3 knockback = entity.position().subtract(playerPos).normalize().scale(1.2);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            });
    }
    
    private void executeBatSwarmAttack(Level level, Player player, Vec3 direction) {
        // 使用KivaBatEffect类生成蝙蝠群效果
        KivaBatEffect.spawnBatSwarmByOwnerDirection(level, player, getAttackDamage() * 2.0F);
        
        // 为玩家添加短暂的速度提升
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0));
    }
    
    // 非玩家实体的攻击方法
    private void executeKivaFormAttackForNonPlayer(Level level, LivingEntity shooter, Vec3 direction) {
        double reach = 8.0;
        Vec3 start = shooter.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(reach));
        
        level.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(start, end).inflate(1.0),
                entity -> entity != shooter && entity.isAlive())
            .forEach(entity -> {
                entity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage());
            });
    }
    
    private void executeGaruluFormAttackForNonPlayer(Level level, LivingEntity shooter, Vec3 direction) {
        for (int i = 0; i < 3; i++) {
            double angleOffset = (i - 1) * Math.PI / 6;
            Vec3 clawDir = new Vec3(
                Math.cos(angleOffset) * direction.x - Math.sin(angleOffset) * direction.z,
                direction.y,
                Math.sin(angleOffset) * direction.x + Math.cos(angleOffset) * direction.z
            ).normalize();
            
            Vec3 targetPos = shooter.getEyePosition(1.0f).add(clawDir.scale(2.0));
            level.getEntitiesOfClass(LivingEntity.class,
                    new net.minecraft.world.phys.AABB(targetPos, targetPos).inflate(1.2),
                    entity -> entity != shooter && entity.isAlive())
                .forEach(entity -> {
                    entity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage() * 0.6f);
                });
        }
    }
    
    private void executeBasshaaFormAttackForNonPlayer(Level level, LivingEntity shooter, Vec3 direction) {
        Vec3 targetPos = shooter.getEyePosition(1.0f).add(direction.scale(6.0));
        level.explode(shooter, targetPos.x, targetPos.y, targetPos.z,
            getAttackDamage() / 5, Level.ExplosionInteraction.MOB);
    }
    
    private void executeDoggaFormAttackForNonPlayer(Level level, LivingEntity shooter, Vec3 direction) {
        Vec3 shooterPos = shooter.getEyePosition(1.0f);
        
        level.getEntitiesOfClass(LivingEntity.class,
                shooter.getBoundingBox().inflate(8.0),
                entity -> entity != shooter && entity.isAlive() && isInFront(shooter, entity, direction, Math.PI / 3))
            .forEach(entity -> {
                entity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage() * 0.7f);
                Vec3 knockback = entity.position().subtract(shooterPos).normalize().scale(1.2);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            });
    }
    
    private void executeBatSwarmAttackForNonPlayer(Level level, LivingEntity shooter, Vec3 direction) {
        // 使用KivaBatEffect类生成蝙蝠群效果
        KivaBatEffect.spawnBatSwarmByOwnerDirection(level, shooter, getAttackDamage() * 2.0F);
    }
    
    // 检查实体是否在前方的扇形区域内
    private boolean isInFront(LivingEntity shooter, Entity entity, Vec3 direction, double maxAngle) {
        Vec3 toEntity = entity.position().subtract(shooter.getEyePosition(1.0f)).normalize();
        double dotProduct = toEntity.dot(direction.normalize());
        return dotProduct > Math.cos(maxAngle);
    }

    @Override
    public String getRiderName() {
        return "Kiva";
    }

    @Override
    public String getActivationSoundName() {
        return "Darkness Moon Break!";
    }

    @Override
    public float getAttackDamage() {
        return 49.0f;
    }

    @Override
    public float getEffectRange() {
        return 9.0f;
    }
}