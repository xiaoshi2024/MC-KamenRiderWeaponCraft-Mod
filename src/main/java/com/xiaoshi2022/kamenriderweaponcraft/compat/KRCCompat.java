package com.xiaoshi2022.kamenriderweaponcraft.compat;

import com.xiaoshi2022.kamenriderweaponcraft.Item.custom.SonicArrowItem;
import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 假面骑士工艺（Kamen Rider Craft, modid=kamenridercraft）软联动
 * 不引入编译依赖，运行期按注册 ID 查找其锁种物品（反射式软联动）
 *
 * 锁种 → 音速弓形态映射：
 *   orange_lockseed                 → ORANGE（橘色）
 *   melon_lockseed                  → MELON（绿色）
 *   lemon_lockseed / lemon_energy_lockseed      → LEMON（黄色）
 *   cherry_energy_lockseed          → CHERRY（绯红）
 *   peach_energy_lockseed           → PEACH（粉色）
 *   dragon_fruits_energy_lockseed   → DRAGON（红色）
 */
public final class KRCCompat {
    public static final String KRC_MODID = "kamenridercraft";

    /** 候选注册名（按优先级，同一形态可对应多个 KRC 锁种） */
    private static final Map<SonicArrowItem.Mode, String[]> LOCKSEED_NAMES = Map.of(
            SonicArrowItem.Mode.ORANGE, new String[]{"orange_lockseed"},
            SonicArrowItem.Mode.MELON, new String[]{"melon_lockseed", "melon_energy_lockseed"},
            SonicArrowItem.Mode.LEMON, new String[]{"lemon_lockseed", "lemon_energy_lockseed"},
            SonicArrowItem.Mode.CHERRY, new String[]{"cherry_energy_lockseed"},
            SonicArrowItem.Mode.PEACH, new String[]{"peach_energy_lockseed"},
            SonicArrowItem.Mode.DRAGON, new String[]{"dragon_fruits_energy_lockseed"}
    );

    private static boolean resolved = false;
    private static final Map<Item, SonicArrowItem.Mode> ITEM_TO_MODE = new IdentityHashMap<>();

    private KRCCompat() {}

    /** 假面骑士工艺是否已安装 */
    public static boolean isLoaded() {
        return ModList.get().isLoaded(KRC_MODID);
    }

    /** 物品是否是可装载的锁种 */
    public static boolean isLockseed(Item item) {
        return getModeFromItem(item) != null;
    }

    /** 锁种物品 → 音速弓形态（非锁种返回 null） */
    public static SonicArrowItem.Mode getModeFromItem(Item item) {
        resolve();
        SonicArrowItem.Mode mode = ITEM_TO_MODE.get(item);
        if (mode != null) return mode;
        return matchByKeyword(item);
    }

    /**
     * 关键词兜底匹配：KRC 的锁种变体众多（fresh/dark/blood/能量 等），
     * 凡 kamenridercraft 命名空间下名称含对应水果关键词的锁种一律接受
     */
    private static SonicArrowItem.Mode matchByKeyword(Item item) {
        if (!isLoaded()) return null;
        try {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (!KRC_MODID.equals(id.getNamespace())) return null;
            String path = id.getPath();
            if (!path.contains("lockseed")) return null;
            if (path.contains("dragon")) return SonicArrowItem.Mode.DRAGON;
            if (path.contains("melon")) return SonicArrowItem.Mode.MELON;
            if (path.contains("orange")) return SonicArrowItem.Mode.ORANGE;
            if (path.contains("lemon")) return SonicArrowItem.Mode.LEMON;
            if (path.contains("cherry")) return SonicArrowItem.Mode.CHERRY;
            if (path.contains("peach")) return SonicArrowItem.Mode.PEACH;
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 运行期按注册 ID 解析 KRC 锁种（注册表冻结后调用，失败静默跳过）
     * 零类引用 = 反射级软联动，不依赖 KRC 的任何 Java 类
     */
    private static void resolve() {
        if (resolved) return;
        resolved = true;
        if (!isLoaded()) return;

        for (Map.Entry<SonicArrowItem.Mode, String[]> entry : LOCKSEED_NAMES.entrySet()) {
            for (String name : entry.getValue()) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(KRC_MODID, name);
                try {
                    // 1.21.1 按 ID 取注册项；不存在时返回 AIR
                    Item item = BuiltInRegistries.ITEM.get(id);
                    if (item != null && item != net.minecraft.world.item.Items.AIR
                            && BuiltInRegistries.ITEM.getKey(item).equals(id)) {
                        ITEM_TO_MODE.put(item, entry.getKey());
                    }
                } catch (Exception ignored) {
                    // KRC 版本更名时静默降级
                }
            }
        }
        KamenRiderWeaponCraft.LOGGER.info("[KRWC] Kamen Rider Craft lockseed linkage resolved: {} items",
                ITEM_TO_MODE.size());
    }
}
