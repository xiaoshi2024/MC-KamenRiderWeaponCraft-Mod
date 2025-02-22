package com.xiaoshi2022.kamen_rider_weapon_craft.worldgen;

import com.xiaoshi2022.kamen_rider_weapon_craft.kamen_rider_weapon_craft;
import com.xiaoshi2022.kamen_rider_weapon_craft.registry.ModBlocks;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.custom.PineFoliagePlacer;
import com.xiaoshi2022.kamen_rider_weapon_craft.worldgen.tree.custom.PineTrunkPlacer;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> PINE_KEY = registerKey("pine");

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {

        register(context, PINE_KEY, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.PINE_LOG.get()),
                new PineTrunkPlacer(5, 4, 3),

                BlockStateProvider.simple(ModBlocks.PINE_LEAVES.get()),
                new PineFoliagePlacer(ConstantInt.of(3), ConstantInt.of(2)),

                new TwoLayersFeatureSize(1, 0, 2)).build());
    }


    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(kamen_rider_weapon_craft.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
