package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class HeiseiswordEnergySyncPacket {
    private final UUID playerUUID;
    private final double currentEnergy;
    private final double maxEnergy;

    public HeiseiswordEnergySyncPacket(Player player, double currentEnergy, double maxEnergy) {
        this.playerUUID = player.getUUID();
        this.currentEnergy = currentEnergy;
        this.maxEnergy = maxEnergy;
    }

    // 编码方法
    public static void encode(HeiseiswordEnergySyncPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerUUID);
        buf.writeDouble(msg.currentEnergy);
        buf.writeDouble(msg.maxEnergy);
    }

    // 解码方法
    public static HeiseiswordEnergySyncPacket decode(FriendlyByteBuf buf) {
        UUID playerUUID = buf.readUUID();
        double currentEnergy = buf.readDouble();
        double maxEnergy = buf.readDouble();
        // 创建一个临时对象，稍后在handle中使用
        return new HeiseiswordEnergySyncPacket(playerUUID, currentEnergy, maxEnergy);
    }

    // 私有构造函数用于解码
    private HeiseiswordEnergySyncPacket(UUID playerUUID, double currentEnergy, double maxEnergy) {
        this.playerUUID = playerUUID;
        this.currentEnergy = currentEnergy;
        this.maxEnergy = maxEnergy;
    }

    // 处理方法
    public static void handle(HeiseiswordEnergySyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // 只在客户端处理
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                // 在客户端更新玩家的能量值
                updateClientEnergy(msg.playerUUID, msg.currentEnergy, msg.maxEnergy);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    // 更新客户端能量值的方法
    private static void updateClientEnergy(UUID playerUUID, double currentEnergy, double maxEnergy) {
        // 在客户端找到对应的玩家
        Player player = net.minecraft.client.Minecraft.getInstance().level.getPlayerByUUID(playerUUID);
        if (player != null) {
            // 直接使用HeiseiswordEnergyManager的setCurrentEnergy方法更新客户端能量值
            com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy.HeiseiswordEnergyManager.setCurrentEnergy(player, currentEnergy);
            com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy.HeiseiswordEnergyManager.setMaxEnergy(player, maxEnergy);
        }
    }
}