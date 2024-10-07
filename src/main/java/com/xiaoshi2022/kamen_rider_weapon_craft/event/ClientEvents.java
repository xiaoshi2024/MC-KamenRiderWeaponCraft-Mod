package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.prop.arrowx.AonicxEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.gui.OpenLockseedGuiScreen;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModMenus;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.OpenLockseedGuiMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.nbt.CompoundTag;

import static net.minecraft.world.InteractionResult.SUCCESS;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.arrowx.AonicxRenderer;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID,value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (KeyBinding.CHANGE_KEY.consumeClick()) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("hhhh"));
            }
            if (KeyBinding.OPEN_LOCKSEED.consumeClick()) {
                Player player = Minecraft.getInstance().player;
                // 检查玩家副手是否持有sonicarrow并打开lockseedGUI
                if (player.getOffhandItem().getItem() == ModItems.SONICARROW.get()) {
                    // 打开lockseedGUI
                    OpenLockseedGuiMenu.open(player);
                }
            }
        }


        @SubscribeEvent
        public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
            if (event.getHand() == InteractionHand.OFF_HAND) {
                ItemStack mainHandItem = event.getEntity().getMainHandItem();
                ItemStack offHandItem = event.getEntity().getOffhandItem();

                // 检查副手物品是否为"sonicarrow"，并且主手物品是否为"melon"
                if (mainHandItem.getItem() == ModItems.MELON.get() && offHandItem.getItem() == ModItems.SONICARROW.get()) {

                    //把sonicarrow替换为sonicarrow-melon
                    event.getEntity().setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.SONICARROW_MELON.get()));
                    // 改变sonicarrow的名称

//                    mainHandItem.setHoverName(Component.literal("sonicarrow-melon"));

                    // 消耗一个"melon"
                    mainHandItem.shrink(1);

                    // 复制sonicarrow的NBT耐久和附魔到新sonicarrow-melon
                    CompoundTag offHandTag = offHandItem.getTag();

                    if (offHandTag != null) {

                        ItemStack newItem = new ItemStack(ModItems.SONICARROW_MELON.get());
                        newItem.setTag(offHandTag);
                        event.getEntity().setItemInHand(InteractionHand.OFF_HAND, newItem);

                        // 取消事件

                        event.setCancellationResult(SUCCESS);

                        event.setCanceled(true);
                    }

                    event.setCancellationResult(SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID, value = Dist.CLIENT,bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents{
     @SubscribeEvent
     public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event){
         event.registerEntityRenderer(AonicxEntity.SONICX_ARROW.get(),AonicxRenderer::new);
     }
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event){
        event.register(KeyBinding.CHANGE_KEY);
        event.register(KeyBinding.OPEN_LOCKSEED);
     }
     @SubscribeEvent
     public static void onClientSetup(FMLClientSetupEvent event) {
         MenuScreens.register(ModMenus.OPEN_LOCKSEED_GUI.get(), OpenLockseedGuiScreen::new);
         }
    }
}

