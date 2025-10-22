package com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;

/**
 * 平成嘿嘿剑能量显示渲染器
 * 用于在游戏界面上以纯数字方式显示武器的能量状态
 * Fabric 1.21.6版本
 */
public class HeiseiswordEnergyRenderer implements HudRenderCallback {

    private static final HeiseiswordEnergyRenderer INSTANCE = new HeiseiswordEnergyRenderer();

    public static void register() {
        HudRenderCallback.EVENT.register(INSTANCE);
    }

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

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        // 获取主手物品
        ItemStack mainHandStack = player.getMainHandStack();
        
        // 只有当玩家手持平成嘿嘿剑时才渲染能量显示
        if (!(mainHandStack.getItem() instanceof com.xiaoshi2022.kamen_rider_weapon_craft.items.custom.Heiseisword)) {
            return;
        }

        // 获取能量数据
        double currentEnergy = HeiseiswordEnergyManager.getCurrentEnergy(player);
        double maxEnergy = HeiseiswordEnergyManager.getMaxEnergy(player);

        // 如果最大能量为0，则不显示
        if (maxEnergy <= 0) return;

        // 设置渲染位置 - 纯数字显示在屏幕底部中央
        int screenWidth = drawContext.getScaledWindowWidth();
        int screenHeight = drawContext.getScaledWindowHeight();
        int y = screenHeight - 20;

        // 渲染能量数值文本
        String energyText = String.format("平成嘿嘿剑能量: %.0f/%.0f", currentEnergy, maxEnergy);
        TextRenderer textRenderer = client.textRenderer;
        int textX = (screenWidth - textRenderer.getWidth(energyText)) / 2;

        // 在Fabric 1.21.6中，不再需要显式的混合模式设置
        // 文本渲染会自动处理必要的混合操作

        // 根据能量百分比确定文本颜色
        double energyPercentage = currentEnergy / maxEnergy;
        int color = getTextColor(energyPercentage);

        // 使用Text.literal创建文本组件并渲染
        drawContext.drawText(textRenderer, Text.literal(energyText), textX, y, color, false);

        // 在Fabric 1.21.6中，不再需要显式禁用混合模式
        // 渲染状态会在下一帧自动重置
    }
}