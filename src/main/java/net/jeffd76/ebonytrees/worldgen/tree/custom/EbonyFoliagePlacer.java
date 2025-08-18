package net.jeffd76.ebonytrees.worldgen.tree.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.jeffd76.ebonytrees.worldgen.tree.ModFoliagePlacers;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class EbonyFoliagePlacer extends FoliagePlacer {
    public static final Codec<EbonyFoliagePlacer> CODEC = RecordCodecBuilder.create(ebonyFoliagePlacerInstance ->
            foliagePlacerParts(ebonyFoliagePlacerInstance).and(Codec.intRange(0, 16).fieldOf("height")
                    .forGetter(fp -> fp.height)).apply(ebonyFoliagePlacerInstance, EbonyFoliagePlacer::new));

    // Realistic ebony tree foliage variables
    private static final int MIN_FOLIAGE_HEIGHT = 6;
    private static final int MAX_FOLIAGE_HEIGHT = 12;
    private static final int MIN_RADIUS = 5;
    private static final int MAX_RADIUS = 8;
    private static final float DENSITY_FACTOR = 0.9f;

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

        // Track branch positions for exposed log coverage
        java.util.Set<net.minecraft.core.BlockPos> branchPositions = new java.util.HashSet<>();

        // Create dense, rounded canopy characteristic of ebony trees
        for (int layer = 0; layer < actualFoliageHeight; layer++) {
            int currentRadius = calculateLayerRadius(layer, actualFoliageHeight, baseRadius, pRandom);
            net.minecraft.core.BlockPos layerPos = pAttachment.pos().above(layer - pOffset);

            // Consolidated branch creation based on layer conditions
            createLayerBranches(pLevel, pBlockSetter, pRandom, pConfig, layerPos,
                    currentRadius, layer, actualFoliageHeight, branchPositions);

            // Create the foliage layer
            createDenseRoundedLayer(pLevel, pBlockSetter, pRandom, pConfig,
                    layerPos, currentRadius);
        }

        // Connect lower canopy branches to main trunk with diagonal supports
        connectBranchesToTrunk(pLevel, pBlockSetter, pRandom, pConfig, branchPositions,
                pAttachment.pos(), pOffset);

        // Cover any exposed branches in upper 3/4 of canopy
        coverExposedBranches(pLevel, pBlockSetter, pRandom, pConfig, branchPositions
        );

        // Add sparse undercanopy
        if (pRandom.nextFloat() < 0.6f) {
            createSparseUndercanopy(pLevel, pBlockSetter, pRandom, pConfig,
                    pAttachment.pos().below(1), baseRadius - 3);
        }
    }

    /**
     * Unified branch creation method that handles all branch types based on layer conditions
     */
    private void createLayerBranches(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom,
                                     TreeConfiguration pConfig, net.minecraft.core.BlockPos layerCenter,
                                     int radius, int layer, int totalHeight, java.util.Set<net.minecraft.core.BlockPos> branchPositions) {

        // Skip branches in top layers to prevent exposure
        if (layer >= totalHeight - 2) return;

        // Only create branches every 4th layer
        if (layer % 4 != 0) return;

        boolean isBottomHalf = layer < totalHeight / 2;

        // Reduced branch count for cleaner look while maintaining coverage
        int branchCount = Math.max(4, radius / 2); // Back to more conservative count

        // Only add extra branches for very large bottom canopies
        if (isBottomHalf && radius >= 8) {
            branchCount = Math.max(6, radius * 2 / 3); // Reduced from radius * 3
        }

        // Create branches for this layer - single pass only
        createBranches(pLevel, pBlockSetter, pRandom, pConfig, layerCenter,
                radius, layer, totalHeight, branchCount, false, branchPositions);
    }

    /**
     * Optimized branch creation with pre-calculated values
     */
    private void createBranches(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom,
                                TreeConfiguration pConfig, net.minecraft.core.BlockPos layerCenter,
                                int radius, int layer, int totalHeight, int branchCount, boolean isDenseMode,
                                java.util.Set<net.minecraft.core.BlockPos> branchPositions) {

        // Pre-calculate all values to avoid repeated calculations
        boolean isUpperCanopy = layer >= totalHeight * 0.25f;
        int maxBranchLength = layer > totalHeight * 0.7f ? radius - 1 : Math.min(radius + 1, 6);
        int targetDistance = Math.min(maxBranchLength, radius - 1);
        double angleStep = (2 * Math.PI) / branchCount;

        for (int i = 0; i < branchCount; i++) {
            double angle = angleStep * i + pRandom.nextDouble() * 0.2 - 0.1;
            double cosAngle = Math.cos(angle);
            double sinAngle = Math.sin(angle);

            // Create branch segments efficiently
            for (int j = 2; j <= targetDistance; j++) {
                int branchX = (int) Math.round(cosAngle * j);
                int branchZ = (int) Math.round(sinAngle * j);

                // Quick bounds check before creating BlockPos
                double distanceSquared = branchX * branchX + branchZ * branchZ;
                if (distanceSquared > radius * radius * 0.81) continue; // radius * 0.9 squared

                net.minecraft.core.BlockPos branchPos = layerCenter.offset(branchX, 0, branchZ);

                if (pLevel.isStateAtPosition(branchPos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
                    placeBranchBlockOptimized(pBlockSetter, pRandom, pConfig, branchPos, branchX, branchZ);

                    // Only track upper canopy branches
                    if (isUpperCanopy) {
                        branchPositions.add(branchPos);
                    }
                }
            }
        }
    }

    /**
     * Optimized branch block placement with minimal object creation
     */
    private void placeBranchBlockOptimized(FoliageSetter pBlockSetter, RandomSource pRandom, TreeConfiguration pConfig,
                                           net.minecraft.core.BlockPos branchPos, int branchX, int branchZ) {

        net.minecraft.world.level.block.state.BlockState branchState = pConfig.trunkProvider.getState(pRandom, branchPos);

        // Inline axis calculation - avoid method calls
        if (branchState.hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)) {
            net.minecraft.core.Direction.Axis axis = Math.abs(branchX) > Math.abs(branchZ) ?
                    net.minecraft.core.Direction.Axis.X : net.minecraft.core.Direction.Axis.Z;
            branchState = branchState.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, axis);
        }

        pBlockSetter.set(branchPos, branchState);
    }

    /**
     * Streamlined lowest level connection with minimal calculations
     */
    private void connectBranchesToTrunk(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom,
                                        TreeConfiguration pConfig, java.util.Set<net.minecraft.core.BlockPos> branchPositions,
                                        net.minecraft.core.BlockPos trunkBase, int offset) {

        if (branchPositions.isEmpty()) return;

        // Find lowest branch height efficiently
        int lowestHeight = Integer.MAX_VALUE;
        int trunkBaseY = trunkBase.getY();

        for (net.minecraft.core.BlockPos branchPos : branchPositions) {
            int height = branchPos.getY() - trunkBaseY + offset;
            if (height < lowestHeight) lowestHeight = height;
        }

        // Process only lowest level branches
        int maxHeight = lowestHeight + 1;
        net.minecraft.core.BlockPos trunkAtLevel = trunkBase.above(lowestHeight);

        for (net.minecraft.core.BlockPos branchPos : branchPositions) {
            int branchHeight = branchPos.getY() - trunkBaseY + offset;

            if (branchHeight <= maxHeight) {
                // Quick distance check
                int dx = branchPos.getX() - trunkBase.getX();
                int dz = branchPos.getZ() - trunkBase.getZ();

                if (dx * dx + dz * dz >= 1) { // Distance >= 1.0 (squared)
                    createLowestLevelConnectionOptimized(pLevel, pBlockSetter, pRandom, pConfig,
                            branchPos, trunkAtLevel);
                }
            }
        }
    }

    /**
     * Optimized exposed branch coverage with minimal set operations
     */
    private void coverExposedBranches(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom,
                                      TreeConfiguration pConfig, java.util.Set<net.minecraft.core.BlockPos> branchPositions) {

        // Early exit if no branches to process
        if (branchPositions.isEmpty()) return;

        // Pre-calculate offsets for 6 directions to avoid repeated object creation
        int[] dx = {0, 0, 0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0, 0, 0};
        int[] dz = {0, 0, 1, -1, 0, 0};

        for (net.minecraft.core.BlockPos branchPos : branchPositions) {
            // Check all 6 directions efficiently
            for (int i = 0; i < 6; i++) {
                net.minecraft.core.BlockPos adjacentPos = branchPos.offset(dx[i], dy[i], dz[i]);

                if (pLevel.isStateAtPosition(adjacentPos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
                    tryPlaceLeaf(pLevel, pBlockSetter, pRandom, pConfig, adjacentPos);
                }
            }
        }
    }

    /**
     * Optimized diagonal connection with pre-calculated state
     */
    private void createLowestLevelConnectionOptimized(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom,
                                                      TreeConfiguration pConfig, net.minecraft.core.BlockPos branchPos,
                                                      net.minecraft.core.BlockPos trunkPos) {

        // Pre-calculate direction and distance
        int dx = branchPos.getX() - trunkPos.getX();
        int dz = branchPos.getZ() - trunkPos.getZ();
        int stepX = Integer.compare(dx, 0);
        int stepZ = Integer.compare(dz, 0);
        int maxSteps = Math.max(Math.abs(dx), Math.abs(dz)) - 1;

        // Pre-determine axis for efficiency
        boolean useXAxis = Math.abs(dx) >= Math.abs(dz);
        net.minecraft.core.Direction.Axis axis = useXAxis ?
                net.minecraft.core.Direction.Axis.X : net.minecraft.core.Direction.Axis.Z;

        // Create diagonal connection
        for (int step = 1; step <= maxSteps; step++) {
            net.minecraft.core.BlockPos connectionPos = trunkPos.offset(stepX * step, 0, stepZ * step);

            if (pLevel.isStateAtPosition(connectionPos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
                net.minecraft.world.level.block.state.BlockState logState = pConfig.trunkProvider.getState(pRandom, connectionPos);

                if (logState.hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)) {
                    logState = logState.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, axis);
                }

                pBlockSetter.set(connectionPos, logState);
            }
        }
    }

    /**
     * Calculates the radius for each layer to create a rounded canopy shape
     */
    private int calculateLayerRadius(int layer, int totalHeight, int baseRadius, RandomSource pRandom) {
        float heightProgress = (float) layer / (float) totalHeight;

        float radiusMultiplier;
        if (heightProgress < 0.3f) {
            radiusMultiplier = 0.4f + (heightProgress / 0.3f) * 0.5f;
        } else if (heightProgress < 0.7f) {
            radiusMultiplier = 0.9f + (pRandom.nextFloat() * 0.2f);
        } else {
            float topProgress = (heightProgress - 0.7f) / 0.3f;
            radiusMultiplier = 0.9f * (1.0f - topProgress * topProgress);
        }

        int calculatedRadius = Math.round(baseRadius * radiusMultiplier);
        return Math.max(1, calculatedRadius + pRandom.nextInt(-1, 2));
    }

    /**
     * Creates a dense, rounded layer of foliage with connected leaf placement
     */
    private void createDenseRoundedLayer(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom,
                                         TreeConfiguration pConfig, net.minecraft.core.BlockPos layerCenter, int radius) {

        // First pass: determine which positions should have leaves
        boolean[][] shouldPlaceLeaf = new boolean[radius * 2 + 1][radius * 2 + 1];

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                double adjustedDistance = distance + (pRandom.nextFloat() - 0.5f) * 0.8;

                if (adjustedDistance <= radius) {
                    float centerDistance = (float) (adjustedDistance / radius);
                    float placementChance = DENSITY_FACTOR * (1.0f - centerDistance * 0.3f);

                    if (centerDistance < 0.5f) {
                        placementChance = Math.max(placementChance, 0.95f);
                    }

                    if (pRandom.nextFloat() < placementChance) {
                        shouldPlaceLeaf[x + radius][z + radius] = true;
                    }
                }
            }
        }

        // Second pass: ensure connectivity by filling gaps between isolated leaf clusters
        ensureLeafConnectivity(shouldPlaceLeaf, radius);

        // Third pass: actually place the leaves
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (shouldPlaceLeaf[x + radius][z + radius]) {
                    net.minecraft.core.BlockPos leafPos = layerCenter.offset(x, 0, z);
                    tryPlaceLeaf(pLevel, pBlockSetter, pRandom, pConfig, leafPos);
                }
            }
        }
    }

    /**
     * Ensures leaf connectivity by filling gaps between leaf clusters
     */
    private void ensureLeafConnectivity(boolean[][] shouldPlaceLeaf, int radius) {
        int size = radius * 2 + 1;
        boolean[][] connected = new boolean[size][size];

        // Mark center area as connected (trunk area)
        int centerIdx = radius;
        for (int x = Math.max(0, centerIdx - 1); x <= Math.min(size - 1, centerIdx + 1); x++) {
            for (int z = Math.max(0, centerIdx - 1); z <= Math.min(size - 1, centerIdx + 1); z++) {
                connected[x][z] = true;
            }
        }

        // Iteratively expand connected areas and fill gaps
        boolean changed = true;
        int iterations = 0;
        while (changed && iterations < 10) { // Prevent infinite loops
            changed = false;
            iterations++;

            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    if (shouldPlaceLeaf[x][z] && !connected[x][z]) {
                        // Check if this leaf is adjacent to a connected area
                        if (hasConnectedNeighbor(connected, x, z, size)) {
                            connected[x][z] = true;
                            changed = true;
                        }
                    }
                }
            }
        }

        // Fill small gaps between connected leaf areas to prevent isolation
        for (int x = 1; x < size - 1; x++) {
            for (int z = 1; z < size - 1; z++) {
                if (!shouldPlaceLeaf[x][z]) {
                    // Check if placing a leaf here would connect isolated areas
                    int connectedNeighbors = 0;
                    if (x > 0 && connected[x-1][z]) connectedNeighbors++;
                    if (x < size-1 && connected[x+1][z]) connectedNeighbors++;
                    if (z > 0 && connected[x][z-1]) connectedNeighbors++;
                    if (z < size-1 && connected[x][z+1]) connectedNeighbors++;

                    // If this position would connect multiple areas or fill a small gap
                    if (connectedNeighbors >= 2) {
                        double distanceFromCenter = Math.sqrt(Math.pow(x - centerIdx, 2) + Math.pow(z - centerIdx, 2));
                        if (distanceFromCenter <= radius) { // Only within canopy bounds
                            shouldPlaceLeaf[x][z] = true;
                            connected[x][z] = true;
                        }
                    }
                }
            }
        }

        // Remove leaves that are still not connected to prevent isolated decay
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                if (shouldPlaceLeaf[x][z] && !connected[x][z]) {
                    shouldPlaceLeaf[x][z] = false;
                }
            }
        }
    }

    /**
     * Checks if a position has a connected neighbor
     */
    private boolean hasConnectedNeighbor(boolean[][] connected, int x, int z, int size) {
        // Check 4-directional neighbors (cardinal directions)
        if (x > 0 && connected[x-1][z]) return true;
        if (x < size-1 && connected[x+1][z]) return true;
        if (z > 0 && connected[x][z-1]) return true;
        return z < size - 1 && connected[x][z + 1];
    }

    /**
     * Creates sparse foliage below the main canopy with connectivity checking
     */
    private void createSparseUndercanopy(LevelSimulatedReader pLevel, FoliageSetter pBlockSetter, RandomSource pRandom,
                                         TreeConfiguration pConfig, net.minecraft.core.BlockPos centerPos, int radius) {

        // First pass: determine potential leaf positions
        boolean[][] shouldPlaceLeaf = new boolean[radius * 2 + 1][radius * 2 + 1];

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);

                if (distance <= radius && pRandom.nextFloat() < 0.3f) {
                    shouldPlaceLeaf[x + radius][z + radius] = true;
                }
            }
        }

        // Ensure connectivity for undercanopy leaves
        ensureLeafConnectivity(shouldPlaceLeaf, radius);

        // Place the connected leaves
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (shouldPlaceLeaf[x + radius][z + radius]) {
                    net.minecraft.core.BlockPos leafPos = centerPos.offset(x, 0, z);
                    tryPlaceLeaf(pLevel, pBlockSetter, pRandom, pConfig, leafPos);
                }
            }
        }
    }

    @Override
    public int foliageHeight(RandomSource pRandom, int pHeight, TreeConfiguration pConfig) {
        int baseHeight = Math.max(MIN_FOLIAGE_HEIGHT, this.height);
        return Math.min(MAX_FOLIAGE_HEIGHT, baseHeight + pRandom.nextInt(3));
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource pRandom, int pLocalX, int pLocalY, int pLocalZ, int pRange, boolean pLarge) {
        double distance = Math.sqrt(pLocalX * pLocalX + pLocalZ * pLocalZ);
        double maxDistance = pRange + (pRandom.nextFloat() - 0.5f) * 1.5;

        if (distance > maxDistance) {
            return true;
        }

        return pRandom.nextFloat() < 0.05f;
    }
}