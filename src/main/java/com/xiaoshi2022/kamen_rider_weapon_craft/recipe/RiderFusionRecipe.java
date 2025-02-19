package com.xiaoshi2022.kamen_rider_weapon_craft.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class RiderFusionRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    final ItemStack output;
    private final NonNullList<Ingredient> ingredients;
    private final int fusionTime;
    final int[] requiredCounts;

    public RiderFusionRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> ingredients, int fusionTime, int[] requiredCounts) {
        this.id = id;
        this.output = output;
        this.ingredients = ingredients;
        this.fusionTime = fusionTime;
        this.requiredCounts = requiredCounts;
        System.out.println("[Recipe] Created recipe: " + id);
        System.out.println(" - Output: " + output);
        System.out.println(" - Fusion time: " + fusionTime);
        for (int i = 0; i < ingredients.size(); i++) {
            System.out.printf(" - Slot %d: %s x%d%n", i, ingredients.get(i).getItems()[0], requiredCounts[i]);
        }
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public boolean matches(Container inv, Level level) {
        System.out.println("[Recipe] Checking recipe match...");
        for (int slot = 0; slot < 4; slot++) {
            ItemStack slotStack = inv.getItem(slot);
            Ingredient ingredient = ingredients.get(slot);
            System.out.printf(" - Slot %d: Actual=%s x%d, Required=%s x%d%n",
                slot,
                slotStack.getItem().getDescriptionId(),  // Replaced getRegistryName with getDescriptionId
                slotStack.getCount(),
                ingredient.getItems()[0].getItem().getDescriptionId(),  // Replaced getRegistryName with getDescriptionId
                requiredCounts[slot]
            );

            if (!ingredient.test(slotStack)) {  // Closing parentheses
                System.out.println("[Recipe] Item does not match: " + slotStack);
                return false;
            }
            if (slotStack.getCount() < requiredCounts[slot]) {
                System.out.println("[Recipe] Insufficient quantity: " + slotStack.getCount() + " < " + requiredCounts[slot]);
                return false;
            }
        }
        System.out.println("[Recipe] Match successful");
        return true;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        System.out.println("[Recipe] Assembling output item: " + output);
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.RIDER_FUSION_RECIPE.get();
    }

    public int getFusionTime() {
        return fusionTime;
    }

    public int getRequiredCount(int slot) {
        return requiredCounts[slot];
    }

    // ---------------------- Serializer Inner Class ----------------------
    public static class Serializer implements RecipeSerializer<RiderFusionRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public RiderFusionRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            System.out.println("[Recipe] Loading recipe from JSON: " + recipeId);
            try {
                // Parse output item
                ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));

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
            System.out.println("[Recipe] Loading recipe from network: " + recipeId);
            ItemStack output = buffer.readItem();

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
            System.out.println("[Recipe] Writing recipe to network: " + recipe.id);
            buffer.writeItemStack(recipe.output, false);

            for (int slot = 0; slot < 4; slot++) {
                recipe.getIngredients().get(slot).toNetwork(buffer);
                buffer.writeInt(recipe.requiredCounts[slot]);
            }

            buffer.writeInt(recipe.getFusionTime());
        }
    }
}
