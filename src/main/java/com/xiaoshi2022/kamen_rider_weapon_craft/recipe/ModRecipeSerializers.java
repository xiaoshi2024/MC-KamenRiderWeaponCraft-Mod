package com.xiaoshi2022.kamen_rider_weapon_craft.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "kamen_rider_weapon_craft");

    public static final RegistryObject<RecipeSerializer<RiderFusionRecipe>> RIDER_FUSION_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("rider_fusion_recipe", () -> new RiderFusionRecipe.Serializer());
}
