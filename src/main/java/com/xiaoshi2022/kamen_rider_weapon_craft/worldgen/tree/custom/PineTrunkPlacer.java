package com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.custom;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.ModTrunkPlacerTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.List;
import java.util.function.BiConsumer;

public class PineTrunkPlacer extends TrunkPlacer {
    public static final Codec<PineTrunkPlacer> CODEC = RecordCodecBuilder.create(instance -> trunkPlacerParts(instance).apply(instance, PineTrunkPlacer::new));

    public PineTrunkPlacer(int baseHeight, int firstRandomHeight, int secondRandomHeight) {
        super(baseHeight, firstRandomHeight, secondRandomHeight);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return ModTrunkPlacerTypes.PINE_TRUNK_PLACER.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, int freeTreeHeight, BlockPos pos, TreeConfiguration config) {
        // Place the straight trunk (similar to Oak Tree)
        int firstRandomHeight = 2; // 增加随机高度范围
        int secondRandomHeight = 2; // 增加随机高度范围
        int height = baseHeight + random.nextInt(firstRandomHeight + 1) + random.nextInt(secondRandomHeight + 1);
        for (int i = 0; i < height; i++) {
            placeLog(level, blockSetter, random, pos.above(i), config);
        }

        // 定义顶部叶子附着点（类似于 Acacia Tree）
        int foliageHeight = 3; // 增加叶子附着点高度
        int foliageRadius = 3; // 增加叶子附着点半径

        // Return the top foliage attachment point
        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pos.above(height - foliageHeight), foliageRadius, false));
    }
}