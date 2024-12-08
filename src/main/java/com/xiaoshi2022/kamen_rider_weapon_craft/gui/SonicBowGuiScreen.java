package com.xiaoshi2022.kamen_rider_weapon_craft.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.SonicBowContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.awt.*;

public class SonicBowGuiScreen extends AbstractContainerScreen<SonicBowContainer> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("kamen_rider_weapon_craft", "textures/gui/sonic_bow_gui.png");
    private Button takeMelonButton;
    private Slot melonSlot;

    public SonicBowGuiScreen(SonicBowContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        //gui的宽度
        this.imageWidth = 176;
        //gui的高度
        this.imageHeight = 166;
        this.melonSlot = pMenu.getCustomSlot(); // 初始化 playerInventory 变量
    }

    @Override
    protected void init() {
        super.init();
        // 使用 Button.builder() 创建按钮
        this.takeMelonButton = Button.builder(Component.literal("T"), (p_97509_) -> {
                    // 按钮点击时执行的动作
                    Slot melonSlot = this.menu.getCustomSlot(); // 获取 melonSlot
                    //当玩家按下按钮槽内物品直接掉落到玩家脚下
                    if (!melonSlot.getItem().isEmpty()) {
                        ItemStack melonStack = melonSlot.getItem(); // 获取槽内物品
                        melonSlot.remove(melonStack.getCount()); // 移除槽内物品
                        this.minecraft.player.drop(melonStack, false); // 将物品掉落到玩家脚下
                    }
                })
                .build(); // 构建按钮
        this.addRenderableWidget(this.takeMelonButton); // 将按钮添加到 GUI
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
//        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();
    }

    private void renderProgress(GuiGraphics pGuiGraphics, int x, int y) {
        // 绘制进度条
        int progressWidth = 100; // 进度条的宽度
        int progressHeight = 10; // 进度条的高度
        pGuiGraphics.fill(x + 70, y + 20, x + 70 + progressWidth, y + 30, 0x80FF0000); // 绘制红色背景
        int progress = this.menu.getProgress();
        pGuiGraphics.fill(x + 70, y + 20, x + 70 + (progress * progressWidth / 100), y + 30, 0xFF00FF00); // 绘制绿色进度
    }

    // 更新进度条的方法，根据melon的放入情况更新
    public void updateProgress(int newProgress) {
        this.menu.updateProgress(newProgress);
    }
}