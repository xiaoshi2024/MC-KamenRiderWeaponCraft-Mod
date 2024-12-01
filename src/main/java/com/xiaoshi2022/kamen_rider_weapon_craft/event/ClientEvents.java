package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.prop.arrowx.AonicxEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.gui.SonicBowGuiScreen;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.SonicBowContainer;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.nbt.CompoundTag;

import static com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding.OPEN_LOCKSEED;
import static net.minecraft.world.InteractionResult.SUCCESS;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.arrowx.AonicxRenderer;
import org.jetbrains.annotations.Nullable;


public class ClientEvents {
    @Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID,value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (KeyBinding.CHANGE_KEY.consumeClick()) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("hhhh"));
            }
            // 如果副手为音速弓且按下y打开GUI
            if (OPEN_LOCKSEED.consumeClick()) {
                Minecraft minecraft = Minecraft.getInstance();
                Player player = minecraft.player;
                if (player != null && player.getOffhandItem().getItem() == ModItems.SONICARROW.get()) {
                    // 创建一个新的SonicBowContainer实例，传递三个参数
                    SonicBowContainer container = new SonicBowContainer(0, player.getInventory(), new FriendlyByteBuf(Unpooled.buffer()));
                    // 使用Minecraft.setScreen打开GUI
                    minecraft.setScreen(new SonicBowGuiScreen(
                            container, player.getInventory(), Component.literal("Sonic Bow")));
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

    }
}

