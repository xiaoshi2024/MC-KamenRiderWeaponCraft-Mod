package com.xiaoshi2022.kamen_rider_weapon_craft.registry;


import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.RiderFusionMachineBlockEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.Time_traveler_studio_blockEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.helheim_crackBlockEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.client.plant.HelheimVineBlockEntity;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.entity.lockseedIronBarsEntity;
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
    public static final RegistryObject<BlockEntityType<RiderFusionMachineBlockEntity>> RIDER_FUSION_MACHINE_BLOCK_ENTITY = BLOCK_ENTITIES.register("rider_fusion_machine_block_entity",
            () -> BlockEntityType.Builder.of(RiderFusionMachineBlockEntity::new, ModBlocks.RIDER_FUSION_MACHINE_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<HelheimVineBlockEntity>> HELHEIM_VINE_ENTITY = BLOCK_ENTITIES.register("helheim_vine_entity",
            () -> BlockEntityType.Builder.of(HelheimVineBlockEntity::new, ModBlocks.HELHEIMVINE.get()).build(null));
  public static final RegistryObject<BlockEntityType<lockseedIronBarsEntity>> LOCKSEEDIRONBARS_ENTITY = BLOCK_ENTITIES.register("lockseed_iron_bars_entity",
            () -> BlockEntityType.Builder.of(lockseedIronBarsEntity::new, ModBlocks.LOCKSEEDIRONBARS.get()).build(null));

//    public static final RegistryObject<BlockEntityType<ModHangingSignBlockEntity>> MOD_HANGING_SIGN =
//            BLOCK_ENTITIES.register("mod_hanging_sign", () ->
//                    BlockEntityType.Builder.of(ModHangingSignBlockEntity::new,
//                            ModBlocks.PINE_HANGING_SIGN.get(), ModBlocks.PINE_WALL_HANGING_SIGN.get()).build(null));

}
