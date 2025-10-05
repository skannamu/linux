package com.skannamu.init;

import com.skannamu.item.block.standardBlock;
import com.skannamu.skannamuMod;
import net.minecraft.block.Block;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockInitialization {

    public static Block STANDARD_BLOCK;
    public static void initializeBlocks() {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, "standard_block");

        Block.Settings blockSettings = Block.Settings.create()
                .strength(1.5f);
                //lootTable(Identifier.of("minecraft", "empty"));

        STANDARD_BLOCK = Registry.register(
                Registries.BLOCK,
                id,
                new standardBlock(blockSettings)
        );

        skannamuMod.LOGGER.info("Registered Standard Block successfully!");
    }
}