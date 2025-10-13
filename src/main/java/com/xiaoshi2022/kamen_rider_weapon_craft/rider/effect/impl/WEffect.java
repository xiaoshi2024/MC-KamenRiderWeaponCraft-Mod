package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;

public class WEffect implements HeiseiRiderEffect {

    // W的不同记忆体组合
    private enum WMemoryCombo {
        CycloneJoker, 
        HeatMetal, 
        LunaTrigger, 
        FangJoker, 
        CycloneTrigger
    }

    @Override
    public void executeSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Joker Extreme攻击，使用不同的记忆体组合
            // 随机选择一个记忆体组合
            WMemoryCombo selectedCombo = WMemoryCombo.values()[level.random.nextInt(WMemoryCombo.values().length)];
            
            switch (selectedCombo) {
                case CycloneJoker:
                    executeCycloneJoker(level, player, direction);
                    break;
                case HeatMetal:
                    executeHeatMetal(level, player, direction);
                    break;
                case LunaTrigger:
                    executeLunaTrigger(level, player, direction);
                    break;
                case FangJoker:
                    executeFangJoker(level, player, direction);
                    break;
                case CycloneTrigger:
                    executeCycloneTrigger(level, player, direction);
                    break;
            }
            
            // 给予玩家双重驱动相关的增益效果
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    private void executeCycloneJoker(Level level, Player player, Vec3 direction) {
        // 飓风王牌形态：高速移动和剑攻击
        // 给予玩家高速移动效果
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 3));
        
        // 向前方进行快速斩击
        double reach = 8.0;
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 end = start.add(direction.scale(reach));
        
        net.minecraft.world.phys.HitResult result = player.pick(reach, 0.0f, false);
        
        if (result instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                    level.damageSources().playerAttack(player), getAttackDamage());
                // 给予敌人短暂的缓速效果
                ((net.minecraft.world.entity.LivingEntity) entity).addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
            }
        }
    }
    
    private void executeHeatMetal(Level level, Player player, Vec3 direction) {
        // 炽热金属形态：火焰和坚硬的攻击 - 优化：移除Thread.sleep，使用更高效的实现
        // 给予玩家抵抗火焰效果
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0));
        
        // 前方制造火焰路径
        for (int i = 0; i < 8; i++) {
            Vec3 firePos = player.getEyePosition(1.0f).add(direction.scale(i + 1));
            net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos(
                (int)firePos.x, 
                (int)firePos.y, 
                (int)firePos.z
            );
            
            if (level.isEmptyBlock(blockPos)) {
                // 在地面上制造火
                net.minecraft.core.BlockPos groundPos = blockPos.below();
                if (level.getBlockState(groundPos).isSolidRender(level, groundPos)) {
                    level.setBlock(blockPos, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState(), 1);
                    // 注意：移除了Thread.sleep(5000)，因为它会阻塞服务器线程
                    // 火焰会按照Minecraft默认的燃烧机制自动熄灭
                }
            }
            
            // 对火焰路径上的敌人造成伤害
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                    new net.minecraft.world.phys.AABB(firePos, firePos).inflate(1.0),
                    entity -> entity != player) // 提前过滤掉玩家自己
                .forEach(entity -> {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.3f);
                    ((net.minecraft.world.entity.LivingEntity) entity).setSecondsOnFire(3);
                });
        }
    }
    
    private void executeLunaTrigger(Level level, Player player, Vec3 direction) {
        // 月神扳机形态：远程和弹性攻击 - 优化：移除Thread.sleep，使用更高效的实现
        // 发射可弯曲的子弹
        Vec3[] bulletDirections = new Vec3[4];
        for (int i = 0; i < 4; i++) {
            // 预计算4个不同的弯曲子弹方向，模拟子弹轨迹的变化
            bulletDirections[i] = new Vec3(
                direction.x + (level.random.nextDouble() - 0.5) * 0.6,
                direction.y + (level.random.nextDouble() - 0.5) * 0.6,
                direction.z + (level.random.nextDouble() - 0.5) * 0.6
            ).normalize();
        }
        
        // 对每个弯曲方向检测并攻击敌人
        for (Vec3 bulletDir : bulletDirections) {
            Vec3 start = player.getEyePosition(1.0f);
            Vec3 end = start.add(bulletDir.scale(15.0));
            
            // 使用更高效的实体查找方式
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                    new net.minecraft.world.phys.AABB(start, end).inflate(0.5),
                    entity -> entity != player) // 提前过滤掉玩家自己
                .forEach(entity -> {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.45f); // 略微提高伤害以弥补没有延迟的效果
                });
        }
        
        // 给予玩家短暂的夜视效果
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0));
    }
    
    private void executeFangJoker(Level level, Player player, Vec3 direction) {
        // 獠牙王牌形态：野兽般的攻击 - 优化：移除Thread.sleep，使用更高效的连续攻击实现
        // 对周围敌人进行连续攻击
        // 优化：在单次循环中处理连续攻击的效果
        Vec3[] attackPositions = new Vec3[3];
        for (int i = 0; i < 3; i++) {
            // 计算三个不同位置的攻击点，模拟连续攻击
            attackPositions[i] = player.position().add(
                (level.random.nextDouble() - 0.5) * 2.0,
                0.0,
                (level.random.nextDouble() - 0.5) * 2.0
            );
        }
        
        // 使用更高效的实体查找方式对所有攻击点附近的敌人造成伤害
        for (Vec3 attackPos : attackPositions) {
            level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, 
                    new net.minecraft.world.phys.AABB(attackPos, attackPos).inflate(5.0), // 略微增大攻击范围
                    entity -> entity != player) // 提前过滤掉玩家自己
                .forEach(entity -> {
                    ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                        level.damageSources().playerAttack(player), getAttackDamage() * 0.45f); // 略微提高伤害以弥补没有延迟的效果
                });
        }
        
        // 给予玩家多种增益效果
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 2));
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1));
    }
    
    private void executeCycloneTrigger(Level level, Player player, Vec3 direction) {
        // 飓风扳机形态：风暴和子弹组合攻击
        // 制造一个小型风暴
        level.explode(player, player.getX(), player.getY(), player.getZ(), 
            getEffectRange() / 2, Level.ExplosionInteraction.MOB);
        
        // 发射多枚子弹
        for (int i = 0; i < 8; i++) {
            double angle = 2 * Math.PI * i / 8;
            Vec3 bulletDir = new Vec3(
                Math.cos(angle) * 0.8 + direction.x * 0.2,
                direction.y + (level.random.nextDouble() - 0.5) * 0.3,
                Math.sin(angle) * 0.8 + direction.z * 0.2
            ).normalize();
            
            Vec3 start = player.getEyePosition(1.0f);
            Vec3 end = start.add(bulletDir.scale(12.0));
            
            net.minecraft.world.phys.HitResult bulletResult = player.pick(12.0, 0.0f, false);
                
                if (bulletResult instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
                    Entity entity = entityHitResult.getEntity();
                    if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                        ((net.minecraft.world.entity.LivingEntity) entity).hurt(
                            level.damageSources().playerAttack(player), getAttackDamage() * 0.3f);
                    }
                }
        }
        
        // 给予玩家短暂的速度加成
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 2));
    }

    @Override
    public String getRiderName() {
        return "W";
    }

    @Override
    public String getActivationSoundName() {
        return "Joker Extreme!";
    }

    @Override
    public float getAttackDamage() {
        return 51.0f; // 高级骑士 - W是双人一体骑士，拥有多种记忆体组合和强大力量，伤害较高
    }

    @Override
    public float getEffectRange() {
        return 7.0f;
    }
}
