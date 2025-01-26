package com.xiaoshi2022.kamen_rider_weapon_craft.procedures;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.SonicBowContainer;
import net.minecraftforge.network.NetworkHooks;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.MenuProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import io.netty.buffer.Unpooled;

public class YAnXiaAnJianShiProcedure {
    private static final int COOLDOWN_INTERVAL = 12 * 20; // 设置冷却时间为12秒

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (entity == null)
            return;

        // 获取玩家最后一次打开 GUI 的时间
        long lastOpened = entity.getPersistentData().getLong("lastOpenedGui");

        // 获取当前时间
        long currentTime = world.dayTime();

        // 检查是否已经过了冷却时间
        if (currentTime - lastOpened >= COOLDOWN_INTERVAL) {
            if ((entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == ModItems.SONICARROW.get()) {
                if (entity instanceof ServerPlayer _ent) {
                    BlockPos _bpos = BlockPos.containing(x, y, z);
                    NetworkHooks.openScreen((ServerPlayer) _ent, new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.literal("ssonic");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                            return new SonicBowContainer(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));
                        }
                    }, _bpos);

                    // 更新玩家最后一次打开 GUI 的时间
                    entity.getPersistentData().putLong("lastOpenedGui", currentTime);
                }
            }
        } else {
            // 如果未达到冷却时间，可以在这里添加一些提示信息
            if (entity instanceof Player player) {
                player.displayClientMessage(Component.literal("冷却时间未结束，还需等待 " + (COOLDOWN_INTERVAL - (currentTime - lastOpened)) / 20 + " 秒"), true);
            }
        }
    }
}