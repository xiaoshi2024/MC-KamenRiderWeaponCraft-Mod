package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.recipe.RiderFusionRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, "kamen_rider_weapon_craft");

    public static final RegistryObject<RecipeType<RiderFusionRecipe>> RIDER_FUSION_RECIPE = RECIPE_TYPES.register("rider_fusion_recipe", () -> new RecipeType<RiderFusionRecipe>() {});
}