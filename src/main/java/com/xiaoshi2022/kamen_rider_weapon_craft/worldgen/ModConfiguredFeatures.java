package com.xiaoshi2022.kamen_rider_weapon_craft.worldgen;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.custom.PineFoliagePlacer;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.custom.PineTrunkPlacer;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> RIDER_FORGE_ALLOY_MINERAL_KEY = registerKey("rider_forge_alloy_mineral");

    public static final ResourceKey<ConfiguredFeature<?, ?>> PINE_KEY = registerKey("pine");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HELHEIM_PLANT_KEY = registerKey("helheim_plant");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HELHEIM_PLANT_2_KEY = registerKey("helheim_plant_2");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HELHEIM_PLANT_3_KEY = registerKey("helheim_plant_3");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HELHEIM_PLANT_4_KEY = registerKey("helheim_plant_4");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HELHEIM_VINE_KEY = registerKey("helheimvine");

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        //配置骑士合金矿石
        RuleTest stoneReplace = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);

        List<OreConfiguration.TargetBlockState> overworldBlockSilverOres = List
                .of(OreConfiguration.target(stoneReplace, ModBlocks.RIDERFORGINGALLOYMINERAL.get().defaultBlockState()));
        register(context, RIDER_FORGE_ALLOY_MINERAL_KEY, Feature.ORE, new OreConfiguration(overworldBlockSilverOres, 10));


        // 注册松树配置特征
        register(context, PINE_KEY, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.PINE_LOG.get()), // 树干
                new PineTrunkPlacer(5, 4, 3), // 自定义树干生成器
                BlockStateProvider.simple(ModBlocks.PINE_LEAVES.get()), // 树叶
                new PineFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2)), // 自定义树叶生成器
                new TwoLayersFeatureSize(1, 0, 2) // 树的大小
        ).build());

        // 注册赫尔海姆植物（逐个注册）
        register(context, HELHEIM_PLANT_KEY, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.HELHEIM_PLANT.get())));
        register(context, HELHEIM_PLANT_2_KEY, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.HELHEIM_PLANT_2.get())));
        register(context, HELHEIM_PLANT_3_KEY, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.HELHEIM_PLANT_3.get())));
        register(context, HELHEIM_PLANT_4_KEY, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.HELHEIM_PLANT_4.get())));
//        register(context, HELHEIM_VINE_KEY, Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.HELHEIMVINE.get())));

        // 使用 RandomPatchConfiguration 增加藤蔓生成率
        register(context, HELHEIM_VINE_KEY, Feature.RANDOM_PATCH, new RandomPatchConfiguration(
                64, // 尝试次数
                2, // 最小间距
                5, // 最大间距
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.HELHEIMVINE.get())))
        ));

    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
            BootstapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key,
            F feature,
            FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}