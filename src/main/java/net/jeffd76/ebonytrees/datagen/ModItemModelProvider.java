package net.jeffd76.ebonytrees.datagen;

import net.jeffd76.ebonytrees.EbonyTrees;
import net.jeffd76.ebonytrees.block.ModBlocks;
import net.jeffd76.ebonytrees.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, EbonyTrees.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.EBONY_SAPLING);

        simpleBlockItem(ModBlocks.EBONY_DOOR);

        fenceItem(ModBlocks.EBONY_FENCE, ModBlocks.EBONY_PLANKS);
        buttonItem(ModBlocks.EBONY_BUTTON, ModBlocks.EBONY_PLANKS);

        evenSimplerBlockItem(ModBlocks.EBONY_STAIRS);
        evenSimplerBlockItem(ModBlocks.EBONY_SLAB);
        evenSimplerBlockItem(ModBlocks.EBONY_PRESSURE_PLATE);
        evenSimplerBlockItem(ModBlocks.EBONY_FENCE_GATE);

        trapdoorItem(ModBlocks.EBONY_TRAPDOOR);

        simpleItem(ModItems.EBONY_SIGN);
        simpleItem(ModItems.EBONY_HANGING_SIGN);

        simpleItem(ModItems.EBONY_BOAT);
        simpleItem(ModItems.EBONY_CHEST_BOAT);

    }

    public void evenSimplerBlockItem(RegistryObject<Block> block) {
        this.withExistingParent(EbonyTrees.MOD_ID + ":" + ForgeRegistries.BLOCKS.getKey(block.get()).getPath(),
                modLoc("block/" + ForgeRegistries.BLOCKS.getKey(block.get()).getPath()));
    }

    public void trapdoorItem(RegistryObject<Block> block) {
        this.withExistingParent(ForgeRegistries.BLOCKS.getKey(block.get()).getPath(),
                modLoc("block/" + ForgeRegistries.BLOCKS.getKey(block.get()).getPath() + "_bottom"));
    }

    public void fenceItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        this.withExistingParent(ForgeRegistries.BLOCKS.getKey(block.get()).getPath(), mcLoc("block/fence_inventory"))
                .texture("texture",  new ResourceLocation(EbonyTrees.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(baseBlock.get()).getPath()));
    }

    public void buttonItem(RegistryObject<Block> block, RegistryObject<Block> baseBlock) {
        this.withExistingParent(ForgeRegistries.BLOCKS.getKey(block.get()).getPath(), mcLoc("block/button_inventory"))
                .texture("texture",  new ResourceLocation(EbonyTrees.MOD_ID, "block/" + ForgeRegistries.BLOCKS.getKey(baseBlock.get()).getPath()));
    }

    private ItemModelBuilder simpleBlockItem(RegistryObject<Block> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(EbonyTrees.MOD_ID,"item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(EbonyTrees.MOD_ID, "item/" + item.getId().getPath()));
    }
}
