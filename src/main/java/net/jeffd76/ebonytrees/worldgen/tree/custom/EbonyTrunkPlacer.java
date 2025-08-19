package net.jeffd76.ebonytrees.worldgen.tree.custom;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.jeffd76.ebonytrees.worldgen.tree.ModTrunkPlacerTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.List;
import java.util.function.BiConsumer;

public class EbonyTrunkPlacer extends TrunkPlacer {

    public static final Codec<EbonyTrunkPlacer> CODEC = RecordCodecBuilder.create(ebonyTrunkPlacerInstance ->
            trunkPlacerParts(ebonyTrunkPlacerInstance).apply(ebonyTrunkPlacerInstance, EbonyTrunkPlacer::new));

    // Realistic ebony tree variables
    private static final int MIN_TREE_HEIGHT = 8;
    private static final int MAX_TREE_HEIGHT = 16;
    private static final float BUTTRESS_CHANCE = 1.0f; // Always create buttresses
    private static final int MIN_BUTTRESS_HEIGHT = 1;
    private static final int MAX_BUTTRESS_HEIGHT = 4;
    private static final float BRANCH_CHANCE = 0.05f;
    private static final int MIN_BRANCH_HEIGHT = 12;

    // Top buttress variables
    private static final float TOP_BUTTRESS_CHANCE = 1.0f; // 75% chance per direction
    private static final int MIN_TOP_BUTTRESS_LENGTH = 2;
    private static final int MAX_TOP_BUTTRESS_LENGTH = 3;

    // Root system variables
    private static final float ROOT_CHANCE = 0.8f; // Increased to 100% for testing
    private static final float DIAGONAL_ROOT_CHANCE = 0.25f; // 25% chance for diagonal roots
    private static final int MAX_ROOT_DEPTH = 2;
    private static final int MAX_ROOT_LENGTH = 2;

    // Pre-calculated directions to avoid repeated array creation
    private static final Direction[] HORIZONTAL_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    // Diagonal directions for root placement
    private static final Direction[][] DIAGONAL_DIRECTIONS = {
            {Direction.NORTH, Direction.EAST}, // Northeast
            {Direction.NORTH, Direction.WEST}, // Northwest
            {Direction.SOUTH, Direction.EAST}, // Southeast
            {Direction.SOUTH, Direction.WEST}  // Southwest
    };

    // Flag to check if we're in world generation context (will be determined dynamically)

