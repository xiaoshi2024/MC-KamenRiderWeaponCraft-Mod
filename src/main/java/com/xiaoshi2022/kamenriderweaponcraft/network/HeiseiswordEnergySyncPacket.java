package com.xiaoshi2022.kamenriderweaponcraft.network;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.rider.energy.HeiseiswordEnergyManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record HeiseiswordEnergySyncPacket(UUID playerUUID, double currentEnergy, double maxEnergy) implements CustomPacketPayload {
    public static final Type<HeiseiswordEnergySyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "energy_sync"));

    // 修复 UUID 编解码 - 使用字符串转换
    public static final StreamCodec<ByteBuf, HeiseiswordEnergySyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8.map(
                            uuidStr -> UUID.fromString(uuidStr),
                            UUID::toString
                    ), HeiseiswordEnergySyncPacket::playerUUID,
                    ByteBufCodecs.DOUBLE, HeiseiswordEnergySyncPacket::currentEnergy,
                    ByteBufCodecs.DOUBLE, HeiseiswordEnergySyncPacket::maxEnergy,
                    HeiseiswordEnergySyncPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(HeiseiswordEnergySyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            HeiseiswordEnergyManager.CLIENT_ENERGY_DATA.put(
                    packet.playerUUID(),
                    new HeiseiswordEnergyManager.EnergyData(packet.currentEnergy(), packet.maxEnergy())
            );
        });
    }
}