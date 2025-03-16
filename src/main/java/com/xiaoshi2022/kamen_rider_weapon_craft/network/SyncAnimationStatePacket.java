package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class SyncAnimationStatePacket {
    private static final Logger LOGGER = LogManager.getLogger();
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
        return new SyncAnimationStatePacket(
                buffer.readBoolean(),
                buffer.readBlockPos()
        );
    }

    public static void handle(SyncAnimationStatePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                handleClient(packet);
            } else {
                LOGGER.warn("Received animation packet in wrong direction: {}", context.getDirection());
            }
        });
        context.setPacketHandled(true);
    }

    private static void handleClient(SyncAnimationStatePacket packet) {
        net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
        if (client == null) return;

        net.minecraft.world.level.Level level = client.level;
        if (level == null || !level.isLoaded(packet.pos)) {
            LOGGER.debug("Received animation packet for unloaded position: {}", packet.pos);
            return;
        }

        client.execute(() -> {
            try {
                BlockEntity be = level.getBlockEntity(packet.pos);
                if (be instanceof RiderFusionMachineBlockEntity fusionMachine) {
                    fusionMachine.shouldPlayEndAnimation = packet.shouldPlayEndAnimation;
                    fusionMachine.requestModelDataUpdate();
                    level.sendBlockUpdated(packet.pos, be.getBlockState(), be.getBlockState(), 3);
                }
            } catch (Exception e) {
                LOGGER.error("Error handling animation packet at {}: {}", packet.pos, e.toString());
            }
        });
    }
}