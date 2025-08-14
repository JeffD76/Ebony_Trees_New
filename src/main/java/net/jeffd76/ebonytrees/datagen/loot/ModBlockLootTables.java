package net.jeffd76.ebonytrees.datagen.loot;

import net.jeffd76.ebonytrees.block.ModBlocks;
import net.jeffd76.ebonytrees.item.ModItems;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {

        this.dropSelf(ModBlocks.EBONY_LOG.get());
        this.dropSelf(ModBlocks.EBONY_WOOD.get());
        this.dropSelf(ModBlocks.STRIPPED_EBONY_WOOD.get());
        this.dropSelf(ModBlocks.STRIPPED_EBONY_LOG.get());
        this.dropSelf(ModBlocks.EBONY_PLANKS.get());

        this.dropSelf(ModBlocks.EBONY_STAIRS.get());
        this.dropSelf(ModBlocks.EBONY_BUTTON.get());
        this.dropSelf(ModBlocks.EBONY_PRESSURE_PLATE.get());
        this.dropSelf(ModBlocks.EBONY_TRAPDOOR.get());
        this.dropSelf(ModBlocks.EBONY_FENCE.get());
        this.dropSelf(ModBlocks.EBONY_FENCE_GATE.get());

        this.add(ModBlocks.EBONY_SLAB.get(),
                block -> createSlabItemTable(ModBlocks.EBONY_SLAB.get()));

        this.add(ModBlocks.EBONY_DOOR.get(),
                block -> createDoorTable(ModBlocks.EBONY_DOOR.get()));

        this.add(ModBlocks.EBONY_LEAVES.get(), block ->
                createLeavesDrops(block, Blocks.OAK_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES)); //TODO: change to sapling

        this.add(ModBlocks.EBONY_SIGN.get(), block ->
                createSingleItemTable(ModItems.EBONY_SIGN.get()));
        this.add(ModBlocks.EBONY_WALL_SIGN.get(), block ->
                createSingleItemTable(ModItems.EBONY_SIGN.get()));
        this.add(ModBlocks.EBONY_HANGING_SIGN.get(), block ->
                createSingleItemTable(ModItems.EBONY_HANGING_SIGN.get()));
        this.add(ModBlocks.EBONY_WALL_HANGING_SIGN.get(), block ->
                createSingleItemTable(ModItems.EBONY_HANGING_SIGN.get()));
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).toList();
    }
}
