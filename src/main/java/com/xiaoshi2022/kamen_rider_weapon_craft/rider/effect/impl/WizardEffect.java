package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WizardEffect extends AbstractHeiseiRiderEffect {

    // Wizard的不同魔法戒指
    private enum WizardRing {
        Flame, 
        Water, 
        Hurricane, 
        Earth, 
        Infinity
    }

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Strike Wizard攻击，使用不同的魔法戒指
            // 随机选择一个魔法戒指
            WizardRing selectedRing = WizardRing.values()[level.random.nextInt(WizardRing.values().length)];
            
            switch (selectedRing) {
                case Flame:
                    executeFlameMagic(level, player, direction);
                    break;
                case Water:
                    executeWaterMagic(level, player, direction);
                    break;
                case Hurricane:
                    executeHurricaneMagic(level, player, direction);
                    break;
                case Earth:
                    executeEarthMagic(level, player, direction);
                    break;
                case Infinity:
                    executeInfinityMagic(level, player, direction);
                    break;
            }
            
            // 给予玩家魔法相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    private void executeFlameMagic(Level level, Player player, Vec3 direction) {
        // 火焰魔法：发射火球
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(15.0));
        
        // 使用正确的射线检测方法
        net.minecraft.world.phys.HitResult result = player.pick(15.0, 0.0f, false);
        
        if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 1.0f);
                ((net.minecraft.world.entity.LivingEntity) entity).setSecondsOnFire(8);
                
                // 爆炸效果
                level.explode(player, entity.getX(), entity.getY(), entity.getZ(), 
                    getEffectRange() / 2, Level.ExplosionInteraction.MOB);
            }
        }
        
        // 给予玩家火焰抗性
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
    }
    
    private void executeWaterMagic(Level level, Player player, Vec3 direction) {
        // 水魔法：模拟降雨效果，减速敌人并灭火
        // 对前方敌人造成伤害和减速效果
        for (int i = 0; i < 8; i++) {
            Vec3 waterPos = player.getEyePosition(1.0f).add(direction.scale(i + 1));
            
            // 对敌人造成伤害和减速效果
            level.getEntities(player, new net.minecraft.world.phys.AABB(waterPos, waterPos).inflate(1.5))
                .forEach(entity -> {
                    if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                        ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                            level.damageSources().playerAttack(player), getAttackDamage() * 0.3f);
                        ((net.minecraft.world.entity.LivingEntity) entity).addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                        // 灭火效果
                        if (((net.minecraft.world.entity.LivingEntity) entity).isOnFire()) {
                            ((net.minecraft.world.entity.LivingEntity) entity).clearFire();
                        }
                    }
                });
        }
        
        // 降雨粒子效果：在范围内创建水粒子
        // 此部分在客户端通过geo动画实现，保留灭火功能在服务器端
        
        // 灭火功能：熄灭范围内的火
        int rainRange = 5; // 降雨范围
        for (int x = -rainRange; x <= rainRange; x++) {
            for (int z = -rainRange; z <= rainRange; z++) {
                // 从玩家位置上方开始，向下检查
                for (int y = 3; y >= -3; y--) {
                    net.minecraft.core.BlockPos firePos = player.blockPosition().offset(x, y, z);
                    if (level.getBlockState(firePos).getBlock() == net.minecraft.world.level.block.Blocks.FIRE) {
                        level.removeBlock(firePos, false);
                    }
                }
            }
        }
    }
    
    private void executeHurricaneMagic(Level level, Player player, Vec3 direction) {
        // 飓风魔法：制造风暴和击退敌人
        // 创建一个小型风暴
        level.explode(player, player.getX(), player.getY(), player.getZ(), 
            getEffectRange() / 2, Level.ExplosionInteraction.MOB);
        
        // 对周围敌人造成伤害和击退效果
        level.getEntities(player, player.getBoundingBox().inflate(getEffectRange()))
            .forEach(entity -> {
                if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.5f);
                    
                    // 强大的击退效果
                    Vec3 knockback = entity.position().subtract(player.position()).normalize().scale(2.0);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
                }
            });
        
        // 给予玩家短暂的飞行能力
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 150, 0));
    }
    
    private void executeEarthMagic(Level level, Player player, Vec3 direction) {
        // 大地魔法：制造岩石盾牌和地震
        // 给予玩家高额防御
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2));
        
        // 制造一个小型地震
        level.explode(player, player.getX(), player.getY() - 1, player.getZ(), 
            getEffectRange(), Level.ExplosionInteraction.BLOCK);
        
        // 对周围敌人造成伤害和缓速效果
        level.getEntities(player, player.getBoundingBox().inflate(getEffectRange()))
            .forEach(entity -> {
                if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.6f);
                    ((net.minecraft.world.entity.LivingEntity) entity).addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 150, 3));
                }
            });
    }
    
    private void executeInfinityMagic(Level level, Player player, Vec3 direction) {
        // 无限魔法：释放强大的魔法能量
        // 对大范围敌人造成伤害
        level.getEntities(player, player.getBoundingBox().inflate(getEffectRange() * 1.5))
            .forEach(entity -> {
                if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.8f);
                }
            });
        
        // 给予玩家多种增益效果
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 3));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 3));
    }

    @Override
    public String getRiderName() {
        return "Wizard";
    }

    @Override
    public String getActivationSoundName() {
        return "Strike Wizard!";
    }

    @Override
    public float getAttackDamage() {
        return 51.0f; // 普通骑士 - Wizard拥有多种魔法属性和Infinity形态，伤害略高于普通骑士
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
}
