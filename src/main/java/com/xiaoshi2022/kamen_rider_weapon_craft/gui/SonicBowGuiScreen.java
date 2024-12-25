package com.xiaoshi2022.kamen_rider_weapon_craft.gui;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.custom.Melon;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.SonicBowContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;


import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.items.IItemHandler;

import static io.netty.handler.codec.DecoderResult.SUCCESS;

public class SonicBowGuiScreen extends AbstractContainerScreen<SonicBowContainer> {
    private final static HashMap<String, Object> guistate = SonicBowContainer.guistate;
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    Button buttonIncreaseEnergy;

    public SonicBowGuiScreen(SonicBowContainer container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    private static final ResourceLocation texture = new ResourceLocation("kamen_rider_weapon_craft:textures/screens/sonic_bow_gui.png");

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();

        // 通过容器实例访问internal
        IItemHandler internal = this.menu.internal;
        renderInventory(guiGraphics, internal);

        // 绘制果汁进度条
        renderJuiceBar(guiGraphics);
    }

    private void renderInventory(GuiGraphics guiGraphics, IItemHandler internal) {
        for (int i = 0; i < internal.getSlots(); i++) {
            ItemStack stack = internal.getStackInSlot(i);
            // 在槽位位置绘制物品
            // 您可以使用guiGraphics.blit或其他方法来绘制物品
            // 例如：
             int x = this.leftPos + 8 + (i % 9) * 18;
             int y = this.topPos + 8 + (i / 9) * 18;
             this.renderItem(guiGraphics, stack, x, y);
        }
    }

    private void renderItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        // 实现物品的渲染逻辑
        // 这里只是一个示例，您需要根据您的需求来实现
    }

    private void renderJuiceBar(GuiGraphics guiGraphics) {
        int x = this.leftPos + 70;
        int y = this.topPos + 20;
        int width = 100;
        int height = 10;
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
        buttonIncreaseEnergy = Button.builder(Component.translatable("gui.cs.ssonic.button_increase_juice"), e -> {
        }).bounds(this.leftPos + 79, this.topPos + 55, 72, 20).build();
        guistate.put("button:button_increase_juice", buttonIncreaseEnergy);
        this.addRenderableWidget(buttonIncreaseEnergy);
    }
}
