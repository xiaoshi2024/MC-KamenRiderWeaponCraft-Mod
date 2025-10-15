package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class KuugaEffect extends AbstractHeiseiRiderEffect {

    // 空我的不同形态
    private enum KuugaForm {
        Mighty, 
        Dragon, 
        Pegasus, 
        Titan
    }

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Rising Mighty Kick攻击，使用不同的形态能力
            // 随机选择一个形态
            KuugaForm selectedForm = KuugaForm.values()[level.random.nextInt(KuugaForm.values().length)];
            
            switch (selectedForm) {
                case Mighty:
                    executeMightyForm(level, player, direction);
                    break;
                case Dragon:
                    executeDragonForm(level, player, direction);
                    break;
                case Pegasus:
                    executePegasusForm(level, player, direction);
                    break;
                case Titan:
                    executeTitanForm(level, player, direction);
                    break;
            }
            
            // 给予玩家全能形态相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    private void executeMightyForm(Level level, Player player, Vec3 direction) {
        // 全能形态：强大的单体攻击
        double reach = 8.0;
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(reach));
        
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                new net.minecraft.world.phys.AABB(start, end).inflate(1.5),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 1.2f);
                
                // 击退敌人
                Vec3 knockback = direction.scale(1.5);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));
            });
    }
    
    private void executeDragonForm(Level level, Player player, Vec3 direction) {
        // 青龙形态：远程水弹攻击
        for (int i = 0; i < 3; i++) {
            double spreadX = (level.random.nextDouble() - 0.5) * 0.3;
            double spreadY = (level.random.nextDouble() - 0.5) * 0.3;
            double spreadZ = (level.random.nextDouble() - 0.5) * 0.3;
            Vec3 waterDir = new Vec3(
                direction.x + spreadX,
                direction.y + spreadY,
                direction.z + spreadZ
            ).normalize();
            
            // 发射水弹
            double waterReach = 12.0;
            Vec3 waterStart = player.getEyePosition(1.0f);
            Vec3 waterEnd = waterStart.add(waterDir.scale(waterReach));
            
            // 优化：使用更高效的实体查找方式
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                    new net.minecraft.world.phys.AABB(waterStart, waterEnd).inflate(0.5),
                    entity -> entity != player) // 提前过滤掉玩家自己
                .forEach(entity -> {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.5f);
                });
        }
    }
    
    private void executePegasusForm(Level level, Player player, Vec3 direction) {
        // 天马形态：精准远程射击
        double reach = 15.0;
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(reach));
        
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                new net.minecraft.world.phys.AABB(start, end).inflate(0.5),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 0.8f);
                
                // 给予玩家夜视效果，模拟天马形态的精准视力
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0));
            });
    }
    
    private void executeTitanForm(Level level, Player player, Vec3 direction) {
        // 泰坦形态：范围攻击和高防御
        // 优化：使用更高效的实体查找方式
        level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                player.getBoundingBox().inflate(5.0),
                entity -> entity != player) // 提前过滤掉玩家自己
            .forEach(entity -> {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage() * 0.7f);
            });
        
        // 给予玩家防御增益，模拟泰坦形态的高防御
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2));
    }

    @Override
    public String getRiderName() {
        return "Kuuga";
    }

    @Override
    public String getActivationSoundName() {
        return "Rising Mighty Kick!";
    }

    @Override
    public float getAttackDamage() {
        return 45.0f; // 基础骑士 - 空我作为平成第一位骑士，伤害设置为基础值
    }

    @Override
    public float getEffectRange() {
        return 15.0f;
    }
}
