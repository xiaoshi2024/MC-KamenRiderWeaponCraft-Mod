package com.xiaoshi2022.kamen_rider_weapon_craft.procedures;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.SonicBowContainer;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.network.NetworkHooks;

public class YAnXiaAnJianShiProcedure {
    private static final int COOLDOWN_TICKS = 2 * 20; // 2 秒

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
        if (!(entity instanceof ServerPlayer player)) return;

        long last = player.getPersistentData().getLong("sonicGuiCooldown");
        long now = world instanceof ServerLevel sl ? sl.getGameTime()
                : world.dayTime();

        if (now - last < COOLDOWN_TICKS) {
            int remain = (int) Math.ceil((COOLDOWN_TICKS - (now - last)) / 20.0);
            player.displayClientMessage(Component.literal("冷却中，还需 " + remain + " 秒"), true);
            return;
        }

        if (player.getOffhandItem().is(ModItems.SONICARROW.get())) {
            BlockPos pos = BlockPos.containing(x, y, z);
            NetworkHooks.openScreen(player,
                    new MenuProvider() {
                        @Override public Component getDisplayName() {
                            return Component.literal("ssonic");
                        }
                        @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) {
                            return new SonicBowContainer(id, inv,
                                    new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(pos));
                        }
                    }, pos);
            player.getPersistentData().putLong("sonicGuiCooldown", now);
        }
    }
}