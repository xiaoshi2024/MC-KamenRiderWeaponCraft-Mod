package com.xiaoshi2022.kamen_rider_weapon_craft.recipe;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, kamen_rider_weapon_craft.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, kamen_rider_weapon_craft.MOD_ID);

    public static final RegistryObject<RecipeType<RiderFusionRecipe>> RIDER_FUSION_RECIPE =
            RECIPE_TYPES.register("rider_fusion_recipe_1", () -> new RecipeType<>() {
            });

    public static final RegistryObject<RecipeSerializer<RiderFusionRecipe>> RIDER_FUSION_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("rider_fusion_recipe_serializer", () -> RiderFusionRecipe.Serializer.INSTANCE);
}