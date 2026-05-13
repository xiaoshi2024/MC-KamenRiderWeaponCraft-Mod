package com.xiaoshi2022.kamenriderweaponcraft.rider.core;

import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.ExternalRiderEffectProvider;
import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.HeiseiRiderEffect;
import com.xiaoshi2022.kamenriderweaponcraft.rider.effect.HeiseiRiderEffectManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CoreSlotManager {

    private static final Logger LOGGER = KamenRiderWeaponCraft.LOGGER;

    private static final String TAG_ATTACHED_CORE_ID = "attached_core_id";
    private static final String TAG_ATTACHED_CORE_MOD = "attached_core_mod";
    private static final String TAG_CORE_DURABILITY_BONUS = "core_durability_bonus";

    public static final Map<String, CoreSlotInfo> REGISTERED_CORES = new HashMap<>();

    public static class CoreSlotInfo {
        public final String coreId;
        public final String coreName;
        public final String modId;
        @Nullable
        public final ResourceLocation modelLocation;
        @Nullable
        public final Supplier<ResourceLocation> animControllerSupplier;
        public final ExternalRiderEffectProvider effectProvider;
        public final int durabilityBonus;

        public CoreSlotInfo(String coreId, String coreName, String modId,
                           @Nullable ResourceLocation modelLocation,
                           @Nullable Supplier<ResourceLocation> animControllerSupplier,
                           ExternalRiderEffectProvider effectProvider,
                           int durabilityBonus) {
            this.coreId = coreId;
            this.coreName = coreName;
            this.modId = modId;
            this.modelLocation = modelLocation;
            this.animControllerSupplier = animControllerSupplier;
            this.effectProvider = effectProvider;
            this.durabilityBonus = durabilityBonus;
        }
    }

    public static void registerExternalCore(String coreId, String coreName, String modId,
                                            @Nullable ResourceLocation modelLocation,
                                            @Nullable Supplier<ResourceLocation> animControllerSupplier,
                                            ExternalRiderEffectProvider effectProvider,
                                            int durabilityBonus) {
        if (REGISTERED_CORES.containsKey(coreId)) {
            LOGGER.warn("Core {} is already registered, skipping...", coreId);
            return;
        }

        CoreSlotInfo info = new CoreSlotInfo(coreId, coreName, modId, modelLocation,
                animControllerSupplier, effectProvider, durabilityBonus);
        REGISTERED_CORES.put(coreId, info);

        HeiseiRiderEffectManager.registerExternalRider(modId + ":" + coreId, effectProvider);

        LOGGER.info("Registered external core: {} from mod {}", coreId, modId);
    }

    public static void registerExternalCore(String coreId, String coreName, String modId,
                                            ExternalRiderEffectProvider effectProvider) {
        registerExternalCore(coreId, coreName, modId, null, null, effectProvider, 0);
    }

    @Nullable
    public static CoreSlotInfo getCoreInfo(String coreId) {
        return REGISTERED_CORES.get(coreId);
    }

    public static boolean hasAttachedCore(ItemStack stack) {
        CompoundTag tag = getCoreTag(stack);
        return tag.contains(TAG_ATTACHED_CORE_ID);
    }

    @Nullable
    public static String getAttachedCoreId(ItemStack stack) {
        if (!hasAttachedCore(stack)) {
            return null;
        }
        return getCoreTag(stack).getString(TAG_ATTACHED_CORE_ID);
    }

    @Nullable
    public static CoreSlotInfo getAttachedCoreInfo(ItemStack stack) {
        String coreId = getAttachedCoreId(stack);
        return coreId != null ? REGISTERED_CORES.get(coreId) : null;
    }

    public static void attachCore(ItemStack stack, String coreId) {
        CoreSlotInfo info = REGISTERED_CORES.get(coreId);
        if (info == null) {
            LOGGER.warn("Cannot attach unknown core: {}", coreId);
            return;
        }

        CompoundTag tag = getOrCreateCoreTag(stack);
        tag.putString(TAG_ATTACHED_CORE_ID, coreId);
        tag.putString(TAG_ATTACHED_CORE_MOD, info.modId);
        tag.putInt(TAG_CORE_DURABILITY_BONUS, info.durabilityBonus);
        saveCoreTag(stack, tag);

        String riderId = info.modId + ":" + coreId;
        HeiseiRiderEffectManager.registerExternalRider(riderId, info.effectProvider);

        LOGGER.info("Attached core {} to sword", coreId);
    }

    public static void detachCore(ItemStack stack) {
        CompoundTag tag = getOrCreateCoreTag(stack);
        String coreId = tag.getString(TAG_ATTACHED_CORE_ID);
        tag.remove(TAG_ATTACHED_CORE_ID);
        tag.remove(TAG_ATTACHED_CORE_MOD);
        tag.remove(TAG_CORE_DURABILITY_BONUS);
        saveCoreTag(stack, tag);

        LOGGER.info("Detached core {} from sword", coreId);
    }

    public static int getTotalDurabilityBonus(ItemStack stack) {
        if (!hasAttachedCore(stack)) {
            return 0;
        }
        return getCoreTag(stack).getInt(TAG_CORE_DURABILITY_BONUS);
    }

    @Nullable
    public static ResourceLocation getAttachedCoreModel(ItemStack stack) {
        CoreSlotInfo info = getAttachedCoreInfo(stack);
        return info != null ? info.modelLocation : null;
    }

    @Nullable
    public static Supplier<ResourceLocation> getAttachedCoreAnimController(ItemStack stack) {
        CoreSlotInfo info = getAttachedCoreInfo(stack);
        return info != null ? info.animControllerSupplier : null;
    }

    @Nullable
    public static HeiseiRiderEffect getAttachedCoreEffect(ItemStack stack) {
        String coreId = getAttachedCoreId(stack);
        if (coreId == null) {
            return null;
        }
        return HeiseiRiderEffectManager.getRiderEffect(coreId);
    }

    public static boolean isValidCore(String coreId) {
        return REGISTERED_CORES.containsKey(coreId);
    }

    public static Map<String, CoreSlotInfo> getRegisteredCores() {
        return new HashMap<>(REGISTERED_CORES);
    }

    private static final String CORE_DATA_KEY = "heiseisword_core_data";

    private static CompoundTag getCoreTag(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (!tag.contains(CORE_DATA_KEY)) {
            tag.put(CORE_DATA_KEY, new CompoundTag());
        }
        return tag.getCompound(CORE_DATA_KEY);
    }

    private static CompoundTag getOrCreateCoreTag(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (!tag.contains(CORE_DATA_KEY)) {
            tag.put(CORE_DATA_KEY, new CompoundTag());
        }
        return tag.getCompound(CORE_DATA_KEY);
    }

    private static void saveCoreTag(ItemStack stack, CompoundTag tag) {
        CompoundTag outerTag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        outerTag.put(CORE_DATA_KEY, tag);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(outerTag));
    }
}