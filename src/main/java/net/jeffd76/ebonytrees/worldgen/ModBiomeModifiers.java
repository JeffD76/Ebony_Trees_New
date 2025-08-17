package net.jeffd76.ebonytrees.worldgen;

import net.jeffd76.ebonytrees.EbonyTrees;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBiomeModifiers {

    // Primary biomes - higher spawn rate
    public static final ResourceKey<BiomeModifier> ADD_TREE_EBONY_DARK_FOREST = registerKey("add_tree_ebony_dark_forest");
    public static final ResourceKey<BiomeModifier> ADD_TREE_EBONY_JUNGLE = registerKey("add_tree_ebony_jungle");

    // Secondary biomes - lower spawn rate
    public static final ResourceKey<BiomeModifier> ADD_TREE_EBONY_BAMBOO_JUNGLE = registerKey("add_tree_ebony_bamboo_jungle");
    public static final ResourceKey<BiomeModifier> ADD_TREE_EBONY_SPARSE_JUNGLE = registerKey("add_tree_ebony_sparse_jungle");

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        // Primary biomes - Medium spawn rate (findable but not overwhelming)
        //context.register(ADD_TREE_EBONY_DARK_FOREST, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
        //        HolderSet.direct(biomes.getOrThrow(Biomes.DARK_FOREST)),
        //        HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.EBONY_PLACED_KEY)),
        //        GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_TREE_EBONY_JUNGLE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.JUNGLE)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.EBONY_PLACED_KEY)),
                GenerationStep.Decoration.VEGETAL_DECORATION));

        // Secondary biomes - Lower spawn rate (accent trees)
        context.register(ADD_TREE_EBONY_BAMBOO_JUNGLE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.BAMBOO_JUNGLE)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.EBONY_PLACED_RARE_KEY)), // Use rare variant
                GenerationStep.Decoration.VEGETAL_DECORATION));

        context.register(ADD_TREE_EBONY_SPARSE_JUNGLE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                HolderSet.direct(biomes.getOrThrow(Biomes.SPARSE_JUNGLE)),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.EBONY_PLACED_RARE_KEY)), // Use rare variant
                GenerationStep.Decoration.VEGETAL_DECORATION));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(EbonyTrees.MOD_ID, name));
    }
}