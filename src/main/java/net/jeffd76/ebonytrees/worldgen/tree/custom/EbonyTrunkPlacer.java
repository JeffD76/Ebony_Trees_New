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
    private static final int MIN_TREE_HEIGHT = 10;
    private static final int MAX_TREE_HEIGHT = 18;
    private static final int TRUNK_THICKNESS_HEIGHT = 6;
    private static final float BUTTRESS_CHANCE = 0.8f;
    private static final int BUTTRESS_HEIGHT = 4;
    private static final float BRANCH_CHANCE = 0.05f;
    private static final int MIN_BRANCH_HEIGHT = 12;

    // Pre-calculated directions to avoid repeated array creation
    private static final Direction[] HORIZONTAL_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

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
        // Set dirt foundation
        setDirtAt(pLevel, pBlockSetter, pRandom, pPos.below(), pConfig);

        // Calculate realistic height with simplified logic
        int baseTrunkHeight = calculateTreeHeight(pFreeTreeHeight, pRandom);

        // Extend trunk 3-4 blocks into canopy
        int trunkExtension = 3 + pRandom.nextInt(2); // 3-4 blocks
        int totalTrunkHeight = baseTrunkHeight + trunkExtension;

        // Pre-determine features to avoid repeated random calls
        boolean hasButtress = pRandom.nextFloat() < BUTTRESS_CHANCE;

        // Single trunk building loop with integrated features
        for (int i = 0; i < totalTrunkHeight; i++) {
            BlockPos currentPos = pPos.above(i);

            // Place main trunk
            placeLog(pLevel, pBlockSetter, pRandom, currentPos, pConfig);

            // Apply trunk features based on height (only for base trunk, not extension)
            if (i < baseTrunkHeight) {
                applyTrunkFeatures(pLevel, pBlockSetter, pRandom, currentPos, pConfig, i, hasButtress);
            }
        }

        // Return foliage attachment at the base trunk height (where canopy starts)
        // The extended trunk will be inside the canopy
        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pPos.above(baseTrunkHeight), 0, false));
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
                                    int currentHeight, boolean hasButtress) {

        // Buttressed base
        if (hasButtress && currentHeight <= BUTTRESS_HEIGHT) {
            addButtressFeatures(pLevel, pBlockSetter, pRandom, pPos, pConfig, currentHeight);
        }

        // Trunk thickness (skip height 0 to avoid ground level thickness)
        if (currentHeight > 0 && currentHeight <= TRUNK_THICKNESS_HEIGHT) {
            addTrunkThickness(pLevel, pBlockSetter, pRandom, pPos, pConfig, currentHeight);
        }

        // Minimal branching
        if (currentHeight >= MIN_BRANCH_HEIGHT && currentHeight % 3 == 0 &&
                pRandom.nextFloat() < BRANCH_CHANCE) {
            addMinimalBranches(pLevel, pBlockSetter, pRandom, pPos, pConfig);
        }
    }

    /**
     * Optimized buttress creation with reduced calculations
     */
    private void addButtressFeatures(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter,
                                     RandomSource pRandom, BlockPos pPos, TreeConfiguration pConfig, int currentHeight) {

        // Pre-calculate buttress strength once
        float buttressStrength = 1.0f - ((float) currentHeight / BUTTRESS_HEIGHT);

        if (buttressStrength > 0.3f && pRandom.nextFloat() < buttressStrength) {
            float placementChance = buttressStrength * 0.8f;

            // Efficient direction iteration
            for (Direction direction : HORIZONTAL_DIRECTIONS) {
                if (pRandom.nextFloat() < placementChance) {
                    BlockPos buttressPos = pPos.relative(direction);
                    if (pLevel.isStateAtPosition(buttressPos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
                        placeLog(pLevel, pBlockSetter, pRandom, buttressPos, pConfig);
                    }
                }
            }
        }
    }

    /**
     * Optimized trunk thickness with smarter direction selection
     */
    private void addTrunkThickness(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter,
                                   RandomSource pRandom, BlockPos pPos, TreeConfiguration pConfig, int height) {

        float thicknessChance = 0.8f - (height * 0.05f);

        if (pRandom.nextFloat() < thicknessChance) {
            // Select random direction once
            Direction chosenDir = HORIZONTAL_DIRECTIONS[pRandom.nextInt(4)];

            // Try to place primary thickness
            if (tryPlaceLogAt(pLevel, pBlockSetter, pRandom, pPos.relative(chosenDir), pConfig)) {
                // 40% chance for opposite direction thickness
                if (pRandom.nextFloat() < 0.4f) {
                    tryPlaceLogAt(pLevel, pBlockSetter, pRandom, pPos.relative(chosenDir.getOpposite()), pConfig);
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

    /**
     * Helper method that combines replaceability check and placement
     */
    private boolean tryPlaceLogAt(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter,
                                  RandomSource pRandom, BlockPos pos, TreeConfiguration pConfig) {
        if (pLevel.isStateAtPosition(pos, BlockBehaviour.BlockStateBase::canBeReplaced)) {
            placeLog(pLevel, pBlockSetter, pRandom, pos, pConfig);
            return true;
        }
        return false;
    }
}