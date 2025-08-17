package net.jeffd76.ebonytrees.item;

import net.jeffd76.ebonytrees.EbonyTrees;
import net.jeffd76.ebonytrees.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EbonyTrees.MOD_ID);

    public static final RegistryObject<CreativeModeTab> EBONY_TREES_TAB = CREATIVE_MODE_TABS.register("ebony_trees_tab",
            ()-> CreativeModeTab.builder().icon(()-> new ItemStack(ModBlocks.EBONY_SAPLING.get()))
                    .title(Component.translatable("creativetab.ebony_trees_tab"))
                    .displayItems((itemDisplayParameters, pOutput) -> {

                        pOutput.accept(ModBlocks.EBONY_SAPLING.get());

                        pOutput.accept(ModItems.EBONY_SIGN.get());
                        pOutput.accept(ModItems.EBONY_HANGING_SIGN.get());

                        pOutput.accept(ModItems.EBONY_BOAT.get());
                        pOutput.accept(ModItems.EBONY_CHEST_BOAT.get());

                        pOutput.accept(ModBlocks.EBONY_STAIRS.get());
                        pOutput.accept(ModBlocks.EBONY_SLAB.get());
                        pOutput.accept(ModBlocks.EBONY_BUTTON.get());
                        pOutput.accept(ModBlocks.EBONY_PRESSURE_PLATE.get());

                        pOutput.accept(ModBlocks.EBONY_FENCE.get());
                        pOutput.accept(ModBlocks.EBONY_FENCE_GATE.get());

                        pOutput.accept(ModBlocks.EBONY_DOOR.get());
                        pOutput.accept(ModBlocks.EBONY_TRAPDOOR.get());

                        pOutput.accept(ModBlocks.EBONY_LOG.get());
                        pOutput.accept(ModBlocks.EBONY_WOOD.get());
                        pOutput.accept(ModBlocks.STRIPPED_EBONY_LOG.get());
                        pOutput.accept(ModBlocks.STRIPPED_EBONY_WOOD.get());

                        pOutput.accept(ModBlocks.EBONY_PLANKS.get());
                        pOutput.accept(ModBlocks.EBONY_LEAVES.get());

                    })
                    .build());

    public static void  register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
