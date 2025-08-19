package net.jeffd76.ebonytrees.worldgen;

import net.jeffd76.ebonytrees.EbonyTrees;
import net.jeffd76.ebonytrees.block.ModBlocks;
import net.jeffd76.ebonytrees.worldgen.tree.custom.EbonyFoliagePlacer;
import net.jeffd76.ebonytrees.worldgen.tree.custom.EbonyTrunkPlacer;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> EBONY_KEY = registerKey("ebony");

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {

        register(context, EBONY_KEY, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(ModBlocks.EBONY_LOG.get()),
                new EbonyTrunkPlacer(12, 6, 4),


                BlockStateProvider.simple(ModBlocks.EBONY_LEAVES.get()),
                new EbonyFoliagePlacer(
                        UniformInt.of(7, 9),
                        UniformInt.of(2, 4),
                        10),

                new TwoLayersFeatureSize(1, 0, 2))
                .build());
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(EbonyTrees.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstapContext<ConfiguredFeature<?, ?>> context,
                                                                                          ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
