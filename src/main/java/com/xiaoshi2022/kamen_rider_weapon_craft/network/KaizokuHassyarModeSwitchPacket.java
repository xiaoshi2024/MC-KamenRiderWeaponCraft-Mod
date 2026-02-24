package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.KaizokuHassyar;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class KaizokuHassyarModeSwitchPacket {
    private final KaizokuHassyar.Mode newMode;

    public KaizokuHassyarModeSwitchPacket(KaizokuHassyar.Mode newMode) {
        this.newMode = newMode;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(newMode.name());
    }

    public static KaizokuHassyarModeSwitchPacket decode(FriendlyByteBuf buffer) {
        String modeName = buffer.readUtf();
        try {
            return new KaizokuHassyarModeSwitchPacket(KaizokuHassyar.Mode.valueOf(modeName));
        } catch (IllegalArgumentException e) {
            return new KaizokuHassyarModeSwitchPacket(KaizokuHassyar.Mode.LOCAL_TRAIN);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ItemStack mainHandStack = player.getMainHandItem();
                if (mainHandStack.getItem() instanceof KaizokuHassyar weapon) {
                    weapon.switchMode(mainHandStack, newMode, player);
                }
                ItemStack offhandStack = player.getOffhandItem();
                if (offhandStack.getItem() instanceof KaizokuHassyar weapon) {
                    weapon.switchMode(offhandStack, newMode, player);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
