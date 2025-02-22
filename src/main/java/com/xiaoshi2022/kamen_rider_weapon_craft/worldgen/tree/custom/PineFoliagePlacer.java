package com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.custom;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
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
import java.util.function.BiConsumer;

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

        for (int i = 0; i < foliageHeight; i++) {
            int currentRadius = foliageRadius - (i / 2); // Reduce radius as height increases
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

                        // 随机生成藤蔓
                        if (Math.abs(x) == currentRadius || Math.abs(z) == currentRadius) {
                            if (random.nextFloat() < 0.3f) { // 30% 概率生成藤蔓
                                for (Direction direction : Direction.Plane.HORIZONTAL) {
                                    BlockPos vinePos = leafPos.relative(direction);
                                    if (random.nextFloat() < 0.6f) { // 60% 概率在每个方向生成藤蔓
                                        setter.set(vinePos, ModBlocks.HELHEIMVINE.get().defaultBlockState());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
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