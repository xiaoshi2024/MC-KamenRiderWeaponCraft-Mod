package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import com.xiaoshi2022.kamen_rider_weapon_craft.weapon_mapBOOK.weapon_map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CloseMapPacket {
    public static void encode(CloseMapPacket msg, FriendlyByteBuf buffer) {
        // 编码数据到缓冲区
    }

    public static CloseMapPacket decode(FriendlyByteBuf buffer) {
        // 从缓冲区解码数据
        return new CloseMapPacket();
    }

    public static void handle(CloseMapPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof weapon_map) {
                    ((weapon_map) stack.getItem()).triggerCloseAnimation(player, stack);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}