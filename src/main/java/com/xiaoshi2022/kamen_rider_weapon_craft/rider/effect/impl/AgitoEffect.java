package com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.impl;

import com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.AbstractHeiseiRiderEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AgitoEffect extends AbstractHeiseiRiderEffect {

    @Override
    public void executePlayerSpecialAttack(Level level, Player player, Vec3 direction) {
        if (!level.isClientSide) {
            // 服务器端：发动Ground Flame攻击，造成范围伤害并给予玩家力量效果
            // 使用方向向量创建锥形攻击区域，只攻击玩家面前的目标
            Vec3 normalizedDirection = direction.normalize();
            double range = getEffectRange();
            
            // 创建基于玩家前方的锥形AABB区域
            Vec3 start = player.getEyePosition(1.0f);
            Vec3 end = start.add(normalizedDirection.scale(range));
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(range / 2, 2.0, range / 2);
            
            level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
                // 排除玩家自己
                if (entity == player) return false;
                
                // 计算实体与玩家之间的方向向量
                Vec3 toEntity = entity.position().subtract(player.position()).normalize();
                // 确保目标在玩家面前45度角范围内（余弦值大于约0.7）
                double dotProduct = toEntity.dot(normalizedDirection);
                return dotProduct > 0.7; // 约45度角
            }).forEach(livingEntity -> {
                // 先检查是否为僵尸村民，如果是则净化，否则造成伤害
                if (livingEntity instanceof ZombieVillager zombieVillager) {
                    // 净化僵尸村民，将其转变为村民
                    purifyZombieVillager(level, zombieVillager);
                } else {
                    // 对其他实体造成伤害
                    livingEntity.hurt(level.damageSources().playerAttack(player), getAttackDamage());
                }
                
                // 为被击中的实体添加发光效果
                livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
            });
            
            // 给予玩家力量效果
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
            // 给予玩家发光效果，象征Agito的光芒
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        } else {
            // 客户端：粒子效果已移除，后续将使用geo动画还原
        }
    }
    
    /**
     * 净化僵尸村民，将其立即转变为村民
     * 使用直接创建村民实体的方式，确保MCA模组兼容性并保留原有数据
     */
    private void purifyZombieVillager(Level level, ZombieVillager zombieVillager) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            try {
                // 保存位置信息用于粒子效果
                double x = zombieVillager.getX();
                double y = zombieVillager.getY();
                double z = zombieVillager.getZ();
                float yaw = zombieVillager.getYRot();
                float pitch = zombieVillager.getXRot();
                
                // 保存僵尸村民的数据
                CompoundTag originalData = new CompoundTag();
                zombieVillager.saveWithoutId(originalData);
                
                // 保存村民数据（职业、类型等）
                VillagerData villagerData = zombieVillager.getVillagerData();
                
                // 创建新的村民实体
                Villager villager = EntityType.VILLAGER.create(serverLevel);
                if (villager != null) {
                    // 设置村民的位置和旋转
                    villager.setPos(x, y, z);
                    villager.setYRot(yaw);
                    villager.setXRot(pitch);
                    
                    // 设置村民数据
                    villager.setVillagerData(villagerData);
                    
                    // 尝试保留MCA模组的村民数据（如果存在）
                    try {
                        // 检查是否有MCA的自定义数据
                        if (originalData.contains("MCA")) {
                            CompoundTag mcaData = originalData.getCompound("MCA");
                            CompoundTag villagerDataTag = new CompoundTag();
                            villager.saveWithoutId(villagerDataTag);
                            villagerDataTag.put("MCA", mcaData);
                            villager.load(villagerDataTag);
                        }
                    } catch (Exception ignored) {
                        // 如果MCA数据处理失败，继续执行
                    }
                    
                    // 尝试设置村民的额外属性
                    try {
                        // 设置交易数据
                        if (originalData.contains("Offers")) {
                            villager.load(originalData);
                        }
                        
                        // 设置其他可能的数据
                        if (originalData.contains("Age")) {
                            villager.setAge(originalData.getInt("Age"));
                        }
                        
                        // 设置村民是否有AI
                        villager.setNoAi(zombieVillager.isNoAi());
                        
                        // 设置村民是否处于静止状态
                        if (zombieVillager.hasCustomName()) {
                            villager.setCustomName(zombieVillager.getCustomName());
                            villager.setCustomNameVisible(zombieVillager.isCustomNameVisible());
                        }
                    } catch (Exception ignored) {
                        // 如果设置额外属性失败，继续执行
                    }
                    
                    // 移除僵尸村民并生成村民
                    zombieVillager.remove(Entity.RemovalReason.DISCARDED);
                    serverLevel.addFreshEntity(villager);
                    
                    // 播放净化音效
                    level.playSound(null, x, y, z, 
                            SoundEvents.ZOMBIE_VILLAGER_CURE,
                            SoundSource.PLAYERS, 1.0F, 1.0F);
                    
                    // 添加净化粒子效果
                    for (int i = 0; i < 20; i++) {
                        double dx = (level.random.nextDouble() - 0.5) * 2.0;
                        double dy = level.random.nextDouble() * 2.0;
                        double dz = (level.random.nextDouble() - 0.5) * 2.0;
                        level.addParticle(
                                ParticleTypes.HEART,
                                x, y + 0.5, z,
                                dx * 0.2, dy * 0.2, dz * 0.2
                        );
                    }
                }
            } catch (Exception e) {
                // 如果整个转换过程失败，记录错误但不崩溃
                System.err.println("净化僵尸村民时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 为非玩家实体（如僵尸）执行Agito的特殊攻击效果
     * 确保非玩家实体使用该武器时也能触发净化和发光效果
     */
    @Override
    public void executeNonPlayerSpecialAttack(Level level, LivingEntity shooter, Vec3 direction) {
        if (!level.isClientSide) {
            // 播放骑士选择音效
            com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager.playSelectionSound(level, shooter, getRiderName());
            
            // 播放攻击音效
            com.xiaoshi2022.kamen_rider_weapon_craft.rider.effect.HeiseiRiderEffectManager.playAttackSound(level, shooter, getRiderName());
            
            // 使用方向向量创建锥形攻击区域，只攻击射手面前的目标
            Vec3 normalizedDirection = (direction != null && direction.lengthSqr() > 0) ? 
                                      direction.normalize() : shooter.getLookAngle().normalize();
            double range = getEffectRange();
            
            // 创建基于射手前方的锥形AABB区域
            Vec3 start = shooter.getEyePosition(1.0f);
            Vec3 end = start.add(normalizedDirection.scale(range));
            net.minecraft.world.phys.AABB attackBox = new net.minecraft.world.phys.AABB(start, end).inflate(range / 2, 2.0, range / 2);
            
            // 对范围内的实体进行净化或攻击
            level.getEntitiesOfClass(LivingEntity.class, attackBox, entity -> {
                // 排除射手自己
                if (entity == shooter) return false;
                
                // 计算实体与射手之间的方向向量
                Vec3 toEntity = entity.position().subtract(shooter.position()).normalize();
                // 确保目标在射手面前45度角范围内（余弦值大于约0.7）
                double dotProduct = toEntity.dot(normalizedDirection);
                return dotProduct > 0.7; // 约45度角
            }).forEach(livingEntity -> {
                // 先检查是否为僵尸村民，如果是则净化，否则造成伤害
                if (livingEntity instanceof ZombieVillager zombieVillager) {
                    // 净化僵尸村民，将其转变为村民
                    purifyZombieVillager(level, zombieVillager);
                } else {
                    // 对其他实体造成伤害
                    livingEntity.hurt(level.damageSources().mobAttack(shooter), getAttackDamage());
                }
                
                // 为被击中的实体添加发光效果
                livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
            });
            
            // 给予使用武器的非玩家实体发光效果
            shooter.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        }
    }

    @Override
    public String getRiderName() {
        return "Agito";
    }

    @Override
    public String getActivationSoundName() {
        return "Ground Flame!";
    }

    @Override
    public float getAttackDamage() {
        return 46.0f; // 普通骑士 - Agito作为平成第二位骑士，伤害提高以应对强大敌人
    }

    @Override
    public float getEffectRange() {
        return 6.0f;
    }
}
