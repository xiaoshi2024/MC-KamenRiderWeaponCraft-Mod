package com.xiaoshi2022.kamen_rider_weapon_craft.entity.ai.zombie;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kamen_rider_weapon_craft")
public class ZombieSonicArrowEventHandler {
    
    // 监听僵尸生成事件
    @SubscribeEvent
    public static void onZombieSpawn(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        
        // 检查是否是僵尸实体
        if (entity instanceof Zombie zombie) {
            // 更新音速弓AI状态
            ZombieSonicArrowController.updateSonicArrowGoalForZombie(zombie);
        }
    }
    
    // 监听装备变化事件，确保僵尸在拿起或放下音速弓时更新AI
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        // 检查是否是僵尸实体
        if (event.getEntity() instanceof Zombie zombie) {
            ItemStack oldItem = event.getFrom();
            ItemStack newItem = event.getTo();
            
            // 检查是否涉及音速弓的装备变化
            boolean wasHoldingSonicArrow = oldItem.getItem() instanceof sonicarrow;
            boolean isHoldingSonicArrow = newItem.getItem() instanceof sonicarrow;
            
            // 如果装备变化涉及音速弓，更新AI
            if (wasHoldingSonicArrow || isHoldingSonicArrow) {
                ZombieSonicArrowController.updateSonicArrowGoalForZombie(zombie);
            }
        }
    }
}