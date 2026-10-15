package com.xiaoshi2022.kamenriderweaponcraft.compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.xiaoshi2022.kamenriderweaponcraft.KamenRiderWeaponCraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import java.util.Map;

/**
 * 假面骑士工艺配方软联动（由 RecipeManagerMixin 在配方重载时调用）
 *
 * 仅当假面骑士工艺（kamenridercraft）安装时生效：
 * 1. 替换其 gaim/sonic_arrow 配方的产物 → 本 mod 的音速弓（保留其锁种+基础武器素材）
 * 2. 注入平成剑配方：基础武器 + 平成骑士锁种 + 平成45骑士眼魂
 */
public final class KRCRecipeInjector {
    private static final String KRC_MODID = KRCCompat.KRC_MODID;

    private static final ResourceLocation KRC_SONIC_ARROW_RECIPE =
            ResourceLocation.fromNamespaceAndPath(KRC_MODID, "gaim/sonic_arrow");
    private static final ResourceLocation HEISEI_SWORD_RECIPE =
            ResourceLocation.fromNamespaceAndPath(KamenRiderWeaponCraft.MODID, "krc_heisei_sword");

    private KRCRecipeInjector() {}

    /**
     * 在 RecipeManager 解析配方 JSON 前改写数据包内容
     * @param recipes 配方 JSON map（可变，RecipeManager#apply HEAD 阶段）
     */
    public static void inject(Map<ResourceLocation, JsonElement> recipes) {
        if (!ModList.get().isLoaded(KRC_MODID)) return;

        // 1. 替换音速弓配方产物
        JsonElement sonic = recipes.get(KRC_SONIC_ARROW_RECIPE);
        if (sonic != null && sonic.isJsonObject()) {
            JsonObject obj = sonic.getAsJsonObject();
            if (obj.has("result")) {
                JsonElement resultElem = obj.get("result");
                if (resultElem.isJsonObject()) {
                    JsonObject result = resultElem.getAsJsonObject();
                    result.add("id", new JsonPrimitive(KamenRiderWeaponCraft.MODID + ":sonicarrow"));
                    // 兜底旧格式（"item" 字段）
                    if (result.has("item")) {
                        result.add("item", new JsonPrimitive(KamenRiderWeaponCraft.MODID + ":sonicarrow"));
                    }
                }
            }
        }

        // 2. 注入平成剑配方（放回原位置避免覆盖已有条目）
        recipes.put(HEISEI_SWORD_RECIPE, buildHeiseiSwordRecipe());
    }

    /**
     * 平成剑配方：
     *  P _ P
     *  P S P    P=平成骑士锁种  S=基础武器  E=平成45骑士眼魂
     *  _ E _
     */
    private static JsonElement buildHeiseiSwordRecipe() {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "equipment");
        recipe.addProperty("group", KamenRiderWeaponCraft.MODID);

        com.google.gson.JsonArray pattern = new com.google.gson.JsonArray();
        pattern.add("P P");
        pattern.add("PSP");
        pattern.add(" E ");
        recipe.add("pattern", pattern);

        JsonObject key = new JsonObject();
        key.add("P", ingredient(KRC_MODID, "heisei_rider_lockseed"));
        key.add("S", ingredient(KRC_MODID, "base_sword"));
        key.add("E", ingredient(KRC_MODID, "fourtyfive_heisei_ghost_eyecon"));
        recipe.add("key", key);

        JsonObject result = new JsonObject();
        result.addProperty("id", KamenRiderWeaponCraft.MODID + ":heiseisword");
        result.addProperty("count", 1);
        recipe.add("result", result);
        return recipe;
    }

    private static JsonObject ingredient(String namespace, String path) {
        JsonObject item = new JsonObject();
        item.addProperty("item", namespace + ":" + path);
        return item;
    }
}
