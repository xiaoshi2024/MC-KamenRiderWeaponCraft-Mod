package com.xiaoshi2022.kamen_rider_weapon_craft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Supplier;

public class DrawMusouSaberPacket {

    public DrawMusouSaberPacket() {}

    /* 关键：写/读 1 字节，Forge 才不会崩溃 */
    public static void encode(DrawMusouSaberPacket msg, FriendlyByteBuf buf) {
        buf.writeByte(0);
    }

    public static DrawMusouSaberPacket decode(FriendlyByteBuf buf) {
        buf.readByte(); // 把哑字节读出来
        return new DrawMusouSaberPacket();
    }

    public static void handle(DrawMusouSaberPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            CuriosApi.getCuriosInventory(player).ifPresent(inv ->
                    inv.getStacksHandler("waist_left").ifPresent(handler -> {
                        ItemStackHandler stacks = (ItemStackHandler) handler.getStacks();
                        for (int i = 0; i < stacks.getSlots(); i++) {
                            ItemStack stack = stacks.getStackInSlot(i);
                            if (stack.getItem().getClass().getSimpleName().equals("musousaberd")) {
                                player.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
                                stacks.setStackInSlot(i, ItemStack.EMPTY);

                                player.level().playSound(
                                        null, player.blockPosition(),
                                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1F, 1F);
                                player.connection.send(
                                        new net.minecraft.network.protocol.game.ClientboundAnimatePacket(player, 0));
                                break;
                            }
                        }
                    })
            );
        });
        ctx.get().setPacketHandled(true);
    }
}