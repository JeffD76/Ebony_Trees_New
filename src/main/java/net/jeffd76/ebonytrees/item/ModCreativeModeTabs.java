package net.jeffd76.ebonytrees.item;

import net.jeffd76.ebonytrees.EbonyTrees;
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
                    })
                    .build());

    public static void  register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
