package com.xiaoshi2022.kamenriderweaponcraft.event;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.entity.ai.HeiseiswordController;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

import static com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft.MODID;

/**
 * 平成剑相关事件处理器
 * - 防止僵尸自然生成时持有平成剑（通过其他方式实现）
 * - 支持任何生物使用平成剑时获得AI
 */
@EventBusSubscriber(modid = MODID)
public class HeiseiswordMobEventHandler {

    private static final String TAG_HEISEISWORD_MOB = "HeiseiswordMob";

    /**
     * 当实体加入世界时触发
     */
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Player) {
                return;
            }

            ItemStack mainHandItem = livingEntity.getMainHandItem();
            
            if (mainHandItem.getItem() instanceof Heiseisword) {
                if (livingEntity instanceof Mob mob) {
                    HeiseiswordController.addHeiseiswordGoal(livingEntity);
                }
                
                livingEntity.getPersistentData().putBoolean(TAG_HEISEISWORD_MOB, true);
            }
        }
    }

    /**
     * 当实体装备改变时触发
     */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        
        if (entity instanceof Player) {
            return;
        }

        ItemStack newItem = event.getTo();
        ItemStack oldItem = event.getFrom();

        if (event.getSlot() != net.minecraft.world.entity.EquipmentSlot.MAINHAND) {
            return;
        }

        if (newItem.getItem() instanceof Heiseisword && !(oldItem.getItem() instanceof Heiseisword)) {
            if (entity instanceof Mob mob) {
                HeiseiswordController.addHeiseiswordGoal(entity);
            }
        } else if (!(newItem.getItem() instanceof Heiseisword) && oldItem.getItem() instanceof Heiseisword) {
            if (entity instanceof Mob mob) {
                HeiseiswordController.removeHeiseiswordGoal(entity);
            }
        }
    }
}