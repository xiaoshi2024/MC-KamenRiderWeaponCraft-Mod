package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kiva.KivaBatEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

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
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
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
                int otherFormIndex = level.random.nextInt(4); // 只从其他4种形态中选择
                if (otherFormIndex < 4) {
                    selectedForm = KivaForm.values()[otherFormIndex]; // 0-3对应其他4种形态
                } else {
                    selectedForm = KivaForm.KivaForm; // 安全默认值
                }
            }
            
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
            
            // 给予玩家吸血鬼相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    private void executeKivaFormAttack(Level level, Player player, Vec3 direction) {
        // 基本形态：释放黑暗能量，吸取敌人生命
        double reach = 8.0;
        net.minecraft.world.phys.HitResult result = player.pick(reach, 0.0f, false);
        
        if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage());
                
                // 吸取生命
                float healAmount = Math.min(getAttackDamage() * 0.3f, player.getMaxHealth() - player.getHealth());
                player.heal(healAmount);
            }
        }
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
            // 优化：使用更高效的实体查找方式
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                    new net.minecraft.world.phys.AABB(targetPos, targetPos).inflate(1.2),
                    entity -> entity != player) // 提前过滤掉玩家自己
                .forEach(entity -> {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.6f);
                });
        }
    }
    
    private void executeBasshaaFormAttack(Level level, Player player, Vec3 direction) {
        // 鱼人形态：水弹攻击 - 优化：减少爆炸威力
        Vec3 targetPos = player.getEyePosition(1.0f).add(direction.scale(6.0));
        level.explode(player, targetPos.x, targetPos.y, targetPos.z, 
            getAttackDamage() / 5, Level.ExplosionInteraction.MOB); // 从/4改为/5
    }
    
    private void executeDoggaFormAttack(Level level, Player player, Vec3 direction) {
        // 魔马形态：强大的音波攻击
        // 对前方扇形区域内的敌人造成伤害并击退
        Vec3 playerPos = player.getEyePosition(1.0f);
        
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                player.getBoundingBox().inflate(8.0),
                entity -> entity != player && // 提前过滤掉玩家自己
                isInFront(player, entity, direction, Math.PI / 3)) // 只攻击前方扇形区域
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 0.7f);
                
                // 击退敌人
                Vec3 knockback = entity.position().subtract(playerPos).normalize().scale(1.2);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            });
    }
    
    // 检查实体是否在玩家前方的扇形区域内
    private boolean isInFront(Player player, Entity entity, Vec3 direction, double maxAngle) {
        Vec3 toEntity = entity.position().subtract(player.getEyePosition(1.0f)).normalize();
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
        return 49.0f; // 普通骑士 - Kiva拥有多种武器形态和吸血鬼力量，伤害略高于基础值
    }

    /**
     * 执行蝙蝠群攻击技能
     * @param level 世界对象
     * @param player 玩家实体
     * @param direction 攻击方向
     */
    private void executeBatSwarmAttack(Level level, Player player, Vec3 direction) {
        // 使用KivaBatEffect类生成蝙蝠群效果
        // 生成更多蝙蝠，增强视觉效果和伤害能力
        KivaBatEffect.spawnBatSwarmByOwnerDirection(level, player, getAttackDamage() * 2.0F);
        
        // 为玩家添加短暂的夜视效果，增强使用体验
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 10, 0));
        
        // 为玩家添加短暂的速度提升
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 3, 0));
    }

    @Override
    public float getEffectRange() {
        return 9.0f;
    }
    
    /**
     * 为非玩家实体（如僵尸）执行Kiva的特殊攻击效果
     * 允许非玩家实体也能使用Kiva的各种形态技能
     */
    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 播放骑士选择音效
            HeiseiRiderEffectManager.playSelectionSound(level, shooter, getRiderName());
            
            // 播放攻击音效
            HeiseiRiderEffectManager.playAttackSound(level, shooter, getRiderName());
            
            // 与玩家相同的形态选择逻辑，60%概率触发蝙蝠群
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
                    // 执行蝙蝠群攻击，这是用户最关心的效果
                    executeBatSwarmAttack(level, shooter, direction);
                    break;
            }
            
            // 应用视觉效果
            applyVisualEffects(level, shooter, direction);
        }
    }
    
    // 非玩家实体的基本形态攻击
    private void executeKivaFormAttackForNonPlayer(Level level, LivingEntity shooter, Vec3 direction) {
        double reach = 8.0;
        
        // 为非玩家实体简化的直线攻击
        Vec3 startPos = shooter.getEyePosition(1.0f);
        Vec3 endPos = startPos.add(direction.scale(reach));
        
        // 查找攻击路径上的实体
        List<LivingEntity> targets = level.getEntitiesOfClass(
            LivingEntity.class,
            new AABB(startPos, endPos).inflate(1.0),
            entity -> entity != shooter && entity.isAlive()
        );
        
        for (net.minecraft.world.entity.LivingEntity target : targets) {
            target.hurt(level.damageSources().mobAttack(shooter), getAttackDamage());
        }
    }
    
    // 非玩家实体的狼人形态攻击
    private void executeGaruluFormAttackForNonPlayer(Level level, LivingEntity shooter, Vec3 direction) {
        for (int i = 0; i < 3; i++) {
            double angleOffset = (i - 1) * Math.PI / 6;
            Vec3 clawDir = new Vec3(
                Math.cos(angleOffset) * direction.x - Math.sin(angleOffset) * direction.z,
                direction.y,
                Math.sin(angleOffset) * direction.x + Math.cos(angleOffset) * direction.z
            ).normalize();
            
            Vec3 targetPos = shooter.getEyePosition(1.0f).add(clawDir.scale(2.0));
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                    new net.minecraft.world.phys.AABB(targetPos, targetPos).inflate(1.2),
                    entity -> entity != shooter)
                .forEach(entity -> {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().mobAttack(shooter), getAttackDamage() * 0.6f);
                });
        }
    }
    
    // 非玩家实体的鱼人形态攻击
    private void executeBasshaaFormAttackForNonPlayer(Level level, LivingEntity shooter, Vec3 direction) {
        Vec3 targetPos = shooter.getEyePosition(1.0f).add(direction.scale(6.0));
        level.explode(shooter, targetPos.x, targetPos.y, targetPos.z, 
            getAttackDamage() / 5, Level.ExplosionInteraction.MOB);
    }
    
    // 非玩家实体的魔马形态攻击
    private void executeDoggaFormAttackForNonPlayer(Level level, LivingEntity shooter, Vec3 direction) {
        Vec3 shooterPos = shooter.getEyePosition(1.0f);
        
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                shooter.getBoundingBox().inflate(8.0),
                entity -> entity != shooter && 
                isInFront(shooter, entity, direction, Math.PI / 3))
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().mobAttack(shooter), getAttackDamage() * 0.7f);
                
                // 击退敌人
                Vec3 knockback = entity.position().subtract(shooterPos).normalize().scale(1.2);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            });
    }
    
    // 检查实体是否在射手前方的扇形区域内（支持任何LivingEntity）
    private boolean isInFront(LivingEntity shooter, Entity entity, Vec3 direction, double maxAngle) {
        Vec3 toEntity = entity.position().subtract(shooter.getEyePosition(1.0f)).normalize();
        double dotProduct = toEntity.dot(direction.normalize());
        return dotProduct > Math.cos(maxAngle);
    }
    
    // 重载蝙蝠群攻击方法，支持任何LivingEntity
    private void executeBatSwarmAttack(Level level, LivingEntity shooter, Vec3 direction) {
        // 使用KivaBatEffect类生成蝙蝠群效果
        KivaBatEffect.spawnBatSwarm(level, shooter, direction, getAttackDamage() * 2.0F, 24);
    }
}
