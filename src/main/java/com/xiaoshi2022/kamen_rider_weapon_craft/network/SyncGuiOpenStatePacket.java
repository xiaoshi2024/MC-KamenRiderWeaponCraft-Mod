package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.entity.RiderFusionMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncGuiOpenStatePacket {
    private final boolean isGuiOpen;
    private final BlockPos pos;

    public SyncGuiOpenStatePacket(boolean isGuiOpen, BlockPos pos) {
        this.isGuiOpen = isGuiOpen;
        this.pos = pos;
    }

    public static void encode(SyncGuiOpenStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.isGuiOpen);
        buffer.writeBlockPos(packet.pos);
    }

    public static SyncGuiOpenStatePacket decode(FriendlyByteBuf buffer) {
        boolean isGuiOpen = buffer.readBoolean();
        BlockPos pos = buffer.readBlockPos();
        return new SyncGuiOpenStatePacket(isGuiOpen, pos);
    }

    public static void handle(SyncGuiOpenStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                net.minecraftforge.fml.DistExecutor.runWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
                    try {
                        // 使用反射获取Minecraft实例，避免直接引用客户端类
                        Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
                        Object minecraftInstance = minecraftClass.getMethod("getInstance").invoke(null);
                        if (minecraftInstance == null) return;

                        // 获取客户端世界
                        Object levelObj = minecraftClass.getMethod("level").invoke(minecraftInstance);
                        if (levelObj == null) return;

                        net.minecraft.world.level.Level level = (net.minecraft.world.level.Level) levelObj;
                        net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(packet.pos);
                        if (blockEntity instanceof RiderFusionMachineBlockEntity riderFusionMachineBlockEntity) {
                            riderFusionMachineBlockEntity.isGuiOpen = packet.isGuiOpen;
                        }
                    } catch (Exception e) {
                        // 忽略客户端相关错误，在服务器端不会执行
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}