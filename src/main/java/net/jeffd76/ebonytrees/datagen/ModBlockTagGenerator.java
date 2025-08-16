package net.jeffd76.ebonytrees.datagen;

import net.jeffd76.ebonytrees.EbonyTrees;
import net.jeffd76.ebonytrees.block.ModBlocks;
import net.jeffd76.ebonytrees.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagGenerator extends BlockTagsProvider {
    public ModBlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, EbonyTrees.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {


        this.tag(ModTags.Blocks.EBONY_LOGS)
                .add(ModBlocks.EBONY_LOG.get())
                .add(ModBlocks.EBONY_WOOD.get())
                .add(ModBlocks.STRIPPED_EBONY_LOG.get())
                .add(ModBlocks.STRIPPED_EBONY_WOOD.get());

        this.tag(BlockTags.LOGS_THAT_BURN)
               .add(ModBlocks.EBONY_LOG.get())
               .add(ModBlocks.EBONY_WOOD.get())
               .add(ModBlocks.STRIPPED_EBONY_LOG.get())
               .add(ModBlocks.STRIPPED_EBONY_WOOD.get());

       this.tag(BlockTags.PLANKS)
               .add(ModBlocks.EBONY_PLANKS.get());

        this.tag(BlockTags.FENCES)
                .add(ModBlocks.EBONY_FENCE.get());
        this.tag(BlockTags.FENCE_GATES)
                .add(ModBlocks.EBONY_FENCE_GATE.get());

    }
}
