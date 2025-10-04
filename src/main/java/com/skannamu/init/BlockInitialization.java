package com.skannamu.init;

import com.skannamu.item.block.standardBlock;
import com.skannamu.skannamuMod;
import com.skannamu.tooltip.standardBlockToolTip;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BlockInitialization {

    public static Block STANDARD_BLOCK;

    private static Block registerBlockWithItem(String name) {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);

        Block.Settings blockSettings = Block.Settings.create()
                .strength(1.5f)
                .requiresTool();

        Block registeredBlock = Registry.register(
                Registries.BLOCK,
                id,
                new standardBlock(blockSettings)
        );

        Item.Settings itemSettings = new Item.Settings();

        Registry.register(
                Registries.ITEM,
                id,
                new standardBlockToolTip(registeredBlock, itemSettings)
        );

        skannamuMod.LOGGER.info("Registered block and item: " + id.toString());
        return registeredBlock;
    }

    public static void initializeBlocks() {
        STANDARD_BLOCK = registerBlockWithItem("standard_block");
        skannamuMod.LOGGER.info("Registered Standard Block successfully!");
    }
}