package com.bios;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class MochaBlockTags {
    public static final TagKey<Block> CASCADABLE = TagKey.of(RegistryKeys.BLOCK, Mocha.id("cascadable"));
}
