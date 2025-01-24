package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals.helheim_crack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    // 创建一个 DeferredRegister 实例用于注册方块
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "kamen_rider_weapon_craft");

    // 注册一个方块
    public static final RegistryObject<helheim_crack> HELHEIM_CRACK_BLOCK = BLOCKS.register("helheim_crack_block",
            () -> new helheim_crack(Block.Properties.of().strength(1.5f, 6.0f).noCollission()));

}