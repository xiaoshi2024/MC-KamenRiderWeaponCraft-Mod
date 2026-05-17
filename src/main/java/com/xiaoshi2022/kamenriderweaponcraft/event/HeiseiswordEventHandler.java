package com.xiaoshi2022.kamenriderweaponcraft.event;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.rider.energy.HeiseiswordEnergyManager;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft.MODID;

/**
 * 平成剑事件处理器
 * 用于处理能量恢复和其他相关事件
 */
@EventBusSubscriber(modid = MODID)
public class HeiseiswordEventHandler {

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

        // 检查玩家背包中是否有平成剑
        boolean hasHeiseisword = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() instanceof Heiseisword) {
                hasHeiseisword = true;
                break;
            }
        }

        if (!hasHeiseisword) {
            return;
        }

        // 更新能量恢复（会扫描背包中所有平成剑）
        HeiseiswordEnergyManager.updateEnergyRegen(player);
    }

    /**
     * 监听玩家攻击实体事件，处理平成剑的特殊攻击效果
     */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (stack.getItem() instanceof Heiseisword heiseiSword) {
            heiseiSword.onLeftClickEntity(stack, player, event.getTarget());
        }
    }
}