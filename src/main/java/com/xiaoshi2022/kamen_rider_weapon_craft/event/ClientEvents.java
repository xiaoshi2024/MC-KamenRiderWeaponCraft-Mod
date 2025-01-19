package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.prop.arrowx.AonicxEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.custom.Melon;
import com.xiaoshi2022.kamen_rider_weapon_craft.gui.SonicBowGuiScreen;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding;
import com.xiaoshi2022.kamen_rider_weapon_craft.weapon_mapBOOK.weapon_map;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.SonicBowContainer;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.nbt.CompoundTag;

import static com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding.CHANGE_KEY;
import static com.xiaoshi2022.kamen_rider_weapon_craft.util.KeyBinding.OPEN_LOCKSEED;
import static net.minecraft.world.InteractionResult.SUCCESS;
import com.xiaoshi2022.kamen_rider_weapon_craft.Item.prop.client.arrowx.AonicxRenderer;


public class ClientEvents {
    @Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID,value = Dist.CLIENT)
    public static class ClientForgeEvents {
//        @SubscribeEvent
//        public static void onKeyInput(InputEvent.Key event) {
//            if (KeyBinding.CHANGE_KEY.consumeClick()) {
//                Minecraft.getInstance().player.sendSystemMessage(Component.literal("hhhh"));
//            }
    //        }
    }


    @Mod.EventBusSubscriber(modid = kamen_rider_weapon_craft.MOD_ID, value = Dist.CLIENT,bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents{
     @SubscribeEvent
     public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event){
         event.registerEntityRenderer(AonicxEntity.SONICX_ARROW.get(),AonicxRenderer::new);
     }
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event){
        event.register(CHANGE_KEY);
        event.register(OPEN_LOCKSEED);
     }

    }
}

