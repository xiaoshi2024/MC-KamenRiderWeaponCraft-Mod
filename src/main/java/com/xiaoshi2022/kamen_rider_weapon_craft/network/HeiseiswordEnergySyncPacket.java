package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Method;
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
        // 使用DistExecutor确保只在客户端执行
        net.minecraftforge.fml.DistExecutor.runWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
            try {
                // 直接更新客户端专用存储，使用同步包中的UUID，避免检查是否是当前客户端玩家
                // 这样可以确保所有玩家的能量数据都被正确更新，包括当前客户端玩家
                com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy.HeiseiswordEnergyManager.CLIENT_ENERGY_DATA.put(
                    playerUUID, 
                    new com.xiaoshi2022.kamen_rider_weapon_craft.rider.energy.HeiseiswordEnergyManager.EnergyData(currentEnergy, maxEnergy)
                );
            } catch (Exception e) {
                // 打印错误信息，便于调试
                e.printStackTrace();
            }
        });
    }
}