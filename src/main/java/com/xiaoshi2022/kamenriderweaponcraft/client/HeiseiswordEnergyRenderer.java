package com.xiaoshi2022.kamenriderweaponcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.rider.energy.HeiseiswordEnergyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = KamenRiderWeaponCraft.MODID, value = Dist.CLIENT)
public class HeiseiswordEnergyRenderer {

    private static final int BAR_WIDTH = 64;
    private static final int BAR_HEIGHT = 64;
    private static final int HUD_RIGHT_OFFSET = 20;
    private static final int HUD_BOTTOM_OFFSET = 30;

    private static final ResourceLocation BAR_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/gui/heiseisword_bar_background.png");
    private static final ResourceLocation BAR_FILL =
            ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/gui/heiseisword_bar_fill.png");
    private static final ResourceLocation BAR_OVERLAY =
            ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "textures/gui/heiseisword_bar_overlay.png");

    @SubscribeEvent
    public static void registerHud(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "energy_bar"),
                (guiGraphics, partialTick) -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    LocalPlayer player = minecraft.player;
                    if (player == null) return;

                    if (!(player.getMainHandItem().getItem() instanceof Heiseisword)) return;

                    double currentEnergy = HeiseiswordEnergyManager.getCurrentEnergy(player);
                    double maxEnergy = HeiseiswordEnergyManager.getMaxEnergy(player);

                    if (maxEnergy <= 0) return;

                    double energyPercentage = Math.max(0.0, Math.min(1.0, currentEnergy / maxEnergy));

                    int screenWidth = minecraft.getWindow().getGuiScaledWidth();
                    int screenHeight = minecraft.getWindow().getGuiScaledHeight();

                    int barX = screenWidth - HUD_RIGHT_OFFSET - BAR_WIDTH;
                    int barY = screenHeight - HUD_BOTTOM_OFFSET - BAR_HEIGHT;

                    RenderSystem.enableBlend();

                    // 渲染背景
                    renderTextureSafe(guiGraphics, BAR_BACKGROUND, barX, barY, BAR_WIDTH, BAR_HEIGHT);

                    // 渲染填充（自上而下）
                    int fillHeight = (int)(BAR_HEIGHT * energyPercentage);
                    if (fillHeight > 0) {
                        int fillY = barY + (BAR_HEIGHT - fillHeight);
                        renderTextureRegionSafe(guiGraphics, BAR_FILL, barX, fillY, 0, BAR_HEIGHT - fillHeight,
                                BAR_WIDTH, fillHeight, BAR_WIDTH, BAR_HEIGHT);
                    }

                    // 渲染覆盖层
                    renderTextureSafe(guiGraphics, BAR_OVERLAY, barX, barY, BAR_WIDTH, BAR_HEIGHT);

                    RenderSystem.disableBlend();
                });
    }

    private static void renderTextureSafe(GuiGraphics guiGraphics, ResourceLocation texture,
                                          int x, int y, int width, int height) {
        try {
            guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
        } catch (Exception e) {
            // 纹理不存在时绘制边框
            guiGraphics.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
            guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFFFFFFFF);
            guiGraphics.fill(x, y, x + width, y + 1, 0xFFFFFFFF);
            guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFFFFFFFF);
        }
    }

    private static void renderTextureRegionSafe(GuiGraphics guiGraphics, ResourceLocation texture,
                                                int x, int y, int u, int v, int width, int height,
                                                int textureWidth, int textureHeight) {
        try {
            guiGraphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        } catch (Exception e) {
            guiGraphics.fill(x, y, x + width, y + height, 0x8000FF00);
        }
    }
}