package net.jeffd76.ebonytrees.item;

import net.jeffd76.ebonytrees.EbonyTrees;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EbonyTrees.MOD_ID);

    public static final RegistryObject<Item> EBONY_SAPLING = ITEMS.register("ebony_sapling",
            ()-> new Item(new Item.Properties()));


    public static void  register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
