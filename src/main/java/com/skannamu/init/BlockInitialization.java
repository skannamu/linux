package com.skannamu.init;

import com.skannamu.item.block.standardBlock;
import com.skannamu.skannamuMod;
import com.skannamu.tooltip.standardBlockToolTip;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockInitialization {

    public static Block STANDARD_BLOCK;

    private static Block registerBlockWithItem(String name) {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);

        Block.Settings blockSettings = Block.Settings.create()
                .registryKey(blockKey)
                .strength(1.5f)
                .requiresTool();

        Block registeredBlock = Registry.register(Registries.BLOCK, blockKey, new standardBlock(blockSettings));

        Item.Settings itemSettings = new Item.Settings()
                .registryKey(itemKey);

        // standardBlockTooltip 사용
        Registry.register(
                Registries.ITEM,
                itemKey,
                new standardBlockToolTip(registeredBlock, itemSettings)
        );

        return registeredBlock;
    }

    public static void initializeBlocks() {
        STANDARD_BLOCK = registerBlockWithItem("standard_block");

        skannamuMod.LOGGER.info("Registered Standard Block successfully!");
    }
}