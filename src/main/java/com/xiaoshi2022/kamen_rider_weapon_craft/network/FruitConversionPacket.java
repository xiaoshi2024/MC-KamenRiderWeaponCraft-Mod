package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import com.xiaoshi2022.kamen_rider_weapon_craft.event.CommonEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FruitConversionPacket {
    private final InteractionHand hand;

    public FruitConversionPacket(InteractionHand hand) {
        this.hand = hand;
    }

    public static void encode(FruitConversionPacket msg, FriendlyByteBuf buffer) {
        buffer.writeEnum(msg.hand);
    }

    public static FruitConversionPacket decode(FriendlyByteBuf buffer) {
        return new FruitConversionPacket(buffer.readEnum(InteractionHand.class));
    }

    public static void handle(FruitConversionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                ServerPlayer player = ctx.get().getSender();
                if (player != null) {
                    // 调用CommonEvents中的处理方法
                    CommonEvents.handleFruitConversion(player, msg.hand);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}