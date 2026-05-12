package com.xiaoshi2022.kamenriderweaponcraft.network;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record HeiseiswordRiderSelectionPacket(boolean isXKeyDown) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HeiseiswordRiderSelectionPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "rider_selection"));

    public static final StreamCodec<ByteBuf, HeiseiswordRiderSelectionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    HeiseiswordRiderSelectionPacket::isXKeyDown,
                    HeiseiswordRiderSelectionPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(HeiseiswordRiderSelectionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer && player.getMainHandItem().getItem() instanceof Heiseisword) {
                Heiseisword.handleRiderSelectionOnServer(serverPlayer, player.getMainHandItem(), packet.isXKeyDown());
            }
        });
    }

    public static void handleClient(HeiseiswordRiderSelectionPacket packet, IPayloadContext context) {
        // 客户端处理（如果需要）
    }
}