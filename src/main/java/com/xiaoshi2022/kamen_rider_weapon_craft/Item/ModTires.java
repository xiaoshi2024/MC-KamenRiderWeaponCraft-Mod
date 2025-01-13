package com.xiaoshi2022.kamen_rider_weapon_craft.Item;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;

public class ModTires {
    public static final ForgeTier DAIMARU = new ForgeTier(2,1400,1.5f,
            2f,22, BlockTags.NEEDS_IRON_TOOL,
            () -> Ingredient.of(ModItems.DAIDAIMARU.get()));
}
