package net.jeffd76.ebonytrees.item;

import net.jeffd76.ebonytrees.EbonyTrees;
import net.jeffd76.ebonytrees.block.ModBlocks;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, EbonyTrees.MOD_ID);

    public static final RegistryObject<Item> EBONY_SAPLING = ITEMS.register("ebony_sapling",
            ()-> new Item(new Item.Properties()));

    public static final RegistryObject<Item> EBONY_SIGN = ITEMS.register("ebony_sign",
            ()-> new SignItem(new Item.Properties().stacksTo(16), ModBlocks.EBONY_SIGN.get(), ModBlocks.EBONY_WALL_SIGN.get()));
    public static final RegistryObject<Item> EBONY_HANGING_SIGN = ITEMS.register("ebony_hanging_sign",
            ()-> new HangingSignItem(ModBlocks.EBONY_HANGING_SIGN.get(), ModBlocks.EBONY_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));


    public static void  register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
