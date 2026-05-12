package com.xiaoshi2022.kamenriderweaponcraft.network;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = KamenRiderWeaponCraft.MODID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {

    private static boolean registered = false;

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        if (registered) return;

        final PayloadRegistrar registrar = event.registrar(KamenRiderWeaponCraft.MODID);

        // 注册骑士选择数据包
        registrar.playBidirectional(
                HeiseiswordRiderSelectionPacket.TYPE,
                HeiseiswordRiderSelectionPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        HeiseiswordRiderSelectionPacket::handleClient,
                        HeiseiswordRiderSelectionPacket::handleServer
                )
        );

        // 注册能量同步数据包 (仅服务端到客户端)
        registrar.playToClient(
                HeiseiswordEnergySyncPacket.TYPE,
                HeiseiswordEnergySyncPacket.STREAM_CODEC,
                HeiseiswordEnergySyncPacket::handleClient
        );

        registered = true;
        KamenRiderWeaponCraft.LOGGER.info("NetworkHandler registered successfully");
    }

    // 发送到服务器
    public static <T extends CustomPacketPayload> void sendToServer(T packet) {
        PacketDistributor.sendToServer(packet);
    }

    // 发送到客户端
    public static <T extends CustomPacketPayload> void sendToClient(T packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}