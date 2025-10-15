package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HeiseiswordRiderSelectionPacket {
    // 不需要额外数据，因为服务端会根据玩家手持的物品来处理

    public HeiseiswordRiderSelectionPacket() {}

    // 编码方法
    public static void encode(HeiseiswordRiderSelectionPacket msg, FriendlyByteBuf buf) {
        // 写入一个哑字节以避免Forge崩溃
        buf.writeByte(0);
    }

    // 解码方法
    public static HeiseiswordRiderSelectionPacket decode(FriendlyByteBuf buf) {
        buf.readByte(); // 读取哑字节
        return new HeiseiswordRiderSelectionPacket();
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
                // 调用实际的处理方法（我们稍后会修改Heiseisword类添加这个方法）
                try {
                    // 使用反射调用Heiseisword中的服务端处理方法
                    Class<?> heiseiswordClass = Class.forName("com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.Heiseisword");
                    java.lang.reflect.Method handleSelectionMethod = heiseiswordClass.getDeclaredMethod("handleRiderSelectionOnServer", ServerPlayer.class, ItemStack.class);
                    handleSelectionMethod.setAccessible(true);
                    handleSelectionMethod.invoke(null, player, stack);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}