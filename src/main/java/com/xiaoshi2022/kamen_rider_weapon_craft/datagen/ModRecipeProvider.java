package com.xiaoshi2022.kamen_rider_weapon_craft.datagen;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        // 添加木板的配方（从 PINE_LOG）
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.PINE_PLANKS.get(), 4)
                .requires(Ingredient.of(ModBlocks.PINE_LOG.get()))
                .unlockedBy("has_log", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.PINE_LOG.get()))
                .save(consumer, new ResourceLocation("kamen_rider_weapon_craft:pine_planks_from_log")); // 指定唯一路径

        // 添加木板的配方（从 PINE_WOOD）
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.PINE_PLANKS.get(), 4)
                .requires(Ingredient.of(ModBlocks.PINE_WOOD.get()))
                .unlockedBy("has_wood", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.PINE_WOOD.get()))
                .save(consumer, new ResourceLocation("kamen_rider_weapon_craft:pine_planks_from_wood")); // 指定唯一路径
        // 添加木板合成木棍的配方
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.STICK, 4)
                .requires(Ingredient.of(ModBlocks.PINE_PLANKS.get(), ModBlocks.PINE_PLANKS.get()))
                .unlockedBy("has_planks", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.PINE_PLANKS.get()))
                .save(consumer, new ResourceLocation("kamen_rider_weapon_craft:sticks_from_pine_planks"));
        // 添加原木合成木材的配方
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.PINE_WOOD.get(), 3)
                .requires(Ingredient.of(ModBlocks.PINE_LOG.get()))
                .unlockedBy("has_log", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.PINE_LOG.get()))
                .save(consumer, new ResourceLocation("kamen_rider_weapon_craft:pine_wood_from_log"));
    }
}