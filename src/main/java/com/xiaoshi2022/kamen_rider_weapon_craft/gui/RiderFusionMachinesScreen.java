package com.xiaoshi2022.kamen_rider_weapon_craft.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.NetworkHandler;
import com.xiaoshi2022.kamen_rider_weapon_craft.network.StartCraftingPacket;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.RiderFusionMachineContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RiderFusionMachinesScreen extends AbstractContainerScreen<RiderFusionMachineContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("kamen_rider_weapon_craft:textures/screens/rider_fusion_machines.png");
    private static final ResourceLocation PROGRESS_BAR = new ResourceLocation("kamen_rider_weapon_craft:textures/screens/fusion_machines_full.png");

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

        int progress = menu.getCraftingProgress();
        int maxProgress = menu.getMaxCraftingProgress();
        if (maxProgress > 0 && progress > 0) {
            float progressRatio = (float) progress / maxProgress;
            int progressWidth = (int) (59 * progressRatio);
            guiGraphics.blit(PROGRESS_BAR, this.leftPos + 93, this.topPos + 23, 0, 0, progressWidth, 17, 59, 17);
        }
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.kamen_rider_weapon_craft.rider_fusion_machines.button_controls"), button -> {
            NetworkHandler.INSTANCE.sendToServer(new StartCraftingPacket(menu.getBlockPos()));
        }).bounds(this.leftPos + 79, this.topPos + 53, 67, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}