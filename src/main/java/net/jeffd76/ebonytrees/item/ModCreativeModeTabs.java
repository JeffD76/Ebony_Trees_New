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
            ()-> CreativeModeTab.builder().icon(()-> new ItemStack(ModItems.EBONY_SAPLING.get()))
                    .title(Component.translatable("creativetab.ebony_trees_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.EBONY_SAPLING.get());

                        output.accept(ModBlocks.EBONY_LOG.get());
                        output.accept(ModBlocks.EBONY_WOOD.get());
                        output.accept(ModBlocks.STRIPPED_EBONY_LOG.get());
                        output.accept(ModBlocks.STRIPPED_EBONY_WOOD.get());

                        output.accept(ModBlocks.EBONY_PLANKS.get());
                        output.accept(ModBlocks.EBONY_LEAVES.get());

                        output.accept(ModBlocks.EBONY_STAIRS.get());
                        output.accept(ModBlocks.EBONY_SLAB.get());
                        output.accept(ModBlocks.EBONY_BUTTON.get());
                        output.accept(ModBlocks.EBONY_PRESSURE_PLATE.get());
                        output.accept(ModBlocks.EBONY_FENCE.get());
                        output.accept(ModBlocks.EBONY_FENCE_GATE.get());

                        output.accept(ModBlocks.EBONY_DOOR.get());
                        output.accept(ModBlocks.EBONY_TRAPDOOR.get());

                        output.accept(ModBlocks.EBONY_SIGN.get());
                        output.accept(ModBlocks.EBONY_HANGING_SIGN.get());



                    })
                    .build());

    public static void  register(IEventBus eventBus) {

        CREATIVE_MODE_TABS.register(eventBus);
    }
}
