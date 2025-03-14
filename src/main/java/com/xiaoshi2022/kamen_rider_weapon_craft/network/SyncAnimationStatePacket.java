package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncAnimationStatePacket {
    private final boolean shouldPlayEndAnimation;
    private final BlockPos pos;

    public SyncAnimationStatePacket(boolean shouldPlayEndAnimation, BlockPos pos) {
        this.shouldPlayEndAnimation = shouldPlayEndAnimation;
        this.pos = pos;
    }

    public static void encode(SyncAnimationStatePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.shouldPlayEndAnimation);
        buffer.writeBlockPos(packet.pos);
    }

    public static SyncAnimationStatePacket decode(FriendlyByteBuf buffer) {
        boolean shouldPlayEndAnimation = buffer.readBoolean();
        BlockPos pos = buffer.readBlockPos();
        return new SyncAnimationStatePacket(shouldPlayEndAnimation, pos);
    }

    public static void handle(SyncAnimationStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() == null) { // 客户端处理
                net.minecraft.world.level.Level level = net.minecraft.client.Minecraft.getInstance().level;
                if (level != null) {
                    net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(packet.pos);
                    if (blockEntity instanceof RiderFusionMachineBlockEntity riderFusionMachineBlockEntity) {
                        riderFusionMachineBlockEntity.shouldPlayEndAnimation = packet.shouldPlayEndAnimation;
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}