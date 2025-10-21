package com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.ModFoliagePlacers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PineFoliagePlacer extends FoliagePlacer {
    public static final Codec<PineFoliagePlacer> CODEC = RecordCodecBuilder.create(instance ->
            foliagePlacerParts(instance).apply(instance, PineFoliagePlacer::new));

    public PineFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return ModFoliagePlacers.PINE_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader level, FoliageSetter setter, RandomSource random,
                                 TreeConfiguration config, int freeTreeHeight, FoliageAttachment foliageAttachment,
                                 int foliageHeight, int foliageRadius, int offset) {
        BlockPos pos = foliageAttachment.pos();

        // 第一阶段：生成树叶
        generateSwampLeaves(level, setter, random, config, pos, foliageHeight, foliageRadius);

        // 第二阶段：生成藤蔓
        generateNaturalVines(level, setter, random, pos, foliageHeight, foliageRadius);
    }

    private void generateSwampLeaves(LevelSimulatedReader level, FoliageSetter setter, RandomSource random,
                                     TreeConfiguration config, BlockPos pos, int foliageHeight, int foliageRadius) {
        // 沼泽风格树叶 - 上部密集下部稀疏
        for (int yOffset = 0; yOffset < foliageHeight; yOffset++) {
            float density = 0.8f - (yOffset * 0.05f);
            int currentRadius = (int)(foliageRadius * (1.1f - (float)yOffset / foliageHeight));

            for (int x = -currentRadius; x <= currentRadius; x++) {
                for (int z = -currentRadius; z <= currentRadius; z++) {
                    if (x*x + z*z <= currentRadius*currentRadius && random.nextFloat() < density) {
                        BlockPos leafPos = pos.offset(x, yOffset, z);
                        if (canPlaceLeaf(level, leafPos)) {
                            BlockState leafState = config.foliageProvider.getState(random, leafPos)
                                    .setValue(LeavesBlock.PERSISTENT, random.nextFloat() < 0.3f);
                            setter.set(leafPos, leafState);
                        }
                    }
                }
            }
        }
    }

    private void generateNaturalVines(LevelSimulatedReader level, FoliageSetter setter, RandomSource random,
                                      BlockPos treeTop, int foliageHeight, int foliageRadius) {
        // 寻找所有可能的藤蔓起始点
        List<BlockPos> vineStarts = new ArrayList<>();
        int minY = treeTop.getY() - foliageHeight + 2; // 从树冠下部开始

        for (int y = treeTop.getY(); y >= minY; y--) {
            for (int x = -foliageRadius; x <= foliageRadius; x++) {
                for (int z = -foliageRadius; z <= foliageRadius; z++) {
                    BlockPos pos = new BlockPos(treeTop.getX() + x, y, treeTop.getZ() + z);
                    BlockPos below = pos.below();

                    if (isLeaf(level, pos) &&
                            level.isStateAtPosition(below, state -> state.isAir())) {
                        vineStarts.add(pos);
                    }
                }
            }
        }

        // 随机选择3-5个起始点生成藤蔓
        Collections.shuffle(vineStarts, new Random(random.nextLong()));
        int vineCount = Math.min(3 + random.nextInt(3), vineStarts.size());

        for (int i = 0; i < vineCount; i++) {
            growVineDownward(level, setter, random, vineStarts.get(i).below());
        }
    }

    private void growVineDownward(LevelSimulatedReader level, FoliageSetter setter,
                                  RandomSource random, BlockPos startPos) {
        BlockPos currentPos = startPos;
        int maxLength = 5 + random.nextInt(6); // 5-10格长

        for (int i = 0; i < maxLength; i++) {
            if (!canPlaceVine(level, currentPos)) break;

            setter.set(currentPos, ModBlocks.HELHEIMVINE.get().defaultBlockState());

            // 25%几率生成侧向藤蔓
            if (random.nextFloat() < 0.25f && i > 1) {
                growSideVine(level, setter, random, currentPos);
            }

            // 10%几率提前终止
            if (random.nextFloat() < 0.1f) break;

            currentPos = currentPos.below();
        }
    }

    private void growSideVine(LevelSimulatedReader level, FoliageSetter setter,
                              RandomSource random, BlockPos pos) {
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int length = 1 + random.nextInt(2);

        BlockPos currentPos = pos;
        for (int i = 0; i < length; i++) {
            currentPos = currentPos.relative(direction);

            if (canPlaceVine(level, currentPos) &&
                    hasSolidNeighbor(level, currentPos)) {
                setter.set(currentPos, ModBlocks.HELHEIMVINE.get().defaultBlockState());
            } else {
                break;
            }
        }
    }

    // 辅助方法
    private boolean canPlaceLeaf(LevelSimulatedReader level, BlockPos pos) {
        return level.isStateAtPosition(pos, state -> state.isAir());
    }

    private boolean canPlaceVine(LevelSimulatedReader level, BlockPos pos) {
        return level.isStateAtPosition(pos, state ->
                state.isAir() || state.getBlock() == ModBlocks.HELHEIMVINE.get());
    }

    private boolean hasSolidNeighbor(LevelSimulatedReader level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (level.isStateAtPosition(pos.relative(dir), state -> !state.isAir())) {
                return true;
            }
        }
        return false;
    }

    private boolean isLeaf(LevelSimulatedReader level, BlockPos pos) {
        return level.isStateAtPosition(pos, state ->
                state.getBlock() instanceof LeavesBlock);
    }

    @Override
    public int foliageHeight(RandomSource random, int freeTreeHeight, TreeConfiguration config) {
        return 4 + random.nextInt(3); // 4-6格高
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int x, int y, int z, int range, boolean large) {
        return false;
    }
}