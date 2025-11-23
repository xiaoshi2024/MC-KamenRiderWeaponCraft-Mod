package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.ridebooker;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.decade.DecadeRiderEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DecadeEffect extends AbstractHeiseiRiderEffect {
    
    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        // 调用父类方法实现前方定向攻击
        super.executePlayerSpecialAttack(level, player, direction);
        
        if (!level.isClientSide) {
            // 为玩家生成Decade特效实体
            DecadeRiderEntity.trySpawnEffect(level, player, direction, getAttackDamage());
            
            // 给予玩家增益效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 0));
            
            // 检测附近持有平成剑的实体并触发对应骑士效果
            triggerNearbyRiderEffects(level, player, direction);
        }
    }
    
    /**
     * 检测附近持有平成剑的实体，并为每个不同的骑士触发额外效果
     */
    private void triggerNearbyRiderEffects(Level level, Player player, Vec3 direction) {
        // 获取玩家周围10格范围内的所有生物
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, 
                player.getBoundingBox().inflate(10.0D), 
                entity -> entity != player && entity.isAlive());
        
        // 使用Set来存储已触发的骑士，避免重复触发
        Set<String> triggeredRiders = new HashSet<>();
        
        // 为每个附近持有平成剑的实体检查其选择的骑士
        for (LivingEntity nearbyEntity : nearbyEntities) {
            // 检查实体是否持有平成剑（假设平成剑是ridebooker类型）
            ItemStack mainHandItem = nearbyEntity.getMainHandItem();
            if (mainHandItem.getItem() instanceof ridebooker) {
                // 从平成剑中获取选择的骑士名称
                String selectedRider = getSelectedRiderFromRidebooker(mainHandItem);
                if (selectedRider != null && !selectedRider.isEmpty() && !triggeredRiders.contains(selectedRider)) {
                    // 为每个不同的骑士触发额外效果
                    triggerRiderEffect(level, player, direction, selectedRider);
                    triggeredRiders.add(selectedRider);
                }
            }
        }
    }
    
    /**
     * 从平成剑物品中获取当前选择的骑士名称
     */
    private String getSelectedRiderFromRidebooker(ItemStack ridebookerStack) {
        // 假设ridebooker物品使用NBT存储当前选择的骑士
        CompoundTag tag = ridebookerStack.getTag();
        if (tag != null && tag.contains("SelectedRider")) {
            return tag.getString("SelectedRider");
        }
        return null;
    }
    
    /**
     * 触发特定骑士的效果
     */
    private void triggerRiderEffect(Level level, Player player, Vec3 direction, String riderName) {
        // 播放特效声音
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.8F, 1.0F);
        
        // 添加视觉效果提示玩家触发了额外骑士效果
        level.addParticle(net.minecraft.core.particles.ParticleTypes.CRIT, 
                player.getX() + direction.x * 2, 
                player.getY() + 1, 
                player.getZ() + direction.z * 2, 
                0.5, 0.5, 0.5);
        
        // 为多个粒子添加循环
        for (int i = 0; i < 20; i++) {
            double dx = (level.random.nextDouble() - 0.5) * 0.5;
            double dy = (level.random.nextDouble() - 0.5) * 0.5;
            double dz = (level.random.nextDouble() - 0.5) * 0.5;
            level.addParticle(net.minecraft.core.particles.ParticleTypes.CRIT, 
                    player.getX() + direction.x * 2 + dx,
                    player.getY() + 1 + dy,
                    player.getZ() + direction.z * 2 + dz,
                    0.0, 0.0, 0.0);
        }
        
        // 创建略微偏移的方向向量
        Vec3 offsetDirection = new Vec3(
                direction.x + (level.random.nextDouble() - 0.5) * 0.3,
                direction.y + (level.random.nextDouble() - 0.5) * 0.3,
                direction.z + (level.random.nextDouble() - 0.5) * 0.3
        ).normalize();
        
        // 根据骑士名称生成对应的骑士特效
        spawnSpecificRiderEffect(level, player, offsetDirection, riderName);
    }
    
    /**
     * 根据骑士名称生成特定的骑士特效
     */
    private void spawnSpecificRiderEffect(Level level, Player player, Vec3 direction, String riderName) {
        if (level.isClientSide) return;
        
        // 使用30%的原始伤害
        float damage = getAttackDamage() * 0.3F;
        
        // 根据不同的骑士名称生成对应的特效
        switch (riderName.toLowerCase()) {
            case "kuuga":
                // 生成Kuuga特效
                try {
                    com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kuuga.KuugaRiderEntity.trySpawnEffect(level, player, direction, damage);
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "agito":
                // 生成Agito特效（只有效果类）
                try {
                    throw new Exception("Agito只有效果类，没有实体类");
                } catch (Exception e) {
                    // 直接使用Decade特效作为替代
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "ryuki":
                // 生成Ryuki特效（只有效果类）
                try {
                    throw new Exception("Ryuki只有效果类，没有实体类");
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "faiz":
                // 生成Faiz特效（只有效果类）
                try {
                    throw new Exception("Faiz只有效果类，没有实体类");
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "blade":
                // 生成Blade特效（只有效果类）
                try {
                    throw new Exception("Blade只有效果类，没有实体类");
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "hibiki":
                // 生成Hibiki特效（只有效果类）
                try {
                    throw new Exception("Hibiki只有效果类，没有实体类");
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "kabuto":
                // 生成Kabuto特效（只有效果类）
                try {
                    throw new Exception("Kabuto只有效果类，没有实体类");
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "den-o":
                // 生成Den-O特效（只有效果类）
                try {
                    throw new Exception("Den-O只有效果类，没有实体类");
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "kiva":
                // 生成Kiva特效
                try {
                    com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.kiva.KivaBatEntity.trySpawnEffect(level, player, direction, damage);
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "decade":
                // 生成Decade特效
                DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                break;
            case "w":
                // 生成W特效
                try {
                    com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.w.WTornadoEntity.trySpawnTornado(level, player, direction);
                    // 如果W有龙卷风特效也生成
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "ooo":
                // 生成OOO特效（需要额外参数）
                try {
                    throw new Exception("OOOGeoEntity需要额外的String参数");
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "fourze":
                // 生成Fourze特效（方法签名不匹配）
                try {
                    throw new Exception("FourzeRocketEntity的方法签名不匹配");
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "wizard":
                // 生成Wizard特效（需要额外参数）
                try {
                    throw new Exception("Wizard需要额外的DragonMagicType参数");
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "gaim":
                // 生成Gaim特效（方法签名不匹配）
                try {
                    throw new Exception("GaimLockSeedEntity的方法签名不匹配");
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "drive":
                // 生成Drive特效
                try {
                    com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.drive.DriveRiderEntity.trySpawnEffect(level, player, direction, damage);
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "ghost":
                // 生成Ghost特效
                try {
                    // 尝试使用不同的类名或包名
                    Class<?> ghostClass = Class.forName("com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.ghost.GhostRiderEntity");
                    // 如果类存在但方法调用失败，会被外层catch捕获
                    throw new Exception("Ghost实体类暂不可用");
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "ex-aid":
            case "exaid":
                // 生成Ex-Aid特效
                try {
                    // 尝试使用不同的类名或包名
                    Class<?> exaidClass = Class.forName("com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.exaid.ExAidRiderEntity");
                    throw new Exception("Ex-Aid实体类暂不可用");
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            case "build":
                // 生成Build特效
                try {
                    com.xiaoshi2022.kamen_rider_weapon_craft.rider.heisei.build.BuildRiderEntity.trySpawnEffect(level, player, direction, damage);
                } catch (Exception e) {
                    DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                }
                break;
            default:
                // 默认使用Decade特效
                DecadeRiderEntity.trySpawnEffect(level, player, direction, damage);
                break;
        }
    }
    
    // Decade现在只会根据附近持有平成剑的实体来触发对应骑士效果

    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        // 调用父类方法实现前方定向攻击
        super.executeNonPlayerSpecialAttack(level, shooter, direction);
        
        if (!level.isClientSide) {
            // 为非玩家实体生成Decade特效实体
            DecadeRiderEntity.trySpawnEffect(level, shooter, direction, getAttackDamage());
            
            // 添加音效
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            
            // 给予实体增益效果
            shooter.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 1));
            shooter.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 0));
        }
    }

    @Override
    public String getRiderName() {
        return "Decade";
    }

    @Override
    public String getActivationSoundName() {
        return "Dimension Kick!";
    }

    @Override
    public float getAttackDamage() {
        return 52.0f; // 高级骑士 - Decade作为骑士破坏者，拥有极高的伤害能力
    }

    @Override
    public float getEffectRange() {
        return 8.0f;
    }
}
