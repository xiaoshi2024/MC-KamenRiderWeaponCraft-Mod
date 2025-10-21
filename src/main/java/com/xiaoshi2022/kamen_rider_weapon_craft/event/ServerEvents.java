package com.xiaoshi2022.kamen_rider_weapon_craft.event;

import com.xiaoshi2022.kamen_rider_weapon_craft.Item.custom.musousaberd;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemStackHandler;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "kamen_rider_weapon_craft", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemStack main = player.getMainHandItem();

        if (!main.isEmpty() || !main.getItem().equals(ModItems.MUSOUSABERD.get())) return;

        CuriosApi.getCuriosInventory(player).ifPresent(inv ->
                inv.getStacksHandler("waist_left").ifPresent(handler -> {
                    ItemStackHandler stacks = (ItemStackHandler) handler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        if (stack.getItem() instanceof musousaberd) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
                            stacks.setStackInSlot(i, ItemStack.EMPTY);

                            player.level().playSound(
                                    null, player.blockPosition(),
                                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1F, 1F
                            );
                            ((ServerPlayer) player).connection.send(
                                    new net.minecraft.network.protocol.game.ClientboundAnimatePacket(player, 0)
                            );
                            event.setCanceled(true);
                            return;
                        }
                    }
                })
        );
    }
}