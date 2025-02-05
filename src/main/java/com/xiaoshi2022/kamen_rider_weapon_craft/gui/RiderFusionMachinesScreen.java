package com.xiaoshi2022.kamen_rider_weapon_craft.gui;

import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.RiderFusionMachineContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

public class RiderFusionMachinesScreen extends AbstractContainerScreen<RiderFusionMachineContainer> {
	private final HashMap<String, Object> guistate = RiderFusionMachineContainer.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private Button button_controls;

	public RiderFusionMachinesScreen(RiderFusionMachineContainer container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	private static final ResourceLocation texture = new ResourceLocation("kamen_rider_weapon_craft:textures/screens/rider_fusion_machines.png");


	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	// 修改renderBg方法
	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		// ...原有纹理绘制代码...

		// 直接从容器获取最新进度（关键修复）
		int progress = menu.getCraftingProgress();
		int maxProgress = menu.getMaxCraftingProgress();

		// 避免分母为0
		if (maxProgress <= 0) maxProgress = 1;

		int progressWidth = (int) ((double) progress / maxProgress * 100);
		guiGraphics.fill(
				this.leftPos + 50, this.topPos + 70,
				this.leftPos + 50 + progressWidth,
				this.topPos + 75,
				0xFF00FF00
		);
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void init() {
		super.init();
		button_controls = new PlainTextButton(this.leftPos + 79, this.topPos + 53, 67, 20, Component.translatable("gui.kamen_rider_weapon_craft.rider_fusion_machines.button_controls"), e -> {
			((RiderFusionMachineContainer) this.menu).tryCraft(); // 调用合成逻辑
		}, this.font);
		guistate.put("button:button_controls", button_controls);
		this.addRenderableWidget(button_controls);
	}
}