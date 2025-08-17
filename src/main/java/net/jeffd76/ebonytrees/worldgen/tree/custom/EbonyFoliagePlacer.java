package net.jeffd76.ebonytrees.worldgen.tree.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.jeffd76.ebonytrees.worldgen.tree.ModFoliagePlacers;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class EbonyFoliagePlacer extends FoliagePlacer {
    public static final Codec<EbonyFoliagePlacer> CODEC = RecordCodecBuilder.create(ebonyFoliagePlacerInstance ->
            foliagePlacerParts(ebonyFoliagePlacerInstance).and(Codec.intRange(0, 16).fieldOf("height")
                    .forGetter(fp -> fp.height)).apply(ebonyFoliagePlacerInstance, EbonyFoliagePlacer::new));

    // Realistic ebony tree foliage variables
    private static final int MIN_FOLIAGE_HEIGHT = 6;  // Minimum canopy height
    private static final int MAX_FOLIAGE_HEIGHT = 12; // Maximum canopy height
    private static final int MIN_RADIUS = 5;          // Minimum canopy radius
    private static final int MAX_RADIUS = 8;         // Maximum canopy radius (matches real 15-20 block diameter)
    private static final float DENSITY_FACTOR = 0.9f; // Very dense foliage (90% density)
    private static final float ROUNDNESS_FACTOR = 0.8f; // How rounded the canopy should be

    private final int height;

    public EbonyFoliagePlacer(IntProvider pRadius, IntProvider pOffset, int height) {
        super(pRadius, pOffset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return ModFoliagePlacers.EBONY_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom, TreeConfiguration pConfig,
                                 int pMaxFreeTreeHeight, FoliageAttachment pAttachment, int pFoliageHeight, int pFoliageRadius, int pOffset) {

        // Calculate realistic canopy dimensions
        int actualFoliageHeight = Math.max(MIN_FOLIAGE_HEIGHT, Math.min(MAX_FOLIAGE_HEIGHT, pFoliageHeight + pRandom.nextInt(3)));
        int baseRadius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, pFoliageRadius + pRandom.nextInt(2)));

        // Create dense, rounded canopy characteristic of ebony trees
        for (int layer = 0; layer < actualFoliageHeight; layer++) {
            int currentRadius = calculateLayerRadius(layer, actualFoliageHeight, baseRadius, pRandom);

            // Add supporting branches for this layer to prevent leaf decay
            // But avoid the very top layers to prevent exposed logs
            if ((layer % 2 == 0 || currentRadius >= 4) && layer < actualFoliageHeight - 2) { // Don't add branches in top 2 layers
                createSupportingBranches(pLevel, pBlockSetter, pRandom, pConfig,
                        pAttachment.pos().above(layer - pOffset),
                        currentRadius, layer, actualFoliageHeight);
            }

            // Add additional edge support branches for larger canopies to prevent edge decay
            if (currentRadius >= 6 && layer % 3 == 0 && layer < actualFoliageHeight - 1) {
                createEdgeSupportBranches(pLevel, pBlockSetter, pRandom, pConfig,
                        pAttachment.pos().above(layer - pOffset),
                        currentRadius, layer, actualFoliageHeight);
            }

            // Add extra dense branching for bottom half where canopy is largest
            if (layer < actualFoliageHeight / 2 && currentRadius >= 7) {
                createDenseBottomBranches(pLevel, pBlockSetter, pRandom, pConfig,
                        pAttachment.pos().above(layer - pOffset),
                        currentRadius, layer, actualFoliageHeight);
            }

            // Create the foliage layer with realistic density and shape
            createDenseRoundedLayer(pLevel, pBlockSetter, pRandom, pConfig,
                    pAttachment.pos().above(layer - pOffset),
                    currentRadius, layer, actualFoliageHeight, pAttachment.doubleTrunk());
        }

        // Add some sparse leaves below the main canopy for realism
        if (pRandom.nextFloat() < 0.6f) {
            createSparseUndercanopy(pLevel, pBlockSetter, pRandom, pConfig,
                    pAttachment.pos().below(1), baseRadius - 3, pAttachment.doubleTrunk());
        }
    }

    /**
     * Creates very dense branching for the bottom half of the canopy where it's largest
     */
    private void createDenseBottomBranches(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom,
                                           TreeConfiguration pConfig, net.minecraft.core.BlockPos layerCenter,
                                           int radius, int layer, int totalHeight) {

        // Create a grid-like pattern of branches to ensure complete coverage
        int branchCount = Math.max(12, radius * 2); // Very dense branching for bottom layers

        for (int i = 0; i < branchCount; i++) {
            double angle = (2 * Math.PI * i) / branchCount + pRandom.nextDouble() * 0.2 - 0.1;

            // Create branches at multiple distances to ensure no gaps
            for (int distance = 2; distance <= Math.min(4, radius); distance++) {
                int branchX = (int) Math.round(Math.cos(angle) * distance);
                int branchZ = (int) Math.round(Math.sin(angle) * distance);

                net.minecraft.core.BlockPos branchPos = layerCenter.offset(branchX, 0, branchZ);

                // Check if this position needs support (is within the canopy area)
                double distanceFromCenter = Math.sqrt(branchX * branchX + branchZ * branchZ);

                if (distanceFromCenter <= radius * 0.9 && this.isReplaceable(pLevel, branchPos)) {
                    // High chance to place branches in bottom half
                    if (pRandom.nextFloat() < 0.8f) {
                        net.minecraft.world.level.block.state.BlockState branchState = pConfig.trunkProvider.getState(pRandom, branchPos);

                        // Set proper axis
                        if (branchState.hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)) {
                            if (Math.abs(branchX) > Math.abs(branchZ)) {
                                branchState = branchState.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                                        net.minecraft.core.Direction.Axis.X);
                            } else {
                                branchState = branchState.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                                        net.minecraft.core.Direction.Axis.Z);
                            }
                        }

                        pBlockSetter.set(branchPos, branchState);
                    }
                }
            }
        }

        // Add some intermediate branches to fill any remaining gaps
        createIntermediateBranches(pLevel, pBlockSetter, pRandom, pConfig, layerCenter, radius);
    }

    /**
     * Creates intermediate branches to fill gaps between main branches
     */
    private void createIntermediateBranches(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom,
                                            TreeConfiguration pConfig, net.minecraft.core.BlockPos layerCenter, int radius) {

        // Add branches in between the main radial branches
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x == 0 && z == 0) continue; // Skip center

                double distance = Math.sqrt(x * x + z * z);

                // Place branches at strategic intervals to prevent gaps
                if (distance > 2 && distance <= Math.min(4, radius * 0.8) &&
                        (Math.abs(x) % 2 == 0 || Math.abs(z) % 2 == 0) &&
                        pRandom.nextFloat() < 0.4f) {

                    net.minecraft.core.BlockPos branchPos = layerCenter.offset(x, 0, z);

                    if (this.isReplaceable(pLevel, branchPos)) {
                        net.minecraft.world.level.block.state.BlockState branchState = pConfig.trunkProvider.getState(pRandom, branchPos);

                        // Set proper axis
                        if (branchState.hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)) {
                            if (Math.abs(x) > Math.abs(z)) {
                                branchState = branchState.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                                        net.minecraft.core.Direction.Axis.X);
                            } else {
                                branchState = branchState.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                                        net.minecraft.core.Direction.Axis.Z);
                            }
                        }

                        pBlockSetter.set(branchPos, branchState);
                    }
                }
            }
        }
    }

    /**
     * Creates additional branches specifically at canopy edges to prevent edge leaf decay
     */
    private void createEdgeSupportBranches(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom,
                                           TreeConfiguration pConfig, net.minecraft.core.BlockPos layerCenter,
                                           int radius, int layer, int totalHeight) {

        // More branches specifically targeting the outer edge
        int edgeBranchCount = Math.max(5, radius); // At least 6 branches, more for larger canopies

        for (int i = 0; i < edgeBranchCount; i++) {
            // Distribute branches evenly around the circle
            double angle = (2 * Math.PI * i) / edgeBranchCount;

            // Target the edge area (75-95% of radius)
            int targetDistance = Math.max(3, (int) (radius * (0.75f + pRandom.nextFloat() * 0.2f)));

            // Calculate branch end position
            int branchEndX = (int) Math.round(Math.cos(angle) * targetDistance);
            int branchEndZ = (int) Math.round(Math.sin(angle) * targetDistance);

            // Create branch from center outward
            int actualBranchLength = Math.min(4, targetDistance); // Stay within decay range

            for (int j = Math.max(1, targetDistance - 3); j <= targetDistance && j <= 4; j++) {
                int branchX = (int) Math.round(Math.cos(angle) * j);
                int branchZ = (int) Math.round(Math.sin(angle) * j);

                net.minecraft.core.BlockPos branchPos = layerCenter.offset(branchX, 0, branchZ);

                // Only place if it won't be exposed
                if (this.isReplaceable(pLevel, branchPos)) {
                    double distanceFromCenter = Math.sqrt(branchX * branchX + branchZ * branchZ);

                    // Only place branches that are well within the expected leaf coverage
                    if (distanceFromCenter <= radius * 0.85) {
                        net.minecraft.world.level.block.state.BlockState branchState = pConfig.trunkProvider.getState(pRandom, branchPos);

                        // Set proper axis
                        if (branchState.hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)) {
                            if (Math.abs(branchX) > Math.abs(branchZ)) {
                                branchState = branchState.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                                        net.minecraft.core.Direction.Axis.X);
                            } else {
                                branchState = branchState.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                                        net.minecraft.core.Direction.Axis.Z);
                            }
                        }

                        pBlockSetter.set(branchPos, branchState);
                    }
                }
            }
        }
    }

    /**
     * Helper method to check if a position can be replaced
     */
    private boolean isReplaceable(LevelSimulatedReader pLevel, net.minecraft.core.BlockPos pPos) {
        return pLevel.isStateAtPosition(pPos, state -> state.canBeReplaced());
    }

    /**
     * Creates supporting branches to prevent leaf decay
     */
    private void createSupportingBranches(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom,
                                          TreeConfiguration pConfig, net.minecraft.core.BlockPos layerCenter,
                                          int radius, int layer, int totalHeight) {

        // Reduce branch length for higher layers to prevent exposure
        float heightProgress = (float) layer / (float) totalHeight;
        int maxBranchLength = heightProgress > 0.7f ? Math.max(2, radius / 2) : Math.min(4, radius - 1);

        // Number of branches depends on radius and layer
        int branchCount = Math.min(8, Math.max(4, radius / 2 + pRandom.nextInt(2))); // Increased branch count

        for (int i = 0; i < branchCount; i++) {
            // Distribute branches evenly around the circle
            double angle = (2 * Math.PI * i) / branchCount + pRandom.nextDouble() * 0.3 - 0.15;

            // Branch length varies with radius but stays within leaf decay range
            int branchLength = Math.min(maxBranchLength, Math.max(1, radius / 2 + pRandom.nextInt(2)));

            for (int j = 1; j <= branchLength; j++) {
                // Calculate branch position
                int branchX = (int) Math.round(Math.cos(angle) * j);
                int branchZ = (int) Math.round(Math.sin(angle) * j);

                net.minecraft.core.BlockPos branchPos = layerCenter.offset(branchX, 0, branchZ);

                // Only place branch if it will be covered by leaves
                if (this.isReplaceable(pLevel, branchPos) && willBeCoveredByLeaves(branchPos, layerCenter, radius, layer, totalHeight, pRandom)) {
                    // Use trunk provider to get consistent wood type
                    net.minecraft.world.level.block.state.BlockState branchState = pConfig.trunkProvider.getState(pRandom, branchPos);

                    // Set proper axis for horizontal branches
                    if (branchState.hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)) {
                        // Determine the primary axis of the branch
                        if (Math.abs(branchX) > Math.abs(branchZ)) {
                            branchState = branchState.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                                    net.minecraft.core.Direction.Axis.X);
                        } else {
                            branchState = branchState.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                                    net.minecraft.core.Direction.Axis.Z);
                        }
                    }

                    pBlockSetter.set(branchPos, branchState);
                }
            }
        }
    }

    /**
     * Checks if a branch position will be covered by leaves to prevent exposed logs
     */
    private boolean willBeCoveredByLeaves(net.minecraft.core.BlockPos branchPos, net.minecraft.core.BlockPos layerCenter,
                                          int radius, int layer, int totalHeight, RandomSource pRandom) {

        double distance = Math.sqrt(Math.pow(branchPos.getX() - layerCenter.getX(), 2) +
                Math.pow(branchPos.getZ() - layerCenter.getZ(), 2));

        // If branch is well within the radius, it should be covered
        if (distance < radius * 0.8) {
            return true;
        }

        // For branches near the edge, check if we're in upper layers where radius shrinks
        float heightProgress = (float) layer / (float) totalHeight;
        if (heightProgress > 0.6f) {
            // In upper layers, be more conservative
            return distance < radius * 0.6;
        }

        return distance < radius * 0.9;
    }

    /**
     * Calculates the radius for each layer to create a rounded canopy shape
     */
    private int calculateLayerRadius(int layer, int totalHeight, int baseRadius, RandomSource pRandom) {
        // Create a rounded/elliptical shape - wider in middle, narrower at top/bottom
        float heightProgress = (float) layer / (float) totalHeight;

        // Use a curve that creates a rounded top (like real ebony trees)
        float radiusMultiplier;
        if (heightProgress < 0.3f) {
            // Bottom section - starts smaller
            radiusMultiplier = 0.4f + (heightProgress / 0.3f) * 0.5f;
        } else if (heightProgress < 0.7f) {
            // Middle section - fullest part
            radiusMultiplier = 0.9f + (pRandom.nextFloat() * 0.2f);
        } else {
            // Top section - tapers to point
            float topProgress = (heightProgress - 0.7f) / 0.3f;
            radiusMultiplier = 0.9f * (1.0f - topProgress * topProgress); // Squared for rounded top
        }

        int calculatedRadius = Math.round(baseRadius * radiusMultiplier);
        return Math.max(1, calculatedRadius + pRandom.nextInt(-1, 2)); // Add small variation
    }

    /**
     * Creates a dense, rounded layer of foliage
     */
    private void createDenseRoundedLayer(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom,
                                         TreeConfiguration pConfig, net.minecraft.core.BlockPos layerCenter, int radius,
                                         int layer, int totalHeight, boolean doubleTrunk) {

        // Create circular/rounded foliage pattern
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Calculate distance from center
                double distance = Math.sqrt(x * x + z * z);

                // Create rounded edges instead of perfect circles
                double adjustedDistance = distance + (pRandom.nextFloat() - 0.5f) * 0.8;

                if (adjustedDistance <= radius) {
                    // Higher chance to place leaves closer to center (dense canopy)
                    float centerDistance = (float) (adjustedDistance / radius);
                    float placementChance = DENSITY_FACTOR * (1.0f - centerDistance * 0.3f);

                    // Ensure very dense core
                    if (centerDistance < 0.5f) {
                        placementChance = Math.max(placementChance, 0.95f);
                    }

                    if (pRandom.nextFloat() < placementChance) {
                        net.minecraft.core.BlockPos leafPos = layerCenter.offset(x, 0, z);
                        this.tryPlaceLeaf(pLevel, pBlockSetter, pRandom, pConfig, leafPos);
                    }
                }
            }
        }
    }

    /**
     * Creates sparse foliage below the main canopy (like hanging branches)
     */
    private void createSparseUndercanopy(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom,
                                         TreeConfiguration pConfig, net.minecraft.core.BlockPos centerPos, int radius, boolean doubleTrunk) {

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);

                if (distance <= radius && pRandom.nextFloat() < 0.3f) { // 30% chance for sparse undergrowth
                    net.minecraft.core.BlockPos leafPos = centerPos.offset(x, 0, z);
                    this.tryPlaceLeaf(pLevel, pBlockSetter, pRandom, pConfig, leafPos);
                }
            }
        }
    }

    @Override
    public int foliageHeight(RandomSource pRandom, int pHeight, TreeConfiguration pConfig) {
        // Return realistic foliage height based on tree characteristics
        int baseHeight = Math.max(MIN_FOLIAGE_HEIGHT, this.height);
        return Math.min(MAX_FOLIAGE_HEIGHT, baseHeight + pRandom.nextInt(3));
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource pRandom, int pLocalX, int pLocalY, int pLocalZ, int pRange, boolean pLarge) {
        // Custom skip logic for more natural, dense canopy
        double distance = Math.sqrt(pLocalX * pLocalX + pLocalZ * pLocalZ);
        double maxDistance = pRange + (pRandom.nextFloat() - 0.5f) * 1.5;

        // Less skipping for dense ebony canopy, but some variation for naturalness
        if (distance > maxDistance) {
            return true;
        }

        // Very rarely skip interior locations to maintain density
        return pRandom.nextFloat() < 0.05f; // Only 5% skip chance for interior
    }
}