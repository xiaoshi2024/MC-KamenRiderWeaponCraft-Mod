package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class KivaEffect implements HeiseiRiderEffect {

    // Kiva的不同形态
    private enum KivaForm {
        KivaForm, 
        GaruluForm, 
        BasshaaForm, 
        DoggaForm
    }

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Darkness Moon Break攻击，使用不同的形态能力
            // 随机选择一个形态
            KivaForm selectedForm = KivaForm.values()[level.random.nextInt(KivaForm.values().length)];
            
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

    @Override
    public float getEffectRange() {
        return 9.0f;
    }
}
