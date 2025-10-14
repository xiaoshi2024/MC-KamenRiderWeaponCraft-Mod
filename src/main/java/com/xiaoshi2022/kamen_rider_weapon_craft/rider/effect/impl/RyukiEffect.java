package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RyukiEffect implements HeiseiRiderEffect {

    // 龙骑的不同卡片
    private enum RyukiCard {
        SwordVent, 
        ShootVent, 
        GuardVent, 
        StrikeVent, 
        FinalVent
    }

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Dragon Rider Kick攻击，使用不同的卡片能力
            // 随机选择一个卡片能力
            RyukiCard selectedCard = RyukiCard.values()[level.random.nextInt(RyukiCard.values().length)];
            
            switch (selectedCard) {
                case SwordVent:
                    executeSwordVent(level, player, direction);
                    break;
                case ShootVent:
                    executeShootVent(level, player, direction);
                    break;
                case GuardVent:
                    executeGuardVent(level, player, direction);
                    break;
                case StrikeVent:
                    executeStrikeVent(level, player, direction);
                    break;
                case FinalVent:
                    executeFinalVent(level, player, direction);
                    break;
            }
            
            // 给予玩家镜世界相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    private void executeSwordVent(Level level, Player player, Vec3 direction) {
        // 剑降临：使用龙剑攻击
        double reach = 8.0;
        net.minecraft.world.phys.HitResult result = player.pick(reach, 0.0f, false);
        
        if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage());
                // 给予敌人缓慢效果
                ((net.minecraft.world.entity.LivingEntity) entity).addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            }
        }
    }
    
    private void executeShootVent(Level level, Player player, Vec3 direction) {
        // 射击降临：发射龙弹
        for (int i = 0; i < 5; i++) {
            // 创建5个龙弹
            double angleOffset = (i - 2) * Math.PI / 12;
            Vec3 bulletDir = new Vec3(
                Math.cos(angleOffset) * direction.x - Math.sin(angleOffset) * direction.z,
                direction.y + (level.random.nextDouble() - 0.5) * 0.3,
                Math.sin(angleOffset) * direction.x + Math.cos(angleOffset) * direction.z
            ).normalize();
            
            // 发射龙弹
            net.minecraft.world.phys.HitResult result = player.pick(15.0, 0.0f, false);
            
            if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
                Entity entity = entityHitResult.getEntity();
                if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.3f);
                    ((net.minecraft.world.entity.LivingEntity) entity).setSecondsOnFire(3);
                }
            }
        }
    }
    
    private void executeGuardVent(Level level, Player player, Vec3 direction) {
        // 防御降临：使用龙盾防御 - 优化：移除Thread.sleep，使用更高效的实现
        // 给予玩家高额防御
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 2));
        
        // 立即对周围敌人造成反弹伤害
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                player.getBoundingBox().inflate(getEffectRange()),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                // 使用荆棘伤害来源模拟反弹效果
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().thorns(player), getAttackDamage() * 0.3f); // 略微提高伤害以弥补没有延迟的效果
            });
    }
    
    private void executeStrikeVent(Level level, Player player, Vec3 direction) {
        // 突击降临：使用龙爪攻击
        level.getEntities(player, player.getBoundingBox().inflate(getEffectRange()))
            .forEach(entity -> {
                if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.6f);
                    // 给予玩家短暂的速度加成
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1));
                }
            });
    }
    
    private void executeFinalVent(Level level, Player player, Vec3 direction) {
        // 最终降临：召唤契约兽无双龙，发动Dragon Rider Kick
        // 创建一个范围伤害
        double reach = 10.0;
        Vec3 targetPos = player.getEyePosition(1.0f).add(direction.scale(reach));
        
        // 制造爆炸效果
        level.explode(player, targetPos.x, targetPos.y, targetPos.z, 
            getEffectRange() / 2, Level.ExplosionInteraction.MOB);
        
        // 对范围内敌人造成高额伤害
        level.getEntities(player, new net.minecraft.world.phys.AABB(targetPos, targetPos).inflate(4.0))
            .forEach(entity -> {
                if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 1.5f);
                    // 强大的击退效果
                    Vec3 knockback = direction.scale(2.0);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
                }
            });
        
        // 给予玩家临时无敌效果
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 5));
    }

    @Override
    public String getRiderName() {
        return "Ryuki";
    }

    @Override
    public String getActivationSoundName() {
        return "Dragon Rider Kick!";
    }

    @Override
    public float getAttackDamage() {
        return 47.0f; // 普通骑士 - Ryuki是平成第三位骑士，使用卡片系统和契约兽，伤害略高于基础值
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
}
