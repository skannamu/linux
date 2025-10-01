package com.skannamu.init;

import com.skannamu.item.block.standardBlock;
import com.skannamu.skannamuMod;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BlockInitialization {

    public static final Block STANDARD_BLOCK = registerBlockWithItem(
            "standard_block",
            new standardBlock()
    );

    private static Block registerBlockWithItem(String name, Block block) {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        Block registeredBlock = Registry.register(Registries.BLOCK, id, block);
        Registry.register(
                Registries.ITEM,
                id,
                new BlockItem(registeredBlock, new Item.Settings())
        );

        return registeredBlock;
    }

    public static void initializeBlocks() {
        Block temp = STANDARD_BLOCK;
        skannamuMod.LOGGER.info("Registered Standard Block successfully!");
    }
}
