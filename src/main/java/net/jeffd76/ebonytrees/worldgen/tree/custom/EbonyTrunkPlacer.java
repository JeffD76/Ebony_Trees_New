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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class EbonyTrunkPlacer extends TrunkPlacer {

    public static final Codec<EbonyTrunkPlacer> CODEC = RecordCodecBuilder.create(ebonyTrunkPlacerInstance ->
            trunkPlacerParts(ebonyTrunkPlacerInstance).apply(ebonyTrunkPlacerInstance, EbonyTrunkPlacer::new));

    // Realistic ebony tree variables based on Diospyros ebenum characteristics
    private static final int MIN_TREE_HEIGHT = 10; // Minimum realistic height
    private static final int MAX_TREE_HEIGHT = 18; // Maximum realistic height
    private static final int TRUNK_THICKNESS_HEIGHT = 6; // Height where trunk stays thick
    private static final float BUTTRESS_CHANCE = 0.7f; // 70% chance for buttressed base
    private static final int BUTTRESS_HEIGHT = 4; // Height of buttressed section
    private static final float BRANCH_CHANCE = 0.15f; // Low branching chance (ebony has minimal lower branching)
    private static final int MIN_BRANCH_HEIGHT = 12; // Branches only start higher up

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

        // Calculate realistic ebony tree height (15-28 blocks)
        int baseTreeHeight = Math.max(MIN_TREE_HEIGHT, Math.min(MAX_TREE_HEIGHT,
                pFreeTreeHeight + pRandom.nextInt(heightRandA) + pRandom.nextInt(heightRandB)));

        // Add slight height variation
        int finalHeight = baseTreeHeight + pRandom.nextInt(-2, 3);
        finalHeight = Math.max(MIN_TREE_HEIGHT, Math.min(MAX_TREE_HEIGHT, finalHeight));

        // Create buttressed base (characteristic of ebony trees)
        boolean hasButtress = pRandom.nextFloat() < BUTTRESS_CHANCE;

        for(int i = 0; i < finalHeight; i++) {
            // Place main trunk
            placeLog(pLevel, pBlockSetter, pRandom, pPos.above(i), pConfig);

            // Create buttressed base for realism
            if (hasButtress && i <= BUTTRESS_HEIGHT) {
                createButtressedBase(pLevel, pBlockSetter, pRandom, pPos.above(i), pConfig, i, BUTTRESS_HEIGHT);
            }

            // Add thick trunk section (ebony trees have notably wide trunks)
            if (i <= TRUNK_THICKNESS_HEIGHT && i > 0) {
                addTrunkThickness(pLevel, pBlockSetter, pRandom, pPos.above(i), pConfig, i);
            }

            // Minimal branching in lower sections (realistic for ebony)
            if (i >= MIN_BRANCH_HEIGHT && i % 3 == 0 && pRandom.nextFloat() < BRANCH_CHANCE) {
                addMinimalBranches(pLevel, pBlockSetter, pRandom, pPos.above(i), pConfig);
            }
        }

        // Return foliage attachment point at the top
        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pPos.above(finalHeight), 0, false));
    }

    /**
     * Creates a buttressed base characteristic of mature ebony trees
     */
    private void createButtressedBase(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter,
                                      RandomSource pRandom, BlockPos pPos, TreeConfiguration pConfig,
                                      int currentHeight, int buttressMaxHeight) {

        // Buttress intensity decreases with height
        float buttressStrength = 1.0f - ((float) currentHeight / buttressMaxHeight);

        if (buttressStrength > 0.3f && pRandom.nextFloat() < buttressStrength) {
            // Add buttress extensions in cardinal directions
            Direction[] horizontalDirections = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
            for (Direction direction : horizontalDirections) {
                if (pRandom.nextFloat() < buttressStrength * 0.8f) {
                    BlockPos buttressPos = pPos.relative(direction);
                    if (isReplaceable(pLevel, buttressPos)) {
                        placeLog(pLevel, pBlockSetter, pRandom, buttressPos, pConfig);
                    }
                }
            }
        }
    }

    /**
     * Adds trunk thickness characteristic of ebony trees
     */
    private void addTrunkThickness(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter,
                                   RandomSource pRandom, BlockPos pPos, TreeConfiguration pConfig, int height) {

        // Trunk gets slightly thinner as it goes up
        float thicknessChance = 0.9f - (height * 0.05f);

        if (pRandom.nextFloat() < thicknessChance) {
            // Randomly add thickness in one or two directions to avoid perfect squares
            Direction[] horizontalDirections = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
            Direction chosenDir = horizontalDirections[pRandom.nextInt(horizontalDirections.length)];

            BlockPos thickPos = pPos.relative(chosenDir);
            if (isReplaceable(pLevel, thickPos)) {
                placeLog(pLevel, pBlockSetter, pRandom, thickPos, pConfig);
            }

            // Sometimes add opposite direction for more natural thickness
            if (pRandom.nextFloat() < 0.4f) {
                Direction opposite = chosenDir.getOpposite();
                BlockPos oppositePos = pPos.relative(opposite);
                if (isReplaceable(pLevel, oppositePos)) {
                    placeLog(pLevel, pBlockSetter, pRandom, oppositePos, pConfig);
                }
            }
        }
    }

    /**
     * Adds minimal branching typical of ebony trees (mostly in upper sections)
     */
    private void addMinimalBranches(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter,
                                    RandomSource pRandom, BlockPos pPos, TreeConfiguration pConfig) {

        // Only add 1-2 small branches occasionally
        int branchCount = pRandom.nextInt(3); // 0, 1, or 2 branches

        for (int b = 0; b < branchCount; b++) {
            Direction[] horizontalDirections = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
            Direction direction = horizontalDirections[pRandom.nextInt(horizontalDirections.length)];
            int branchLength = pRandom.nextInt(2, 4); // Short branches (2-3 blocks)

            for (int j = 1; j <= branchLength; j++) {
                BlockPos branchPos = pPos.relative(direction, j);
                if (isReplaceable(pLevel, branchPos)) {
                    BlockState branchState = pConfig.trunkProvider.getState(pRandom, branchPos)
                            .setValue(RotatedPillarBlock.AXIS, direction.getAxis());
                    pBlockSetter.accept(branchPos, branchState);
                }
            }
        }
    }

    /**
     * Helper method to check if a position can be replaced
     */
    private boolean isReplaceable(LevelSimulatedReader pLevel, BlockPos pPos) {
        return pLevel.isStateAtPosition(pPos, state -> state.canBeReplaced());
    }
}