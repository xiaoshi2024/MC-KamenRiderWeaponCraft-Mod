package com.xiaoshi2022.kamen_rider_weapon_craft.procedures;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.Advancement;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DdropProcedure {
	@SubscribeEvent
	public static void onGemDropped(ItemTossEvent event) {
		if (event.getPlayer() instanceof ServerPlayer) {
			ServerPlayer player = (ServerPlayer) event.getPlayer();
			ItemStack itemStack = event.getEntity().getItem(); // 获取丢弃的物品

			if (itemStack.getItem() == ModItems.DAIDAIMARU.get()) {
				Advancement advancement = player.getServer().getAdvancements().getAdvancement(new ResourceLocation("kamen_rider_weapon_craft", "daidaimaru"));
				if (advancement != null) {
					AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
					if (!progress.isDone()) {
						player.getAdvancements().award(advancement, "trigger");
					}
				}
			}
		}
	}
}