package com.xiaoshi2022.kamenriderweaponcraft.rider.core;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.Heiseisword;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CoreInteractionHandler {

    private static final Logger LOGGER = KamenRiderWeaponCraft.LOGGER;
    private static final Map<Item, String> CORE_ITEM_MAP = new HashMap<>();
    private static Function<String, ItemStack> coreItemFactory;

    public static void registerCoreItem(ItemStack itemStack, String coreId) {
        CORE_ITEM_MAP.put(itemStack.getItem(), coreId);
        LOGGER.info("Registered core item for: {}", coreId);
    }

    public static void registerCoreItemById(String itemRegistryName, String coreId) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(
                itemRegistryName.contains(":") ? itemRegistryName.split(":")[0] : "minecraft",
                itemRegistryName.contains(":") ? itemRegistryName.split(":")[1] : itemRegistryName
        );
        Item item = BuiltInRegistries.ITEM.get(resourceLocation);
        if (item != null && item != Items.AIR) {
            CORE_ITEM_MAP.put(item, coreId);
            LOGGER.info("Registered core item: {} -> {}", itemRegistryName, coreId);
        } else {
            LOGGER.warn("Failed to register core item: {} - item not found", itemRegistryName);
        }
    }

    public static void registerCoreItemFactory(Function<String, ItemStack> factory) {
        coreItemFactory = factory;
    }

    public static boolean isCoreItem(ItemStack stack) {
        if (stack == null) return false;
        return CORE_ITEM_MAP.containsKey(stack.getItem());
    }

    public static String getCoreIdFromItem(ItemStack stack) {
        if (stack == null) return null;
        return CORE_ITEM_MAP.get(stack.getItem());
    }

    public static boolean hasRegisteredCores() {
        return !CORE_ITEM_MAP.isEmpty();
    }

    public static void handleRightClick(PlayerInteractEvent.RightClickItem event) {
        handleRightClickInteraction(event);
    }

    public static void handleRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        handleRightClickInteraction(event);
    }

    private static void handleRightClickInteraction(PlayerInteractEvent event) {
        Player player = event.getEntity();
        if (player == null) return;

        ItemStack mainhand = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();
        Level level = player.level();

        if (!Heiseisword.isHeiseisword(mainhand)) {
            return;
        }

        if (Heiseisword.isHeiseisword(offhand)) {
            return;
        }

        if (level.isClientSide) {
            return;
        }

        ItemStack sword = mainhand;

        if (isCoreItem(offhand) && !CoreSlotManager.hasAttachedCore(sword)) {
            String coreId = getCoreIdFromItem(offhand);
            if (coreId == null) return;

            CoreSlotManager.attachCore(sword, coreId);
            offhand.shrink(1);

            LOGGER.info("Player {} attached core {} to heiseisword", player.getName().getString(), coreId);
            return;
        }

        if (offhand.isEmpty() && CoreSlotManager.hasAttachedCore(sword)) {
            String coreId = CoreSlotManager.getAttachedCoreId(sword);
            CoreSlotManager.detachCore(sword);

            ItemStack coreItemStack = createCoreItemStack(coreId);
            if (!coreItemStack.isEmpty()) {
                Vec3 eyePos = player.getEyePosition();
                Vec3 lookVec = player.getLookAngle();
                Vec3 dropPos = eyePos.add(lookVec.scale(1.0));

                ItemEntity itemEntity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, coreItemStack);
                itemEntity.setPickUpDelay(10);
                level.addFreshEntity(itemEntity);
            }

            LOGGER.info("Player {} detached core {} from heiseisword", player.getName().getString(), coreId);
        }
    }

    private static ItemStack createCoreItemStack(String coreId) {
        if (coreItemFactory != null) {
            return coreItemFactory.apply(coreId);
        }

        CoreSlotManager.CoreSlotInfo info = CoreSlotManager.getCoreInfo(coreId);
        if (info != null) {
            LOGGER.warn("No core item factory registered, cannot create item for core: {}", coreId);
        }

        return ItemStack.EMPTY;
    }

    public static void clearRegisteredItems() {
        CORE_ITEM_MAP.clear();
        LOGGER.info("Cleared all registered core items");
    }
}