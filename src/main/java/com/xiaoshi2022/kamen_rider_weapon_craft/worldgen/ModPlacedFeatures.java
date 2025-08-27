package com.xiaoshi2022.kamen_rider_weapon_craft.worldgen;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.ModConfiguredFeatures;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> RIDER_FORGE_ALLOY_MINERAL_PLACED_KEY = registerKey("rider_forge_alloy_mineral_placed");

    public static final ResourceKey<PlacedFeature> PINE_PLACED_KEY = registerKey("pine_placed");
    public static final ResourceKey<PlacedFeature> HELHEIM_PLANT_PLACED_KEY = registerKey("helheim_plant_placed");
    public static final ResourceKey<PlacedFeature> HELHEIM_PLANT_2_PLACED_KEY = registerKey("helheim_plant_2_placed");
    public static final ResourceKey<PlacedFeature> HELHEIM_PLANT_3_PLACED_KEY = registerKey("helheim_plant_3_placed");
    public static final ResourceKey<PlacedFeature> HELHEIM_PLANT_4_PLACED_KEY = registerKey("helheim_plant_4_placed");
    public static final ResourceKey<PlacedFeature> HELHEIM_VINE_PLACED_KEY = registerKey("helheim_vine_placed_key");

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, RIDER_FORGE_ALLOY_MINERAL_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.RIDER_FORGE_ALLOY_MINERAL_KEY),
                ModOrePlacement.commonOrePlacement(16,HeightRangePlacement.uniform(
                        VerticalAnchor.absolute(-64), VerticalAnchor.absolute(80))
                ));

        // 注册松树的放置逻辑
        register(context, PINE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.PINE_KEY),
                List.of(
                        PlacementUtils.countExtra(3, 0.1f, 2), // 控制生成密度
                        InSquarePlacement.spread(), // 在水平范围内均匀分布
                        HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.aboveBottom(256)), // 使用高度范围确保生成在地形范围内
                        PlacementUtils.filteredByBlockSurvival(Blocks.GRASS) // 确保生成在合适的方块上
                ));

// 注册 Helheim Plant 的放置逻辑
        register(context, HELHEIM_PLANT_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HELHEIM_PLANT_KEY),
                List.of(
                        PlacementUtils.countExtra(10, 0.1F, 1), // 控制生成密度
                        InSquarePlacement.spread(), // 在水平范围内均匀分布
                        HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.aboveBottom(256)), // 使用高度范围确保生成在地形范围内
                        PlacementUtils.filteredByBlockSurvival(Blocks.GRASS) // 确保生成在合适的方块上
                ));
        register(context, HELHEIM_PLANT_2_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HELHEIM_PLANT_2_KEY),
                List.of(
                        PlacementUtils.countExtra(10, 0.1F, 1), // 控制生成密度
                        InSquarePlacement.spread(), // 在水平范围内均匀分布
                        HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.aboveBottom(256)), // 使用高度范围确保生成在地形范围内
                        PlacementUtils.filteredByBlockSurvival(Blocks.GRASS) // 确保生成在合适的方块上
                ));
        register(context, HELHEIM_PLANT_3_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HELHEIM_PLANT_3_KEY),
                List.of(
                        PlacementUtils.countExtra(10, 0.1F, 1), // 控制生成密度
                        InSquarePlacement.spread(), // 在水平范围内均匀分布
                        HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.aboveBottom(256)), // 使用高度范围确保生成在地形范围内
                        PlacementUtils.filteredByBlockSurvival(Blocks.GRASS) // 确保生成在合适的方块上
                ));
        register(context, HELHEIM_PLANT_4_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HELHEIM_PLANT_4_KEY),
                List.of(
                        PlacementUtils.countExtra(10, 0.1F, 1), // 控制生成密度
                        InSquarePlacement.spread(), // 在水平范围内均匀分布
                        HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.aboveBottom(256)), // 使用高度范围确保生成在地形范围内
                        PlacementUtils.filteredByBlockSurvival(Blocks.GRASS) // 确保生成在合适的方块上
                ));
        // 注册 Helheim Vine 的放置逻辑
        register(context, HELHEIM_VINE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.HELHEIM_VINE_KEY),
                List.of(
                        PlacementUtils.countExtra(6, 0.2F, 3), // 增加生成密度，使荆棘效果更明显
                        InSquarePlacement.spread(), // 在水平范围内均匀分布
                        HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(0), VerticalAnchor.aboveBottom(256)), // 在整个高度范围内生成
                        PlacementUtils.filteredByBlockSurvival(Blocks.GRASS), // 确保生成在草地上
                        BiomeFilter.biome() // 只在特定生物群系中生成
                ));


    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, name));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }

    static class ModOrePlacement {

        public static List<PlacementModifier> orePlacement(PlacementModifier modifier, PlacementModifier modifier1) {
            return List.of(modifier,InSquarePlacement.spread(),modifier1,BiomeFilter.biome());
        }
        public static List<PlacementModifier> commonOrePlacement(int i, PlacementModifier modifier) {
            return orePlacement(CountPlacement.of(i),modifier);
        }
        public static List<PlacementModifier> rareOrePlacement(int i, PlacementModifier modifier) {
            return orePlacement(RarityFilter.onAverageOnceEvery(i),modifier);
        }
    }
}