package com.xiaoshi2022.kamen_rider_weapon_craft.compact;

import com.xiaoshi2022.kamen_rider_weapon_craft.recipe.RiderFusionRecipe;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class RiderFusionRecipes implements IRecipeCategory<RiderFusionRecipe> {
    public static final ResourceLocation UID = new ResourceLocation("kamen_rider_weapon_craft", "rider_fusion");
    public static final ResourceLocation TEXTURE = new ResourceLocation("kamen_rider_weapon_craft", "textures/screens/rider_fusion_machines.png");

    public static final RecipeType<RiderFusionRecipe> RIDER_FUSION_RECIPE_RECIPE_TYPE =
            new RecipeType<>(UID, RiderFusionRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public RiderFusionRecipes(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 254, 103);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new  ItemStack(ModBlocks.RIDER_FUSION_MACHINE_BLOCK.get()));
    }

    @Override
    public RecipeType<RiderFusionRecipe> getRecipeType() {
        return RIDER_FUSION_RECIPE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.kamen_rider_weapon_craft.rider_fusion_machine_block");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RiderFusionRecipe recipe, IFocusGroup focusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 35, 23).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.INPUT, 54, 43).addIngredients(recipe.getIngredients().get(1));
        builder.addSlot(RecipeIngredientRole.INPUT, 73, 23).addIngredients(recipe.getIngredients().get(2));
        builder.addSlot(RecipeIngredientRole.INPUT, 153, 43).addIngredients(recipe.getIngredients().get(3));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 153, 23).addItemStack(recipe.getResultItem(null));
    }
}
