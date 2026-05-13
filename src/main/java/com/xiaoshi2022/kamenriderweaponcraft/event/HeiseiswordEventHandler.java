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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft.MODID;

/**
 * 平成剑事件处理器
 * 用于处理能量恢复和其他相关事件
 */
@EventBusSubscriber(modid = MODID)
public class HeiseiswordEventHandler {
    // ✅ 修复：使用 Map 存储每个玩家的独立计数器
    private static final Map<UUID, Integer> tickCounters = new ConcurrentHashMap<>();

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

        // 检查是否手持平成剑
        boolean hasHeiseisword = player.getMainHandItem().getItem() instanceof Heiseisword ||
                player.getOffhandItem().getItem() instanceof Heiseisword;
        if (!hasHeiseisword) {
            // 如果玩家没有手持平成剑，清理其计数器
            tickCounters.remove(player.getUUID());
            return;
        }

        // 获取或创建玩家的 tick 计数器
        int tickCounter = tickCounters.getOrDefault(player.getUUID(), 0);
        tickCounter++;

        // 每20个tick（1秒）恢复一次能量
        if (tickCounter >= 20) {
            tickCounter = 0;
            // 更新玩家能量恢复
            HeiseiswordEnergyManager.updateEnergyRegen(player);
        }

        tickCounters.put(player.getUUID(), tickCounter);
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