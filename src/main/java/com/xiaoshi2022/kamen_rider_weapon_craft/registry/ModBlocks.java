package com.xiaoshi2022.kamen_rider_weapon_craft.registry;

import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.custom.ModFlammableRotatedPillarBlock;
//import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.custom.ModHangingSignBlock;
//import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.custom.ModWallHangingSignBlock;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals.*;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals.plant.helheim_plant;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.PineTreeGrower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals.plant.HelheimVine;

import java.util.ArrayList;
import java.util.List;

public class ModBlocks {
    // 创建一个 DeferredRegister 实例用于注册方块
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "kamen_rider_weapon_craft");

    //功能方块
    public static final RegistryObject<lockseedIronBars> LOCKSEEDIRONBARS = BLOCKS.register(
            "lockseed_iron_bars", () -> new lockseedIronBars(BlockBehaviour.Properties.of().strength(1.5f, 6.0f).noOcclusion()));

    // 注册一个方块
    public static final RegistryObject<helheim_crack> HELHEIM_CRACK_BLOCK = BLOCKS.register("helheim_crack_block",
            () -> new helheim_crack(Block.Properties.of().strength(1.5f, 6.0f).noCollission()));

    public static final RegistryObject<Time_traveler_studio_block> TIME_TRAVELER_STUDIO_BLOCK = BLOCKS.register("time_traveler_studio_block",
            () -> new Time_traveler_studio_block(Block.Properties.of().strength(1.5f, 6.0f).noCollission()));

    public static final RegistryObject<Block> RIDER_FUSION_MACHINE_BLOCK = BLOCKS.register(
            "rider_fusion_machine_block", RiderFusionMachineBlock::new);

    public static final RegistryObject<Block> RIDERFORGINGALLOYMINERAL = BLOCKS.register("riderforgingalloymineral", RiderforgingalloymineralBlock::new);

    public static final RegistryObject<HelheimVine> HELHEIMVINE = BLOCKS.register("helheimvine",
            () -> new HelheimVine(Block.Properties.of().noCollission().randomTicks().instabreak()));

    // 注册植物
    public static final RegistryObject<Block> PINE_LOG = BLOCKS.register("pine_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG).strength(3f)));
    public static final RegistryObject<Block> PINE_WOOD = BLOCKS.register("pine_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD).strength(3f)));
    public static final RegistryObject<Block> STRIPPED_PINE_LOG = BLOCKS.register("stripped_pine_log",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG).strength(3f)));
    public static final RegistryObject<Block> STRIPPED_PINE_WOOD = BLOCKS.register("stripped_pine_wood",
            () -> new ModFlammableRotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD).strength(3f)));

    public static final RegistryObject<Block> PINE_LEAVES = BLOCKS.register("pine_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)){
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 60;
                }

                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 30;
                }
            });

    public static final RegistryObject<Block> PINE_PLANKS = BLOCKS.register("pine_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)) {
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }

                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }

                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            });

//    public static final RegistryObject<Block> PINE_HANGING_SIGN = BLOCKS.register("pine_hanging_sign",
//            () -> new ModHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_HANGING_SIGN), ModWoodTypes.PINE));
//    public static final RegistryObject<Block> PINE_WALL_HANGING_SIGN = BLOCKS.register("pine_wall_hanging_sign",
//            () -> new ModWallHangingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_HANGING_SIGN), ModWoodTypes.PINE));


    //注册树苗
    public static final RegistryObject<Block> PINE_SAPLING = BLOCKS.register("pine_sapling",
            () -> new SaplingBlock(new PineTreeGrower(), BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)));

//    // 存储注册的方块的 RegistryObject 列表
//    public static final List<RegistryObject<Block>> HELHEIM_PLANTS = new ArrayList<>();
//
//    static {
//        // 定义植物的名称数组
//        String[] plantNames = {"helheim_plant", "helheim_plant_2", "helheim_plant_3", "helheim_plant_4"};
//        for (String name : plantNames) {
//            // 注册方块并将 RegistryObject 添加到列表中
//            RegistryObject<Block> plant = BLOCKS.register(name, helheim_plant::new);
//            HELHEIM_PLANTS.add(plant);
//        }
//    }
    public static final RegistryObject<Block> HELHEIM_PLANT = BLOCKS.register("helheim_plant",
            () -> new helheim_plant(BlockBehaviour.Properties.copy(Blocks.WHEAT)));
    public static final RegistryObject<Block> HELHEIM_PLANT_2 = BLOCKS.register("helheim_plant_2",
            () -> new helheim_plant(BlockBehaviour.Properties.copy(Blocks.WHEAT)));
    public static final RegistryObject<Block> HELHEIM_PLANT_3 = BLOCKS.register("helheim_plant_3",
            () -> new helheim_plant(BlockBehaviour.Properties.copy(Blocks.WHEAT)));
    public static final RegistryObject<Block> HELHEIM_PLANT_4 = BLOCKS.register("helheim_plant_4",
            () -> new helheim_plant(BlockBehaviour.Properties.copy(Blocks.WHEAT)));
}