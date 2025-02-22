package com.xiaoshi2022.kamen_rider_weapon_craft.blocks.portals.plant;

import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class helheim_plant extends SugarCaneBlock {
    public helheim_plant() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().sound(SoundType.GRASS).instabreak().noCollission().offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY));
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 100;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 60;
    }

    @Override
    public PlantType getPlantType(BlockGetter world, BlockPos pos) {
        return PlantType.PLAINS;
    }

    @Override
    public void randomTick(BlockState blockstate, ServerLevel world, BlockPos pos, RandomSource random) {
        if (world.isEmptyBlock(pos.above())) {
            int i = 1;
            for (; world.getBlockState(pos.below(i)).is(this); ++i);
            if (i < 2) {
                int j = blockstate.getValue(AGE);
                if (ForgeHooks.onCropsGrowPre(world, pos, blockstate, true)) {
                    if (j == 15) {
                        world.setBlockAndUpdate(pos.above(), defaultBlockState());
                        world.setBlock(pos, blockstate.setValue(AGE, 0), 4);
                    } else {
                        world.setBlock(pos, blockstate.setValue(AGE, j + 1), 4);
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void blockColorLoad(RegisterColorHandlersEvent.Block event) {
        BlockColors blockColors = event.getBlockColors();
        List<RegistryObject<Block>> helheimPlants = ModBlocks.HELHEIM_PLANTS;

        for (RegistryObject<Block> plantRegistryObject : helheimPlants) {
            if (plantRegistryObject.isPresent()) {
                blockColors.register((state, world, pos, index) -> {
                    return world != null && pos != null ? BiomeColors.getAverageGrassColor(world, pos) : GrassColor.get(0.5D, 1.0D);
                }, plantRegistryObject.get());
            }
        }
    }
}

