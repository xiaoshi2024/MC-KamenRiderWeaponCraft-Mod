package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HeiseiswordRiderSelectionPacket {
    private final boolean isXKeyDown;

    public HeiseiswordRiderSelectionPacket(boolean isXKeyDown) {
        this.isXKeyDown = isXKeyDown;
    }

    // 编码方法
    public static void encode(HeiseiswordRiderSelectionPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.isXKeyDown);
    }

    // 解码方法
    public static HeiseiswordRiderSelectionPacket decode(FriendlyByteBuf buf) {
        boolean isXKeyDown = buf.readBoolean();
        return new HeiseiswordRiderSelectionPacket(isXKeyDown);
    }

    // 处理方法
    public static void handle(HeiseiswordRiderSelectionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // 获取主手物品
            ItemStack stack = player.getMainHandItem();
            // 检查是否是Heiseisword
            if (stack.getItem().getClass().getSimpleName().equals("Heiseisword")) {
                // 调用实际的处理方法
                try {
                    // 使用反射调用Heiseisword中的服务端处理方法
                    Class<?> heiseiswordClass = Class.forName("com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword");
                    java.lang.reflect.Method handleSelectionMethod = heiseiswordClass.getDeclaredMethod("handleRiderSelectionOnServer", ServerPlayer.class, ItemStack.class, boolean.class);
                    handleSelectionMethod.setAccessible(true);
                    handleSelectionMethod.invoke(null, player, stack, msg.isXKeyDown);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public boolean isXKeyDown() {
        return isXKeyDown;
    }
}