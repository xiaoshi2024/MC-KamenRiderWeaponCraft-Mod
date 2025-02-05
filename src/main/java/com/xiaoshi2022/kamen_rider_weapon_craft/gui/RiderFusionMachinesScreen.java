package com.xiaoshi2022.kamen_rider_weapon_craft.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.RiderFusionMachineContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RiderFusionMachinesScreen extends AbstractContainerScreen<RiderFusionMachineContainer> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("kamen_rider_weapon_craft:textures/screens/rider_fusion_machines.png");

	public RiderFusionMachinesScreen(RiderFusionMachineContainer container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		RenderSystem.disableBlend();

		// 绘制进度条
		// 绘制进度条
		int progress = menu.getCraftingProgress();
		int maxProgress = menu.getMaxCraftingProgress();
		float progressPercent = (float) progress / maxProgress;
		int progressWidth = (int) (85 * progressPercent);
		guiGraphics.blit(TEXTURE, this.leftPos + 55, this.topPos + 22, 118, 18, 176 + progressWidth, 0, progressWidth, 16);
	}

	@Override
	protected void init() {
		super.init();
		addRenderableWidget(Button.builder(Component.translatable("gui.kamen_rider_weapon_craft.rider_fusion_machines.button_controls"), button -> {
			((RiderFusionMachineContainer) this.menu).startCrafting();
		}).bounds(this.leftPos + 79, this.topPos + 53, 67, 20).build());
	}
}