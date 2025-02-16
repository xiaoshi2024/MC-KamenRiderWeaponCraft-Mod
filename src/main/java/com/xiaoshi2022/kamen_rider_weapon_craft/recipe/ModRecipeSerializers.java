package com.xiaoshi2022.kamen_rider_weapon_craft.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "kamen_rider_weapon_craft");

    public static final RegistryObject<RecipeSerializer<RiderFusionRecipe>> RIDER_FUSION_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("rider_fusion_recipe", RiderFusionRecipe.Serializer::new);

    public static class Serializer implements RecipeSerializer<RiderFusionRecipe> {

        @Override
        public RiderFusionRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            // 解析输出物品
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));

            // 解析输入槽位 (0-3)
            JsonObject ingredientsObj = GsonHelper.getAsJsonObject(json, "ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.withSize(4, Ingredient.EMPTY);
            int[] requiredCounts = new int[4];

            for (int slot = 0; slot < 4; slot++) {
                String slotKey = String.valueOf(slot);
                if (!ingredientsObj.has(slotKey)) {
                    throw new JsonParseException("Missing ingredient definition for slot " + slot);
                }
                JsonObject slotObj = ingredientsObj.getAsJsonObject(slotKey);
                ingredients.set(slot, Ingredient.fromJson(slotObj));
                requiredCounts[slot] = GsonHelper.getAsInt(slotObj, "count", 1);
            }

            // 解析融合时间（必须提供）
            if (!json.has("fusion_time")) {
                throw new JsonParseException("Missing required field 'fusion_time'");
            }
            int fusionTime = GsonHelper.getAsInt(json, "fusion_time");

            return new RiderFusionRecipe(recipeId, output, ingredients, fusionTime, requiredCounts);
        }

        @Override
        public RiderFusionRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            // 读取输出物品
            ItemStack output = buffer.readItem();

            // 读取输入槽位
            NonNullList<Ingredient> ingredients = NonNullList.withSize(4, Ingredient.EMPTY);
            int[] requiredCounts = new int[4];
            for (int slot = 0; slot < 4; slot++) {
                ingredients.set(slot, Ingredient.fromNetwork(buffer));
                requiredCounts[slot] = buffer.readInt();
            }

            // 读取融合时间
            int fusionTime = buffer.readInt();

            return new RiderFusionRecipe(recipeId, output, ingredients, fusionTime, requiredCounts);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RiderFusionRecipe recipe) {
            // 写入输出物品
            buffer.writeItemStack(recipe.output, false);

            // 写入输入槽位
            for (int slot = 0; slot < 4; slot++) {
                recipe.getIngredients().get(slot).toNetwork(buffer);
                buffer.writeInt(recipe.requiredCounts[slot]);
            }

            // 写入融合时间
            buffer.writeInt(recipe.getFusionTime());
        }
    }
}