package com.xiaoshi2022.kamenriderweaponcraft.event;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.rider.energy.HeiseiswordEnergyManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft.MODID;

/**
 * 平成剑事件处理器
 * 用于处理能量恢复和其他相关事件
 */
@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class HeiseiswordEventHandler {
    // 用于跟踪tick计数，每20个tick（1秒）恢复一次能量
    private static int tickCounter = 0;
    
    /**
     * 监听玩家tick事件，处理能量恢复
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        
        // 只在服务器端处理
        if (player.level().isClientSide) {
            return;
        }
        
        // 每20个tick（1秒）恢复一次能量
        if (tickCounter++ >= 20) {
            tickCounter = 0;
            // 更新玩家能量恢复
            HeiseiswordEnergyManager.updateEnergyRegen(player);
        }
    }
    
    /**
     * 监听玩家攻击实体事件，处理平成剑的特殊攻击效果
     * 特别是电王模式下的武器形态攻击
     */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        
        // 检查手持物品是否为平成剑
        if (stack.getItem() instanceof Heiseisword heiseiSword) {
            // 调用平成剑的onLeftClickEntity方法处理攻击逻辑
            boolean handled = heiseiSword.onLeftClickEntity(stack, player, event.getTarget());
            if (handled) {
                // 如果攻击被处理，可以阻止默认的攻击行为
                // event.setCanceled(true);
            }
        }
    }
}