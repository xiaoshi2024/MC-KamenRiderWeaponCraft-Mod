package com.xiaoshi2022.kamen_rider_weapon_craft.gui;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.sonicarrow;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.SonicBowContainer;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraftforge.items.IItemHandler;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.HashMap;

public class SonicBowGuiScreen extends AbstractContainerScreen<SonicBowContainer> {
    private final static HashMap<String, Object> guistate = SonicBowContainer.guistate;
    private final Player entity;
    Button buttonIncreaseEnergy;

    private int progressTicks = 0; // 当前进度
    private static final int MAX_PROGRESS_TICKS = 40; // 2 秒完成（每秒 20 tick）
    private boolean isProgressing = false; // 是否正在渲染进度条

    public SonicBowGuiScreen(SonicBowContainer container, Inventory inventory, Component text) {
        super(container, inventory, text);
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

        // 更新进度条
        if (isProgressing) {
            progressTicks++;
            if (progressTicks >= MAX_PROGRESS_TICKS) {
                progressTicks = MAX_PROGRESS_TICKS;
                isProgressing = false;

                // 进度条完成后关闭 GUI
                this.minecraft.player.closeContainer();

                // 将空槽位的数据传递给左手武器
                ItemStack offhandStack = entity.getOffhandItem();
                if (offhandStack.getItem() == ModItems.SONICARROW.get()) {
                    CompoundTag tag = offhandStack.getOrCreateTag();
                    tag.put("Inventory", this.menu.internal.serializeNBT()); // 更新 NBT 数据
                    offhandStack.setTag(tag);

                    // 检查左手武器的存储槽位数据是否为空
                    if (this.menu.internal.getStackInSlot(0).isEmpty()) {
                        // 切换武器模式
                        sonicarrow weapon = (sonicarrow) offhandStack.getItem();
                        weapon.switchMode(offhandStack, sonicarrow.Mode.MELON);
                        entity.displayClientMessage(Component.literal("Switched to Melon Mode"), true);
                    }
                }
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();

        // 渲染输入槽位的物品
        IItemHandler internal = this.menu.internal;
        renderInventory(guiGraphics, internal);

        // 渲染果汁进度条
        renderJuiceBar(guiGraphics);
    }

    private void renderInventory(GuiGraphics guiGraphics, IItemHandler internal) {
        for (int i = 0; i < internal.getSlots(); i++) {
            ItemStack stack = internal.getStackInSlot(i);
            if (!stack.isEmpty()) {
                int x = this.leftPos + 30; // 输入槽位的 X 坐标
                int y = this.topPos + 28; // 输入槽位的 Y 坐标
                guiGraphics.renderItem(stack, x, y); // 渲染物品
            }
        }
    }

    private void renderJuiceBar(GuiGraphics guiGraphics) {
        int x = this.leftPos + 70; // 进度条的 X 坐标
        int y = this.topPos + 20;  // 进度条的 Y 坐标
        int width = 100;           // 进度条的总宽度
        int height = 10;           // 进度条的高度

        // 渲染空白边框
        guiGraphics.blit(new ResourceLocation("kamen_rider_weapon_craft:textures/screens/sonic_bow_bar.png"),
                        x, y, 0, 0, width, height, width, height);

        // 计算填充比例
        float fillRatio = (float) progressTicks / MAX_PROGRESS_TICKS;
        int filledWidth = (int) (width * fillRatio);

        // 渲染填充条
        guiGraphics.blit(new ResourceLocation("kamen_rider_weapon_craft:textures/screens/sonic_bow_bar_1.png"),
                        x, y, 0, 0, filledWidth, height, width, height);
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
        // 留空，禁用默认的标签和槽位渲染
    }

    @Override
    public void init() {
        super.init();
        buttonIncreaseEnergy = Button.builder(Component.translatable("gui.cs.ssonic.button_increase_juice"), e -> {
            // 清除输入槽位的物品
            ItemStack inputStack = this.menu.internal.getStackInSlot(0);
            if (!inputStack.isEmpty()) {
                this.menu.internal.setStackInSlot(0, ItemStack.EMPTY);

                // 清除左手武器的存储槽位数据
                ItemStack offhandStack = entity.getOffhandItem();
                if (offhandStack.getItem() == ModItems.SONICARROW.get()) {
                    CompoundTag tag = offhandStack.getOrCreateTag();
                    tag.put("Inventory", new CompoundTag()); // 清除存储槽位数据
                    offhandStack.setTag(tag);

                    // 更新左手物品的存储槽位数据为清空
                    this.menu.internal.deserializeNBT(new CompoundTag());

                    // 开始进度条动画
                    isProgressing = true;
                    progressTicks = 0;
                }
            }
        }).bounds(this.leftPos + 79, this.topPos + 55, 72, 20).build();
        guistate.put("button:button_increase_juice", buttonIncreaseEnergy);
        this.addRenderableWidget(buttonIncreaseEnergy);
    }
}
