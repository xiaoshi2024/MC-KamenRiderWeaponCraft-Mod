package com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.dimension;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.biome.ModBiomes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.OptionalLong;

public class ModDimensions {
    // 定义自定义维度的 ResourceKey
    public static final ResourceKey<LevelStem> HELHEIM_DIMENSION_KEY = ResourceKey.create(
            Registries.LEVEL_STEM, new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "helheim")
    );

    public static final ResourceKey<Level> HELHEIM_LEVEL_KEY = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "helheim")
    );

    public static final ResourceKey<DimensionType> HELHEIM_DIMENSION_TYPE = ResourceKey.create(
            Registries.DIMENSION_TYPE, new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, "helheim_type")
    );

    /**
     * 注册自定义维度类型
     */
    public static void bootstrapType(BootstapContext<DimensionType> context) {
        context.register(HELHEIM_DIMENSION_TYPE, new DimensionType(
                OptionalLong.empty(), // 固定时间为夜晚
                true, // 有天空光照
                false, // 无天花板
                false, // 非超暖维度
                true, // 自然维度
                1.0, // 坐标缩放
                false, // 床无法工作
                false, // 重生锚无法工作
                -64, // 最小高度
                384, // 总高度
                256, // 逻辑高度
                BlockTags.INFINIBURN_NETHER, // 火烧规则
                BuiltinDimensionTypes.OVERWORLD_EFFECTS, // 使用主世界效果
                0.0f, // 环境光
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)
        ));
    }

    /**
     * 注册自定义维度的 LevelStem
     */
    public static void bootstrapStem(BootstapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        // 使用主世界的噪声生成设置
        Holder.Reference<NoiseGeneratorSettings> overworldNoiseSettings = noiseGenSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD);

        // 创建一个固定生物群系的噪声生成器
        NoiseBasedChunkGenerator chunkGenerator = new NoiseBasedChunkGenerator(
                new FixedBiomeSource(biomeRegistry.getOrThrow(ModBiomes.HELHEIM_BIOME)), // 使用自定义生物群系
                overworldNoiseSettings // 使用主世界的噪声设置
        );

        // 创建自定义维度的 LevelStem
        LevelStem stem = new LevelStem(dimTypes.getOrThrow(ModDimensions.HELHEIM_DIMENSION_TYPE), chunkGenerator);

        context.register(HELHEIM_DIMENSION_KEY, stem);
    }
}