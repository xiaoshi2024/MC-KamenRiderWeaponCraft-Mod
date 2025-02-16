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

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // 在服务端启动合成
            if (context.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ServerPlayer player = context.get().getSender();
                if (player != null) {
                    Level level = player.level();
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof RiderFusionMachineBlockEntity fusionMachine) {
                        fusionMachine.startCrafting();
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}