    public EbonyTrunkPlacer(int pBaseHeight, int pHeightRandA, int pHeightRandB) {
        super(pBaseHeight, pHeightRandA, pHeightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return ModTrunkPlacerTypes.EBONY_TRUNK_PLACER.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter,
                                                            RandomSource pRandom, int pFreeTreeHeight, BlockPos pPos, TreeConfiguration pConfig) {
        // Determine if this is world generation or sapling growth
        // World generation typically has larger free tree heights and uses LevelSimulatedReader
        boolean isWorldGen = determineWorldGenContext(pLevel, pFreeTreeHeight);

        // Set dirt foundation
        setDirtAt(pLevel, pBlockSetter, pRandom, pPos.below(), pConfig);

        // Add root system only for world generation (not sapling growth)
        if (isWorldGen) {
            addRoots(pRandom, pPos, pBlockSetter, pConfig, HORIZONTAL_DIRECTIONS, true);
        }

        // Calculate realistic height with simplified logic
        int baseTrunkHeight = calculateTreeHeight(pFreeTreeHeight, pRandom);

        // Extend trunk 3-4 blocks into canopy
        int trunkExtension = 3 + pRandom.nextInt(2); // 3-4 blocks
        int totalTrunkHeight = baseTrunkHeight + trunkExtension;

        // Pre-determine features to avoid repeated random calls
        boolean hasButtress = pRandom.nextFloat() < BUTTRESS_CHANCE;

        // Generate individual buttress heights for each direction (1-4 blocks each)
        int[] buttressHeights = new int[HORIZONTAL_DIRECTIONS.length];
        for (int i = 0; i < HORIZONTAL_DIRECTIONS.length; i++) {
            buttressHeights[i] = MIN_BUTTRESS_HEIGHT + pRandom.nextInt(MAX_BUTTRESS_HEIGHT - MIN_BUTTRESS_HEIGHT + 1);
        }

        // Pre-generate top buttress configurations
        boolean[] hasTopButtress = new boolean[HORIZONTAL_DIRECTIONS.length];
        int[] topButtressLengths = new int[HORIZONTAL_DIRECTIONS.length];
        for (int i = 0; i < HORIZONTAL_DIRECTIONS.length; i++) {
            hasTopButtress[i] = pRandom.nextFloat() < TOP_BUTTRESS_CHANCE;
            if (hasTopButtress[i]) {
                topButtressLengths[i] = MIN_TOP_BUTTRESS_LENGTH +
                        pRandom.nextInt(MAX_TOP_BUTTRESS_LENGTH - MIN_TOP_BUTTRESS_LENGTH + 1);
            }
        }

        // Single trunk building loop with integrated features
        for (int i = 0; i < totalTrunkHeight; i++) {
            BlockPos currentPos = pPos.above(i);

            // Place main trunk
            placeLog(pLevel, pBlockSetter, pRandom, currentPos, pConfig);

            // Apply trunk features based on height (only for base trunk, not extension)
            if (i < baseTrunkHeight) {
                applyTrunkFeatures(pLevel, pBlockSetter, pRandom, currentPos, pConfig, i, hasButtress, buttressHeights);

                // Place top buttresses in the upper portion of the trunk (last 3 blocks before foliage)
                if (i >= baseTrunkHeight - 10) {
                    addTopButtressesAtHeight(pLevel, pBlockSetter, pRandom, currentPos, pConfig,
                            hasTopButtress, topButtressLengths, baseTrunkHeight - i);
                }
            }
        }

        // Return foliage attachment at the base trunk height (where canopy starts)
        // The extended trunk will be inside the canopy
        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pPos.above(baseTrunkHeight), 0, false));
    }

    /**
     * Determines if we're in world generation context vs sapling growth
     */
    private boolean determineWorldGenContext(LevelSimulatedReader pLevel, int pFreeTreeHeight) {
        // Multiple indicators can help determine the context:

        // 1. World generation typically has much larger free tree heights
        if (pFreeTreeHeight > 20) {
            return true; // Very likely world generation
        }

        // 2. Sapling growth usually has smaller, more constrained heights
        if (pFreeTreeHeight < 8) {
            return false; // Very likely sapling growth
        }

        // 3. Check the type of LevelSimulatedReader
        // World generation often uses specific reader types
        String readerClassName = pLevel.getClass().getSimpleName();

        // Common world generation reader classes
        if (readerClassName.contains("WorldGen") ||
                readerClassName.contains("ChunkGenerator") ||
                readerClassName.contains("FeaturePlaceContext")) {
            return true;
        }

        // Common sapling growth reader classes
        if (readerClassName.contains("Level") && !readerClassName.contains("Simulated")) {
            return false;
        }

        // Default assumption based on height for edge cases
        return pFreeTreeHeight >= 12;
    }

    /**
     * Adds a root system beneath the tree
     */
    public void addRoots(RandomSource rand, BlockPos pos,
                         BiConsumer<BlockPos, BlockState> consumer, TreeConfiguration baseTreeFeatureConfig,
                         Direction[] extendedDirs, boolean isWorldGen) {
        BlockState rootState = baseTreeFeatureConfig.trunkProvider.getState(rand, pos);

        if (rand.nextFloat() < ROOT_CHANCE) {
            // Start placing roots from below the trunk base
            BlockPos rootStartPos = pos.below();

            // Place initial root block
            consumer.accept(rootStartPos, rootState);

            if (isWorldGen) {
                // Extend roots downward (up to MAX_ROOT_DEPTH blocks)
                BlockPos currentPos = rootStartPos;
                for (int i = 0; i < MAX_ROOT_DEPTH; i++) {
                    currentPos = currentPos.below();
                    consumer.accept(currentPos, rootState);
                }

                // Add lateral roots extending from various depths
                for (Direction direction : extendedDirs) {
                    // Place roots at different depths for more natural look
                    placeRotatedRoot(rand, rootStartPos.relative(direction),
                            consumer, baseTreeFeatureConfig, direction);

                    // Sometimes add deeper lateral roots
                    if (rand.nextFloat() < 0.6f) {
                        placeRotatedRoot(rand, rootStartPos.below().relative(direction),
                                consumer, baseTreeFeatureConfig, direction);
                    }
                }

                // Add diagonal roots with 25% chance each
                for (Direction[] diagonalPair : DIAGONAL_DIRECTIONS) {
                    if (rand.nextFloat() < DIAGONAL_ROOT_CHANCE) {
                        placeDiagonalRoot(rand, rootStartPos, consumer, baseTreeFeatureConfig, diagonalPair);
                    }
                }

            }
        }

    }

    /**
     * Places a root extending in a specific direction
     */
    private void placeRotatedRoot(RandomSource rand, BlockPos startPos,
                                  BiConsumer<BlockPos, BlockState> consumer, TreeConfiguration config,
                                  Direction direction) {
        // Create root state with proper axis alignment
        BlockState rootState = config.trunkProvider.getState(rand, startPos)
                .setValue(RotatedPillarBlock.AXIS, direction.getAxis());

        // Extend root 1-3 blocks in the given direction
        int rootLength = 1 + rand.nextInt(MAX_ROOT_LENGTH);

        for (int i = 1; i <= rootLength; i++) {
            BlockPos rootPos = startPos.relative(direction, i);

            // Place root block - be less restrictive about placement
            consumer.accept(rootPos, rootState);

            // Occasionally place a root going down from lateral roots
            if (i == rootLength && rand.nextFloat() < 0.4f) {
                BlockPos deeperRoot = rootPos.below();
                consumer.accept(deeperRoot, rootState);
            }
        }
    }

    /**
     * Adds top buttresses at specific height during trunk building
     */
    private void addTopButtressesAtHeight(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter,
                                          RandomSource pRandom, BlockPos currentPos, TreeConfiguration pConfig,
                                          boolean[] hasTopButtress, int[] topButtressLengths, int remainingHeight) {

        // Check each direction for top buttress placement
        for (int dirIndex = 0; dirIndex < HORIZONTAL_DIRECTIONS.length; dirIndex++) {
            if (hasTopButtress[dirIndex] && remainingHeight <= topButtressLengths[dirIndex]) {
                Direction direction = HORIZONTAL_DIRECTIONS[dirIndex];
                BlockPos buttressPos = currentPos.relative(direction);

                // Place buttress block if position is suitable
                if (pLevel.isStateAtPosition(buttressPos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
                    placeLog(pLevel, pBlockSetter, pRandom, buttressPos, pConfig);
                }
            }
        }
    }



    /**
     * Places a diagonal root extending in two directions (e.g., northeast, southwest)
     */
    private void placeDiagonalRoot(RandomSource rand, BlockPos startPos,
                                   BiConsumer<BlockPos, BlockState> consumer, TreeConfiguration config,
                                   Direction[] diagonalPair) {
        // Create root state - use the first direction's axis for consistency
        BlockState rootState = config.trunkProvider.getState(rand, startPos)
                .setValue(RotatedPillarBlock.AXIS, diagonalPair[0].getAxis());

        // Diagonal root length is typically shorter (1-2 blocks)
        int rootLength = 1 + rand.nextInt(2);

        BlockPos currentPos = startPos;
        for (int i = 1; i <= rootLength; i++) {
            // Move diagonally by applying both directions
            currentPos = currentPos.relative(diagonalPair[0]).relative(diagonalPair[1]);

            // Place root block
            consumer.accept(currentPos, rootState);

            // Occasionally place a root going down from the diagonal root tip
            if (i == rootLength && rand.nextFloat() < 0.3f) {
                BlockPos deeperRoot = currentPos.below();
                consumer.accept(deeperRoot, rootState);
            }
        }
    }

    /**
     * Simplified height calculation
     */
    private int calculateTreeHeight(int pFreeTreeHeight, RandomSource pRandom) {
        int baseHeight = Math.max(MIN_TREE_HEIGHT, Math.min(MAX_TREE_HEIGHT,
                pFreeTreeHeight + pRandom.nextInt(heightRandA) + pRandom.nextInt(heightRandB)));

        // Add variation and clamp in one operation
        return Math.max(MIN_TREE_HEIGHT, Math.min(MAX_TREE_HEIGHT,
                baseHeight + pRandom.nextInt(-2, 3)));
    }

    /**
     * Unified method to apply all trunk features based on height
     */
    private void applyTrunkFeatures(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter,
                                    RandomSource pRandom, BlockPos pPos, TreeConfiguration pConfig,
                                    int currentHeight, boolean hasButtress, int[] buttressHeights) {

        // Buttressed base - now with individual heights per direction
        if (hasButtress) {
            addButtressFeatures(pLevel, pBlockSetter, pRandom, pPos, pConfig, currentHeight, buttressHeights);
        }

        // Minimal branching
        if (currentHeight >= MIN_BRANCH_HEIGHT && currentHeight % 3 == 0 &&
                pRandom.nextFloat() < BRANCH_CHANCE) {
            addMinimalBranches(pLevel, pBlockSetter, pRandom, pPos, pConfig);
        }
    }

    /**
     * Enhanced buttress creation with individual heights per direction
     */
    private void addButtressFeatures(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter,
                                     RandomSource pRandom, BlockPos pPos, TreeConfiguration pConfig,
                                     int currentHeight, int[] buttressHeights) {

        // Check each direction individually with its own height
        for (int dirIndex = 0; dirIndex < HORIZONTAL_DIRECTIONS.length; dirIndex++) {
            Direction direction = HORIZONTAL_DIRECTIONS[dirIndex];
            int buttressHeight = buttressHeights[dirIndex];

            // Only place buttress if current height is within this direction's buttress height
            if (currentHeight < buttressHeight) {
                BlockPos buttressPos = pPos.relative(direction);
                if (pLevel.isStateAtPosition(buttressPos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
                    placeLog(pLevel, pBlockSetter, pRandom, buttressPos, pConfig);
                }
            }
        }
    }

    /**
     * Streamlined minimal branching
     */
    private void addMinimalBranches(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter,
                                    RandomSource pRandom, BlockPos pPos, TreeConfiguration pConfig) {

        int branchCount = pRandom.nextInt(3); // 0, 1, or 2 branches

        for (int b = 0; b < branchCount; b++) {
            Direction direction = HORIZONTAL_DIRECTIONS[pRandom.nextInt(4)];
            int branchLength = pRandom.nextInt(2, 4); // 2-3 blocks

            // Create branch in single direction
            createBranchInDirection(pLevel, pBlockSetter, pRandom, pPos, pConfig, direction, branchLength);
        }
    }

    /**
     * Optimized branch creation in specific direction
     */
    private void createBranchInDirection(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter,
                                         RandomSource pRandom, BlockPos startPos, TreeConfiguration pConfig,
                                         Direction direction, int length) {

        // Pre-calculate branch state with correct axis
        BlockState branchTemplate = pConfig.trunkProvider.getState(pRandom, startPos)
                .setValue(RotatedPillarBlock.AXIS, direction.getAxis());

        for (int j = 1; j <= length; j++) {
            BlockPos branchPos = startPos.relative(direction, j);
            if (pLevel.isStateAtPosition(branchPos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
                pBlockSetter.accept(branchPos, branchTemplate);
            } else {
                break; // Stop if we hit an obstacle
            }
        }
    }
}