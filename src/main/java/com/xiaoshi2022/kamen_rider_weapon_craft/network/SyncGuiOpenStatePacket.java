package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachineBlockEntity;
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
                net.minecraft.world.level.Level level = net.minecraft.client.Minecraft.getInstance().level;
                if (level != null) {
                    net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(packet.pos);
                    if (blockEntity instanceof RiderFusionMachineBlockEntity riderFusionMachineBlockEntity) {
                        riderFusionMachineBlockEntity.isGuiOpen = packet.isGuiOpen;
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}