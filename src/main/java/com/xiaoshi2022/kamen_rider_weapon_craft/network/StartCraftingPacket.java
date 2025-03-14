package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StartCraftingPacket {
    private final BlockPos pos;

    public StartCraftingPacket(BlockPos pos) {
        this.pos = pos;
    }

    public StartCraftingPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    public static StartCraftingPacket decode(FriendlyByteBuf buffer) {
        return new StartCraftingPacket(buffer);
    }

    public static void handle(StartCraftingPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    Level level = player.level();
                    if (level == null) {
                        return;
                    }

                    BlockEntity blockEntity = level.getBlockEntity(packet.pos);
                    if (blockEntity instanceof RiderFusionMachineBlockEntity fusionMachine) {
                        fusionMachine.startCrafting();
                    } else {
                        System.err.println("Block entity at position " + packet.pos + " is not a RiderFusionMachineBlockEntity");
                    }
                } else {
                    System.err.println("Player is null in StartCraftingPacket.handle");
                }
            } else {
                System.err.println("Packet direction is not PLAY_TO_SERVER");
            }
        });
        ctx.get().setPacketHandled(true);
    }
}