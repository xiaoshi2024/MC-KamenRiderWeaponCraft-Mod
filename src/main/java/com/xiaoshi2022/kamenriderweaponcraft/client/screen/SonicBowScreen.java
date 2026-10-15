package com.xiaoshi2022.kamenriderweaponcraft.client.screen;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.world.inventory.SonicBowMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 音速弓锁种装载界面（布局沿用 1.20.1：176x166，锁种槽 30,28）
 */
public class SonicBowScreen extends AbstractContainerScreen<SonicBowMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/gui/sonic_bow_gui.png");

    public SonicBowScreen(SonicBowMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 贴图实际尺寸就是 176x166（非标准 256x256 图集），纹理尺寸参数必须与之一致，
        // 否则会按 256 网格采样导致 GUI 被放大裁切
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0,
                this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 1.20.1 原版禁用了标签渲染，保持一致
    }
}
