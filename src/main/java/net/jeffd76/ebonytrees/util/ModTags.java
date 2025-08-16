package net.jeffd76.ebonytrees.util;

import net.jeffd76.ebonytrees.EbonyTrees;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> EBONY_LOGS = createTag("ebony_logs");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(EbonyTrees.MOD_ID, name));
        }
    }

    public static class Items {
        public static final TagKey<Item> EBONY_LOGS = createTag("ebony_logs");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(EbonyTrees.MOD_ID, name));
        }
    }
}