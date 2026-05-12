package com.xiaoshi2022.kamenriderweaponcraft;

import com.mojang.blaze3d.platform.InputConstants;
import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = KamenRiderWeaponCraft.MODID, value = Dist.CLIENT)
public class KamenRiderWeaponCraftClient {
    public static KeyMapping nextRiderKey;
    public static KeyMapping prevRiderKey;

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        nextRiderKey = new KeyMapping(
                "key.kamenriderweaponcraft.next_rider",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "key.categories.gameplay"
        );
        prevRiderKey = new KeyMapping(
                "key.kamenriderweaponcraft.prev_rider",
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "key.categories.gameplay"
        );
        event.register(nextRiderKey);
        event.register(prevRiderKey);
    }

    @EventBusSubscriber(modid = KamenRiderWeaponCraft.MODID, value = Dist.CLIENT)
    public static class KeyInputHandler {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;

            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof Heiseisword heiseisword)) return;

            if (nextRiderKey.consumeClick()) {
                heiseisword.handleClientRiderSelection(true);
            } else if (prevRiderKey.consumeClick()) {
                heiseisword.handleClientRiderSelection(false);
            }
        }
    }
}