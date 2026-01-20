package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.entity.RiderFusionMachineBlockEntity;
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
        net.minecraftforge.fml.DistExecutor.runWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () -> {
            try {
                // 使用反射获取Minecraft实例，避免直接引用客户端类
                Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
                Object minecraftInstance = minecraftClass.getMethod("getInstance").invoke(null);
                if (minecraftInstance == null) return;

                // 获取客户端世界
                Object levelObj = minecraftClass.getMethod("level").invoke(minecraftInstance);
                if (levelObj == null) {
                    LOGGER.debug("Received animation packet but client level is null");
                    return;
                }
                net.minecraft.world.level.Level level = (net.minecraft.world.level.Level) levelObj;

                if (!level.isLoaded(packet.pos)) {
                    LOGGER.debug("Received animation packet for unloaded position: {}", packet.pos);
                    return;
                }

                // 使用客户端线程执行
                minecraftClass.getMethod("execute", Runnable.class).invoke(minecraftInstance, (Runnable) () -> {
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
            } catch (Exception e) {
                LOGGER.error("Failed to handle SyncAnimationStatePacket client-side: {}", e.getMessage());
            }
        });
    }
}