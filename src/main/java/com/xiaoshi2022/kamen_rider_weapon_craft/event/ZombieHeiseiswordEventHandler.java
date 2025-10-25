package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamen_rider_weapon_craft.entity.ai.zombie.ZombieHeiseiswordController;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
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
            // 20%的几率让自然生成的僵尸持有平成剑
            else if (zombie.level().random.nextFloat() <= 0.2f && !zombie.isBaby()) {
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
    
    // 设置持有平成剑的僵尸血量为300点
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
        
        // 设置新的血量上限为300点
        zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(HeiseiswordConstants.HEISEISWORD_HEALTH);
        // 同时恢复到满血
        zombie.setHealth(HeiseiswordConstants.HEISEISWORD_HEALTH);
    }
    
    // 恢复僵尸的原始血量
    private static void restoreOriginalHealth(Zombie zombie) {
        // 确保只在服务器端执行
        if (zombie.level().isClientSide()) return;
        
        CompoundTag nbt = zombie.getPersistentData();
        // 检查是否存储了原始血量
        if (nbt.contains(HeiseiswordConstants.TAG_ORIGINAL_HEALTH)) {
            double originalHealth = nbt.getDouble(HeiseiswordConstants.TAG_ORIGINAL_HEALTH);
            // 设置回原始血量上限
            zombie.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(originalHealth);
            // 调整当前血量，确保不超过新的上限
            zombie.setHealth((float)Math.min(zombie.getHealth(), originalHealth));
            // 移除原始血量标签
            nbt.remove(HeiseiswordConstants.TAG_ORIGINAL_HEALTH);
        }
    }
}