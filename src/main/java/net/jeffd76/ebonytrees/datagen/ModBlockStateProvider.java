package net.jeffd76.ebonytrees.datagen;

import net.jeffd76.ebonytrees.EbonyTrees;
import net.jeffd76.ebonytrees.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, EbonyTrees.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

        //blockWithItem(ModBlocks.SAPPHIRE_BLOCK);
        
        logBlock(((RotatedPillarBlock) ModBlocks.EBONY_LOG.get()));
        axisBlock(((RotatedPillarBlock) ModBlocks.EBONY_WOOD.get()), blockTexture(ModBlocks.EBONY_LOG.get()), blockTexture(ModBlocks.EBONY_LOG.get()));
        axisBlock(((RotatedPillarBlock) ModBlocks.STRIPPED_EBONY_LOG.get()), blockTexture(ModBlocks.STRIPPED_EBONY_LOG.get()),
                new ResourceLocation(EbonyTrees.MOD_ID, "block/stripped_ebony_log_top"));
        axisBlock(((RotatedPillarBlock) ModBlocks.STRIPPED_EBONY_WOOD.get()), blockTexture(ModBlocks.STRIPPED_EBONY_LOG.get()),
                blockTexture(ModBlocks.STRIPPED_EBONY_LOG.get()));

        blockItem(ModBlocks.EBONY_LOG);
        blockItem(ModBlocks.EBONY_WOOD);
        blockItem(ModBlocks.STRIPPED_EBONY_LOG);
        blockItem(ModBlocks.STRIPPED_EBONY_WOOD);

        blockWithItem(ModBlocks.EBONY_PLANKS);

        leavesBlock(ModBlocks.EBONY_LEAVES);

        stairsBlock(((StairBlock) ModBlocks.EBONY_STAIRS.get()), blockTexture(ModBlocks.EBONY_PLANKS.get()));
        slabBlock(((SlabBlock) ModBlocks.EBONY_SLAB.get()), blockTexture(ModBlocks.EBONY_PLANKS.get()), blockTexture(ModBlocks.EBONY_PLANKS.get()));

        buttonBlock(((ButtonBlock) ModBlocks.EBONY_BUTTON.get()), blockTexture(ModBlocks.EBONY_PLANKS.get()));
        pressurePlateBlock(((PressurePlateBlock) ModBlocks.EBONY_PRESSURE_PLATE.get()), blockTexture(ModBlocks.EBONY_PLANKS.get()));

        fenceBlock(((FenceBlock) ModBlocks.EBONY_FENCE.get()), blockTexture(ModBlocks.EBONY_PLANKS.get()));
        fenceGateBlock(((FenceGateBlock) ModBlocks.EBONY_FENCE_GATE.get()), blockTexture(ModBlocks.EBONY_PLANKS.get()));

        doorBlockWithRenderType(((DoorBlock) ModBlocks.EBONY_DOOR.get()), modLoc("block/ebony_door_bottom"), modLoc("block/ebony_door_top"),
                "cutout");
        trapdoorBlockWithRenderType(((TrapDoorBlock) ModBlocks.EBONY_TRAPDOOR.get()), modLoc("block/ebony_trapdoor"), true,
                "cutout");

        signBlock(((StandingSignBlock) ModBlocks.EBONY_SIGN.get()), ((WallSignBlock) ModBlocks.EBONY_WALL_SIGN.get()),
                blockTexture(ModBlocks.EBONY_PLANKS.get()));

        hangingSignBlock(ModBlocks.EBONY_HANGING_SIGN.get(), ModBlocks.EBONY_WALL_HANGING_SIGN.get(), blockTexture(ModBlocks.EBONY_PLANKS.get()));



    }

    public void hangingSignBlock(Block signBlock, Block wallSignBlock, ResourceLocation texture) {
        ModelFile sign = models().sign(name(signBlock), texture);
        hangingSignBlock(signBlock, wallSignBlock, sign);
    }

    public void hangingSignBlock(Block signBlock, Block wallSignBlock, ModelFile sign) {
        simpleBlock(signBlock, sign);
        simpleBlock(wallSignBlock, sign);
    }

    private String name(Block block) {
        return key(block).getPath();
    }

    private ResourceLocation key(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void blockItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockItem(blockRegistryObject.get(), new ModelFile.UncheckedModelFile(EbonyTrees.MOD_ID +
                ":block/" + ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath()));
    }

    private void leavesBlock(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(),
                models().singleTexture(ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath(), new ResourceLocation("minecraft:block/leaves"),
                        "all", blockTexture(blockRegistryObject.get())).renderType("cutout"));
    }
}
