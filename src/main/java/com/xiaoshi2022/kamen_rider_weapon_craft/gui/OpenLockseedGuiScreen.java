package com.xiaoshi2022.kamen_rider_weapon_craft.gui;

import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.OpenLockseedGuiMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;


import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

public class OpenLockseedGuiScreen extends AbstractContainerScreen<OpenLockseedGuiMenu> {
    private final static HashMap<String, Object> guistate = OpenLockseedGuiMenu.guistate;
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    Button button_button_lock;

    public OpenLockseedGuiScreen(OpenLockseedGuiMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    private static final ResourceLocation texture = new ResourceLocation("kamen_rider_weapon_craft:textures/screens/open_lockseed_gui.png");

    public OpenLockseedGuiScreen(Player player) {
        return new OpenLockseedGuiMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(_bpos));

    }


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
        guiGraphics.drawString(this.font, Component.translatable("gui.kamen_rider_weapon_craft.open_lockseed_gui.label_energy"), 42, 43, -13434880, false);
    }

    @Override
    public void init() {
        super.init();
        button_button_lock = Button.builder(Component.translatable("gui.kamen_rider_weapon_craft.open_lockseed_gui.button_button_lock"), e -> {
        }).bounds(this.leftPos + 87, this.topPos + 16, 82, 20).build();
        guistate.put("button:button_button_lock", button_button_lock);
        this.addRenderableWidget(button_button_lock);
    }
}
