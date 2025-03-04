package com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.dimension.ModDimensions;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.ModFoliagePlacers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

import java.util.*;

public class PineFoliagePlacer extends FoliagePlacer {
    public static final Codec<PineFoliagePlacer> CODEC = RecordCodecBuilder.create(instance -> foliagePlacerParts(instance).apply(instance, PineFoliagePlacer::new));

    public PineFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return ModFoliagePlacers.PINE_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, FoliageSetter setter, RandomSource random, TreeConfiguration config, int freeTreeHeight, FoliageAttachment foliageAttachment, int foliageHeight, int foliageRadius, int offset) {
        BlockPos pos = foliageAttachment.pos();

        // 生成树叶逻辑
        for (int i = 0; i < foliageHeight; i++) {
            int currentRadius = foliageRadius - (i / 2); // 随高度增加减少半径
            for (int x = -currentRadius; x <= currentRadius; x++) {
                for (int z = -currentRadius; z <= currentRadius; z++) {
                    if (Math.abs(x) + Math.abs(z) <= currentRadius) {
                        BlockPos leafPos = pos.offset(x, i, z);

                        // 检查该位置是否已经有树干，如果有，则跳过
                        if (((LevelReader) level).getBlockState(leafPos).getBlock() == ModBlocks.PINE_LOG.get()) {
                            continue;
                        }

                        BlockState leafState = config.foliageProvider.getState(random, leafPos);
                        leafState = leafState.setValue(LeavesBlock.PERSISTENT, false); // 默认设置为可衰落

                        if (isConnectedToTrunk(level, leafPos, pos)) {
                            leafState = leafState.setValue(LeavesBlock.PERSISTENT, true); // 如果连接到树干，则设置为持久
                        }

                        setter.set(leafPos, leafState);
                    }
                }
            }
        }

        // 检查是否在赫尔海姆维度
        if (isInHelheimDimension((LevelReader) level)) {
            // 在所有树上生成赫尔海姆藤蔓
            generateHelheimVines(level, setter, random, pos, foliageRadius, foliageHeight);
        }


        // 赫尔海姆藤蔓的“直线落雨式”生成逻辑
        // 随机选择几条路径生成藤蔓
        int numVinesToGenerate = random.nextInt(5) + 2; // 随机生成2到5条藤蔓

        for (int vineIndex = 0; vineIndex < numVinesToGenerate; vineIndex++) {
            // 随机选择一条路径的起始位置
            int startX = random.nextInt(foliageRadius * 2 + 1) - foliageRadius;
            int startZ = random.nextInt(foliageRadius * 2 + 1) - foliageRadius;

            // 确保起始位置在树叶范围内
            if (Math.abs(startX) + Math.abs(startZ) <= foliageRadius) {
                BlockPos startVinePos = pos.offset(startX, 0, startZ); // 从第一层开始

                // 检查起始位置是否有树叶
                if (((LevelReader) level).getBlockState(startVinePos).getBlock() == ModBlocks.PINE_LEAVES.get()) {
                    BlockPos currentPos = startVinePos;

                    // 沿路径生成藤蔓
                    for (int y = 0; y < foliageHeight; y++) {
                        currentPos = currentPos.below(1); // 向下生成
                        if (((LevelReader) level).getBlockState(currentPos).isAir()) {
                            setter.set(currentPos, ModBlocks.HELHEIMVINE.get().defaultBlockState());
                        }
                    }
                }
            }
        }
    }

    // 在 PineFoliagePlacer 类中添加一个通用方法
    private void generateHelheimVines(LevelSimulatedReader level, FoliageSetter setter, RandomSource random, BlockPos pos, int foliageRadius, int foliageHeight) {
        int numVinesToGenerate = random.nextInt(5) + 2; // 随机生成2到5条藤蔓

        for (int vineIndex = 0; vineIndex < numVinesToGenerate; vineIndex++) {
            // 随机选择一条路径的起始位置
            int startX = random.nextInt(foliageRadius * 2 + 1) - foliageRadius;
            int startZ = random.nextInt(foliageRadius * 2 + 1) - foliageRadius;

            // 确保起始位置在树叶范围内
            if (Math.abs(startX) + Math.abs(startZ) <= foliageRadius) {
                BlockPos startVinePos = pos.offset(startX, 0, startZ); // 从第一层开始

                // 检查起始位置是否有树叶
                if (((LevelReader) level).getBlockState(startVinePos).getBlock() instanceof LeavesBlock) {
                    BlockPos currentPos = startVinePos;

                    // 沿路径生成藤蔓
                    for (int y = 0; y < foliageHeight; y++) {
                        currentPos = currentPos.below(1); // 向下生成
                        if (((LevelReader) level).getBlockState(currentPos).isAir()) {
                            setter.set(currentPos, ModBlocks.HELHEIMVINE.get().defaultBlockState());
                        }
                    }
                }
            }
        }
    }

    private boolean isInHelheimDimension(LevelReader level) {
        // 检查当前维度是否为赫尔海姆维度
        return level == ModDimensions.HELHEIM_DIMENSION_TYPE;
    }

    private boolean isConnectedToTrunk(LevelSimulatedReader level, BlockPos leafPos, BlockPos trunkPos) {
        // 使用队列实现广度优先搜索（BFS）
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(leafPos);
        visited.add(leafPos);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();

            // 检查当前方块是否为树干
            if (((LevelReader) level).getBlockState(currentPos).getBlock() == ModBlocks.PINE_LOG.get()) {
                return true;
            }

            // 检查当前方块的相邻方块
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = currentPos.relative(direction);

                // 如果相邻方块是树叶且未访问过，加入队列
                if (!visited.contains(neighborPos) &&
                        ((LevelReader) level).getBlockState(neighborPos).is(ModBlocks.PINE_LEAVES.get())) {
                    queue.add(neighborPos);
                    visited.add(neighborPos);
                }
            }
        }

        return false; // 如果没有找到树干，返回 false
    }

    @Override
    public int foliageHeight(RandomSource random, int freeTreeHeight, TreeConfiguration config) {
        return 4 + random.nextInt(3); // 随机高度从4到6
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int x, int y, int z, int range, boolean large) {
        return false;
    }
}