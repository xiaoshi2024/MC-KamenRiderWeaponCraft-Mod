package com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * 平成嘿嘿剑能量显示渲染器
 * 用于在游戏界面上以自上而下的进度条样式显示武器的能量状态
 */
public class HeiseiswordEnergyRenderer {
    
    // 进度条HUD相关常量
    private static final int BAR_WIDTH = 64; // 进度条宽度
    private static final int BAR_HEIGHT = 64; // 进度条高度
    private static final int HUD_RIGHT_OFFSET = 20; // 从屏幕右侧的偏移量
    private static final int HUD_BOTTOM_OFFSET = 30; // 从屏幕底部的偏移量
    
    // 进度条HUD的资源位置（需要在资源包中添加对应的纹理）
    private static final ResourceLocation BAR_BACKGROUND = new ResourceLocation("kamen_rider_weapon_craft:textures/gui/heiseisword_bar_background.png");
    private static final ResourceLocation BAR_FILL = new ResourceLocation("kamen_rider_weapon_craft:textures/gui/heiseisword_bar_fill.png");
    private static final ResourceLocation BAR_OVERLAY = new ResourceLocation("kamen_rider_weapon_craft:textures/gui/heiseisword_bar_overlay.png");

    /**
     * 渲染能量的GUI覆盖层 - 自上而下的进度条样式显示
     */
    public static final IGuiOverlay RENDER_ENERGY_BAR = ((gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        
        // 只有当玩家手持平成嘿嘿剑时才渲染能量显示
        if (!(player.getMainHandItem().getItem() instanceof Heiseisword)) {
            return;
        }
        
        // 获取能量数据
        double currentEnergy = HeiseiswordEnergyManager.getCurrentEnergy(player);
        double maxEnergy = HeiseiswordEnergyManager.getMaxEnergy(player);
        
        // 计算能量百分比
        double energyPercentage = currentEnergy / maxEnergy;
        
        // 确保能量百分比在0-1之间
        energyPercentage = Math.max(0.0, Math.min(1.0, energyPercentage));
        
        // 设置渲染位置 - 在屏幕右侧、物品栏上方
        int barX = screenWidth - HUD_RIGHT_OFFSET - BAR_WIDTH;
        int barY = screenHeight - HUD_BOTTOM_OFFSET - BAR_HEIGHT;
        
        // 启用混合
        RenderSystem.enableBlend();
        
        // 渲染进度条背景
        try {
            guiGraphics.blit(BAR_BACKGROUND, barX, barY, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        } catch (Exception e) {
            // 如果纹理不存在，使用默认的背景色
            guiGraphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0x80000000); // 半透明黑色背景
        }
        
        // 计算填充高度（自上而下）
        int fillHeight = (int)(BAR_HEIGHT * energyPercentage);
        int fillY = barY + (BAR_HEIGHT - fillHeight); // 填充位置从底部开始向上
        
        // 渲染进度条填充
        if (fillHeight > 0) {
            try {
                // 自上而下渲染填充效果
                guiGraphics.blit(BAR_FILL, barX, fillY, 0, BAR_HEIGHT - fillHeight, BAR_WIDTH, fillHeight, BAR_WIDTH, BAR_HEIGHT);
            } catch (Exception e) {
                // 如果纹理不存在，使用默认的填充色
                guiGraphics.fill(barX, fillY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0x8000FF00); // 半透明绿色填充
            }
        }
        
        // 渲染进度条覆盖层（边框等）
        try {
            guiGraphics.blit(BAR_OVERLAY, barX, barY, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        } catch (Exception e) {
            // 如果覆盖层纹理不存在，绘制简单的边框
            guiGraphics.fill(barX, barY, barX + 1, barY + BAR_HEIGHT, 0xFFFFFFFF); // 左边框
            guiGraphics.fill(barX + BAR_WIDTH - 1, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFFFFFFFF); // 右边框
            guiGraphics.fill(barX, barY, barX + BAR_WIDTH, barY + 1, 0xFFFFFFFF); // 上边框
            guiGraphics.fill(barX, barY + BAR_HEIGHT - 1, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFFFFFFFF); // 下边框
            
            // 绘制能量百分比文本
            String percentageText = String.format("%.0f%%", energyPercentage * 100);
            guiGraphics.drawString(gui.getFont(), percentageText, 
                    barX + BAR_WIDTH / 2 - gui.getFont().width(percentageText) / 2, 
                    barY + BAR_HEIGHT / 2 - gui.getFont().lineHeight / 2, 0xFFFFFFFF, true);
        }
        
        RenderSystem.disableBlend();
    });
    
    // 注意：现在使用自上而下的进度条样式显示能量，不再需要文本颜色计算方法
}