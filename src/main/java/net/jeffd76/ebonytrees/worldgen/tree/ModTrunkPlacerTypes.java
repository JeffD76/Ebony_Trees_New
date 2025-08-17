package net.jeffd76.ebonytrees.worldgen.tree;

import net.jeffd76.ebonytrees.EbonyTrees;
import net.jeffd76.ebonytrees.worldgen.tree.custom.EbonyTrunkPlacer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModTrunkPlacerTypes {
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER =
            DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, EbonyTrees.MOD_ID);

    public static final RegistryObject<TrunkPlacerType<EbonyTrunkPlacer>> EBONY_TRUNK_PLACER =
            TRUNK_PLACER.register("ebony_trunk_placer", ()-> new TrunkPlacerType<>(EbonyTrunkPlacer.CODEC));

    public static void register(IEventBus eventBus) {
        TRUNK_PLACER.register(eventBus);
    }
}
