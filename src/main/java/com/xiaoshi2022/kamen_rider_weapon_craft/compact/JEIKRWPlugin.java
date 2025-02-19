package com.xiaoshi2022.kamen_rider_weapon_craft.compact;

import com.xiaoshi2022.kamen_rider_weapon_craft.gui.RiderFusionMachinesScreen;
import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.recipe.RiderFusionRecipe;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class JEIKRWPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(kamen_rider_weapon_craft.MOD_ID,"jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        // 注册配方分类
        registration.addRecipeCategories(new RiderFusionRecipes(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<RiderFusionRecipe> riderFusionRecipes = recipeManager.getAllRecipesFor(ModRecipes.RIDER_FUSION_RECIPE.get());
        registration.addRecipes(RiderFusionRecipes.RIDER_FUSION_RECIPE_RECIPE_TYPE, riderFusionRecipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(RiderFusionMachinesScreen.class, 88, 23, 20, 30,
                RiderFusionRecipes.RIDER_FUSION_RECIPE_RECIPE_TYPE);
    }
}
