package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamen_rider_weapon_craft.entity.ai.zombie.ZombieHeiseiswordController;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft.MOD_ID;

// 常量定义
class HeiseiswordConstants {
    // 平成剑持有者的血量上限
    static final float HEISEISWORD_HEALTH = 300.0F;
    // 存储原始血量的NBT键名
    static final String TAG_ORIGINAL_HEALTH = "OriginalHealth";
    // 存储原始移动速度的NBT键名
    static final String TAG_ORIGINAL_MOVEMENT_SPEED = "OriginalMovementSpeed";
    // 存储原始近战伤害的NBT键名
    static final String TAG_ORIGINAL_ATTACK_DAMAGE = "OriginalAttackDamage";
    // 移动速度加成倍数
    static final double MOVEMENT_SPEED_BONUS = 1.8;
    // 近战伤害加成倍数
    static final double ATTACK_DAMAGE_BONUS = 2.5;
    // 僵尸首领范围（影响附近僵尸的范围）
    static final double ZOMBIE_LEADER_RANGE = 16.0;
}

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ZombieHeiseiswordEventHandler {
    
    // 常量定义
    private static final String TAG_HEISEISWORD_ZOMBIE = "HeiseiswordZombie";
    
    // 当实体生成到世界中时触发
    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
        // 确保只在服务器端执行
        if (event.getEntity().level().isClientSide()) return;
        
        // 检查实体是否是僵尸
        if (event.getEntity() instanceof Zombie zombie) {
            CompoundTag nbt = zombie.getPersistentData();
            
            // 检查僵尸主手是否持有Heiseisword
            ItemStack mainHandItem = zombie.getMainHandItem();
            if (mainHandItem.getItem() instanceof Heiseisword) {
                if (!ZombieHeiseiswordController.hasHeiseiswordGoal(zombie)) {
                    ZombieHeiseiswordController.addHeiseiswordGoal(zombie);
                }
                // 设置血量为300点
                setHeiseiswordHealth(zombie);
                // 添加标记，表明这是一个平成剑僵尸
                nbt.putBoolean(TAG_HEISEISWORD_ZOMBIE, true);
            } 
            // 3%的几率让自然生成的僵尸持有平成剑
            else if (zombie.level().random.nextFloat() <= 0.03f && !zombie.isBaby()) {
                // 检查是否已经处理过这个僵尸或是否是自定义生成的
                if (!nbt.contains(TAG_HEISEISWORD_ZOMBIE) && !nbt.contains("IsCustomSpawned")) {
                    // 给僵尸装备平成剑
                    ItemStack heiseisword = new ItemStack(ModItems.HEISEISWORD.get());
                    zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, heiseisword);
                    
                    // 添加HeiseiswordGoal
                    if (!ZombieHeiseiswordController.hasHeiseiswordGoal(zombie)) {
                        ZombieHeiseiswordController.addHeiseiswordGoal(zombie);
                    }
                    
                    // 设置血量为300点
                    setHeiseiswordHealth(zombie);
                    
                    // 添加标记，表明这是一个平成剑僵尸
                    nbt.putBoolean(TAG_HEISEISWORD_ZOMBIE, true);
                }
            }
        }
    }
    
    // 当实体装备改变时触发
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        // 检查实体是否是僵尸
        if (event.getEntity() instanceof Zombie zombie) {
            // 检查是否是主手装备变化
            if (event.getSlot().getType() == net.minecraft.world.entity.EquipmentSlot.Type.HAND) {
                ItemStack newItem = event.getTo();
                ItemStack oldItem = event.getFrom();
                
                // 新物品是Heiseisword，但旧物品不是
                if (newItem.getItem() instanceof Heiseisword && !(oldItem.getItem() instanceof Heiseisword)) {
                    // 添加HeiseiswordGoal
                    if (!ZombieHeiseiswordController.hasHeiseiswordGoal(zombie)) {
                        ZombieHeiseiswordController.addHeiseiswordGoal(zombie);
                    }
                    // 设置血量为300点
                    setHeiseiswordHealth(zombie);
                }
                // 旧物品是Heiseisword，但新物品不是
                else if (!(newItem.getItem() instanceof Heiseisword) && oldItem.getItem() instanceof Heiseisword) {
                    // 移除HeiseiswordGoal
                    ZombieHeiseiswordController.removeHeiseiswordGoal(zombie);
                    // 恢复原始血量
                    restoreOriginalHealth(zombie);
                }
            }
        }
    }
    
    // 设置持有平成剑的僵尸属性：血量、敏捷度和近战伤害
    private static void setHeiseiswordHealth(Zombie zombie) {
        // 确保只在服务器端执行
        if (zombie.level().isClientSide()) return;
        
        CompoundTag nbt = zombie.getPersistentData();
        // 存储原始血量（如果还没有存储）
        if (!nbt.contains(HeiseiswordConstants.TAG_ORIGINAL_HEALTH)) {
            // 存储原始最大血量值
            double originalHealth = zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).getValue();
            nbt.putDouble(HeiseiswordConstants.TAG_ORIGINAL_HEALTH, originalHealth);
        }
        
        // 存储原始移动速度（如果还没有存储）
        if (!nbt.contains(HeiseiswordConstants.TAG_ORIGINAL_MOVEMENT_SPEED)) {
            double originalSpeed = zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).getValue();
            nbt.putDouble(HeiseiswordConstants.TAG_ORIGINAL_MOVEMENT_SPEED, originalSpeed);
        }
        
        // 存储原始近战伤害（如果还没有存储）
        if (!nbt.contains(HeiseiswordConstants.TAG_ORIGINAL_ATTACK_DAMAGE)) {
            double originalDamage = zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).getValue();
            nbt.putDouble(HeiseiswordConstants.TAG_ORIGINAL_ATTACK_DAMAGE, originalDamage);
        }
        
        // 设置新的血量上限为300点
        zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(HeiseiswordConstants.HEISEISWORD_HEALTH);
        // 同时恢复到满血
        zombie.setHealth(HeiseiswordConstants.HEISEISWORD_HEALTH);
        
        // 设置提升后的移动速度（敏捷度）
        double originalSpeed = nbt.getDouble(HeiseiswordConstants.TAG_ORIGINAL_MOVEMENT_SPEED);
        zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(originalSpeed * HeiseiswordConstants.MOVEMENT_SPEED_BONUS);
        
        // 设置提升后的近战伤害
        double originalDamage = nbt.getDouble(HeiseiswordConstants.TAG_ORIGINAL_ATTACK_DAMAGE);
        zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(originalDamage * HeiseiswordConstants.ATTACK_DAMAGE_BONUS);
        
        // 标记为僵尸首领
        nbt.putBoolean("IsZombieLeader", true);
    }
    
    // 恢复僵尸的原始属性
    private static void restoreOriginalHealth(Zombie zombie) {
        // 确保只在服务器端执行
        if (zombie.level().isClientSide()) return;
        
        CompoundTag nbt = zombie.getPersistentData();
        
        // 恢复原始血量
        if (nbt.contains(HeiseiswordConstants.TAG_ORIGINAL_HEALTH)) {
            double originalHealth = nbt.getDouble(HeiseiswordConstants.TAG_ORIGINAL_HEALTH);
            // 设置回原始血量上限
            zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(originalHealth);
            // 调整当前血量，确保不超过新的上限
            zombie.setHealth((float)Math.min(zombie.getHealth(), originalHealth));
            // 移除原始血量标签
            nbt.remove(HeiseiswordConstants.TAG_ORIGINAL_HEALTH);
        }
        
        // 恢复原始移动速度
        if (nbt.contains(HeiseiswordConstants.TAG_ORIGINAL_MOVEMENT_SPEED)) {
            double originalSpeed = nbt.getDouble(HeiseiswordConstants.TAG_ORIGINAL_MOVEMENT_SPEED);
            zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(originalSpeed);
            nbt.remove(HeiseiswordConstants.TAG_ORIGINAL_MOVEMENT_SPEED);
        }
        
        // 恢复原始近战伤害
        if (nbt.contains(HeiseiswordConstants.TAG_ORIGINAL_ATTACK_DAMAGE)) {
            double originalDamage = nbt.getDouble(HeiseiswordConstants.TAG_ORIGINAL_ATTACK_DAMAGE);
            zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(originalDamage);
            nbt.remove(HeiseiswordConstants.TAG_ORIGINAL_ATTACK_DAMAGE);
        }
        
        // 移除僵尸首领标记
        nbt.remove("IsZombieLeader");
    }
    
    // 当实体死亡并掉落物品时触发
    @SubscribeEvent
    public static void onEntityDeath(LivingDropsEvent event) {
        // 确保只在服务器端执行
        if (event.getEntity().level().isClientSide()) return;
        
        // 检查死亡的实体是否是僵尸
        if (event.getEntity() instanceof Zombie zombie) {
            CompoundTag nbt = zombie.getPersistentData();
            
            // 检查僵尸是否是平成剑僵尸（通过之前设置的标记）
            if (nbt.contains(TAG_HEISEISWORD_ZOMBIE) && nbt.getBoolean(TAG_HEISEISWORD_ZOMBIE)) {
                // 确保主手物品是平成剑
                ItemStack mainHandItem = zombie.getMainHandItem();
                if (mainHandItem.getItem() instanceof Heiseisword && !mainHandItem.isEmpty()) {
                    // 创建一个新的平成剑物品栈以确保掉落
                    ItemStack heiseiswordDrop = new ItemStack(ModItems.HEISEISWORD.get());
                    
                    // 复制原物品的NBT数据（包括选中的骑士等信息）
                    if (mainHandItem.hasTag()) {
                        heiseiswordDrop.setTag(mainHandItem.getTag().copy());
                    }
                    
                    // 强制添加平成剑到掉落物中
                    event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(
                        zombie.level(), 
                        zombie.getX(), 
                        zombie.getY(), 
                        zombie.getZ(), 
                        heiseiswordDrop
                    ));
                }
                
                // 添加掉落异类表盘的几率（20%几率）
                if (zombie.level().random.nextFloat() <= 0.2f) {
                    try {
                        // 直接创建aiziowc实例
                        // Directly use the registered AIZIOWC item from the mod's registry
                        ItemStack ai_wc = new ItemStack(com.xiaoshi2022.kamen_rider_boss_you_and_me.registry.ModItems.AIZIOWC.get());
                        
                        // 添加异类表盘到掉落物中
                        event.getDrops().add(new ItemEntity(
                            zombie.level(), 
                            zombie.getX(), 
                            zombie.getY(), 
                            zombie.getZ(),
                            ai_wc
                        ));
                    } catch (Exception e) {
                        // 如果出现异常，记录警告但不崩溃
                        org.apache.logging.log4j.LogManager.getLogger().warn("Failed to create aiziowc item, skipping drop");
                    }
                }
            }
        }
    }
}