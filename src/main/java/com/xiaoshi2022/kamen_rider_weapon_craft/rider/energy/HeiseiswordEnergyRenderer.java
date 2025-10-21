package com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

/**
 * 平成嘿嘿剑能量显示渲染器
 * 用于在游戏界面上以纯数字方式显示武器的能量状态
 */
public class HeiseiswordEnergyRenderer {
    
    /**
     * 渲染能量的GUI覆盖层 - 纯数字显示
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
        
        // 设置渲染位置 - 纯数字显示在屏幕底部中央
        int y = screenHeight - 20;
        
        // 渲染能量数值文本
        String energyText = String.format("平成嘿嘿剑能量: %.0f/%.0f", currentEnergy, maxEnergy);
        int textX = (screenWidth - gui.getFont().width(energyText)) / 2;
        
        // 启用混合并渲染文本
        RenderSystem.enableBlend();
        
        // 根据能量百分比确定文本颜色
        double energyPercentage = currentEnergy / maxEnergy;
        int color = getTextColor(energyPercentage);
        
        guiGraphics.drawString(gui.getFont(), energyText, textX, y, color, true);
        
        RenderSystem.disableBlend();
    });
    
    /**
     * 根据能量百分比返回对应的文本颜色
     */
    private static int getTextColor(double percentage) {
        if (percentage >= 0.7) {
            // 高能量：绿色
            return 0x00FF00; // 绿色
        } else if (percentage >= 0.3) {
            // 中能量：黄色
            return 0xFFFF00; // 黄色
        } else {
            // 低能量：红色
            return 0xFF0000; // 红色
        }
    }
}