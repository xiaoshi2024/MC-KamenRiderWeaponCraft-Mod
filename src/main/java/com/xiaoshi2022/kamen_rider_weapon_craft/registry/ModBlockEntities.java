package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.Time_traveler_studio_blockEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.helheim_crackBlockEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.world.inventory.SonicBowContainer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "kamen_rider_weapon_craft");

    public static final RegistryObject<BlockEntityType<helheim_crackBlockEntity>> HELHEIM_CRACK_BLOCK_ENTITY = BLOCK_ENTITIES.register("helheim_crack_block_entity", () ->
            BlockEntityType.Builder.of(helheim_crackBlockEntity::new, ModBlocks.HELHEIM_CRACK_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<Time_traveler_studio_blockEntity>> TIME_TRAVELER_STUDIO_BLOCK_ENTITY = BLOCK_ENTITIES.register("time_traveler_studio_block_entity", () ->
            BlockEntityType.Builder.of(Time_traveler_studio_blockEntity::new, ModBlocks.TIME_TRAVELER_STUDIO_BLOCK.get()).build(null));
}
