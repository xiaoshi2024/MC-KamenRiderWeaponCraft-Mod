package com.xiaoshi2022.kamen_rider_weapon_craft.recipe;

import com.google.gson.JsonObject;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class RiderFusionRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final ItemStack output;
    private final Ingredient[] ingredients; // 索引0: 基础控制电路，索引1: 合金矿，索引2: 可选材料

    public RiderFusionRecipe(ResourceLocation id, ItemStack output, Ingredient[] ingredients) {
        this.id = id;
        this.output = output;
        this.ingredients = ingredients;
        // 确保 ingredients 的长度为 3
        if (ingredients.length != 3) {
            throw new IllegalArgumentException("Rider Fusion Recipe 必须包含 3 个输入材料！");
        }
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        if (inv == null) {
            return false;
        }

        // 检查槽位0（基础控制电路）
        if (!ingredients[0].test(inv.getItem(0))) {
            return false;
        }

        // 检查槽位1（骑士锻造合金矿）
        if (!ingredients[1].test(inv.getItem(1))) {
            return false;
        }

        // 检查槽位2（蛋糕）
        if (!ingredients[2].test(inv.getItem(2))) {
            return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 1;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267052_) {
        return output.copy(); // 返回副本而非原始引用
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.RIDER_FUSION_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.RIDER_FUSION_RECIPE.get();
    }

    public static class Serializer implements RecipeSerializer<RiderFusionRecipe> {
        @Override
        public RiderFusionRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            Ingredient[] ingredients = new Ingredient[3];
            for (int i = 0; i < 3; i++) {
                ingredients[i] = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredients").get(String.valueOf(i)));
            }
            return new RiderFusionRecipe(recipeId, output, ingredients);
        }

        @Override
        public RiderFusionRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ItemStack output = buffer.readItem();
            Ingredient[] ingredients = new Ingredient[3];
            for (int i = 0; i < 3; ) {
                ingredients[i] = Ingredient.fromNetwork(buffer);
            }
            return new RiderFusionRecipe(recipeId, output, ingredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RiderFusionRecipe recipe) {
            buffer.writeItemStack(recipe.output, false);
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buffer);
            }
        }
    }
}