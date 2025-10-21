package com.xiaoshi2022.kamen_rider_weapon_craft.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers implements RecipeSerializer<RiderFusionRecipe> {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "kamen_rider_weapon_craft");

    public static final RegistryObject<RecipeSerializer<RiderFusionRecipe>> RIDER_FUSION_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("rider_fusion_recipe_serializer", RiderFusionRecipe.Serializer::new);

    @Override
    public RiderFusionRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        if (recipeId == null) {
            throw new IllegalArgumentException("Recipe ID cannot be null");
        }
        System.out.println("[Recipe] Loading recipe from JSON: " + recipeId);
        try {
            // Parse output item
            ItemStack output;
            if (json.has("output") && !json.get("output").isJsonNull()) {
                output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            } else {
                output = ItemStack.EMPTY;
            }

            // Parse input slots (0-3)
            JsonObject ingredientsObj = GsonHelper.getAsJsonObject(json, "ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.withSize(4, Ingredient.EMPTY);
            int[] requiredCounts = new int[4];

            for (int slot = 0; slot < 4; slot++) {
                String slotKey = String.valueOf(slot);
                if (!ingredientsObj.has(slotKey)) {
                    System.out.println("[Recipe] Missing slot definition: " + slot);
                    throw new JsonParseException("Missing ingredient definition for slot " + slot);
                }
                JsonObject slotObj = ingredientsObj.getAsJsonObject(slotKey);
                ingredients.set(slot, Ingredient.fromJson(slotObj));
                requiredCounts[slot] = GsonHelper.getAsInt(slotObj, "count", 1);
            }

            // Parse fusion time
            if (!json.has("fusion_time")) {
                System.out.println("[Recipe] Missing fusion time field");
                throw new JsonParseException("Missing required field 'fusion_time'");
            }
            int fusionTime = GsonHelper.getAsInt(json, "fusion_time");

            return new RiderFusionRecipe(recipeId, output, ingredients, fusionTime, requiredCounts);
        } catch (Exception e) {
            System.out.println("[Recipe] JSON parsing failed: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public RiderFusionRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        if (recipeId == null) {
            throw new IllegalArgumentException("Recipe ID cannot be null");
        }
        System.out.println("[Recipe] Loading recipe from network: " + recipeId);
        ItemStack output = buffer.readItem();
        if (output.isEmpty()) {
            output = ItemStack.EMPTY;
        }

        NonNullList<Ingredient> ingredients = NonNullList.withSize(4, Ingredient.EMPTY);
        int[] requiredCounts = new int[4];
        for (int slot = 0; slot < 4; slot++) {
            ingredients.set(slot, Ingredient.fromNetwork(buffer));
            requiredCounts[slot] = buffer.readInt();
        }

        int fusionTime = buffer.readInt();
        return new RiderFusionRecipe(recipeId, output, ingredients, fusionTime, requiredCounts);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RiderFusionRecipe recipe) {
        System.out.println("[Recipe] Writing recipe to network: " + recipe.getRecipeId());
        buffer.writeItemStack(recipe.output, false);

        for (int slot = 0; slot < 4; slot++) {
            recipe.getIngredients().get(slot).toNetwork(buffer);
            buffer.writeInt(recipe.requiredCounts[slot]);
        }

        buffer.writeInt(recipe.getFusionTime());
    }
}