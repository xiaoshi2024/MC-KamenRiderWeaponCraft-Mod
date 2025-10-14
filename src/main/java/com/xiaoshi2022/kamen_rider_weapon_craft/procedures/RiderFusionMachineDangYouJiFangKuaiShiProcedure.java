package com.xiaoshi2022.kamen_rider_weapon_craft.procedures;

import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.RiderFusionMachineContainer;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.network.NetworkHooks;

public class RiderFusionMachineDangYouJiFangKuaiShiProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null) {
			return;
		}

		// 设置方块动画状态
		BlockPos blockPos = BlockPos.containing(x, y, z);
		BlockState blockState = world.getBlockState(blockPos);
		if (blockState.getBlock().getStateDefinition().getProperty("animation") instanceof IntegerProperty animationProperty) {
			IntegerProperty integerProperty = animationProperty;
			if (integerProperty.getPossibleValues().contains(1)) {
				world.setBlock(blockPos, blockState.setValue(integerProperty, 1), 3);
			}
		}

		// 打开 GUI
		if (entity instanceof ServerPlayer serverPlayer) {
			NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
				@Override
				public Component getDisplayName() {
					return Component.literal("RiderFusionMachines");
				}

				@Override
				public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
					return new RiderFusionMachineContainer(containerId, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(blockPos));
				}
			}, blockPos);
		}
	}
}