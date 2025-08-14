package net.jeffd76.ebonytrees.util;

import net.jeffd76.ebonytrees.EbonyTrees;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public class ModWoodTypes {
    public static final WoodType EBONY = WoodType.register(new WoodType(EbonyTrees.MOD_ID + ":ebony", BlockSetType.OAK));
}
