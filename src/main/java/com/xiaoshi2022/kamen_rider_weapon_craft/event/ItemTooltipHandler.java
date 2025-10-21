package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * 为模组中所有物品添加不同用途提示的事件处理器
 * 使用语言文件管理不同物品的提示文本
 */
@Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltips = event.getToolTip();
        TooltipFlag flag = event.getFlags();

        // 检查物品是否属于当前模组
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null && kamen_rider_weapon_craft.MOD_ID.equals(itemId.getNamespace())) {

            // 构建物品特定的提示翻译键
            // 格式: tooltip.kamen_rider_weapon_craft.[物品ID].usage
            String tooltipKey = "tooltip." + kamen_rider_weapon_craft.MOD_ID + "." + itemId.getPath() + ".usage";

            // 添加基于翻译键的物品用途提示
            Component tooltip = Component.translatable(tooltipKey);
            if (!tooltip.getString().equals(tooltipKey)) {
                // 如果找到了翻译，则添加提示（金色斜体）
                tooltips.add(Component.literal(""));
                tooltips.add(Component.literal("「Kamen Rider Weapon」").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
                tooltips.add(((net.minecraft.network.chat.MutableComponent) tooltip).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
            }
        }
    }
}