package com.skannamu.init;

import com.skannamu.item.block.standardBlock;
import com.skannamu.skannamuMod;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockInitialization {

    public static Block STANDARD_BLOCK;  // 수정: final 제거, 초기화 지연

    private static Block registerBlockWithItem(String name) {  // 수정: Block 매개변수 제거, name만 받음
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);

        // Settings 생성 및 key 설정
        Block.Settings blockSettings = Block.Settings.create()
                .registryKey(blockKey)
                .strength(1.5f)
                .requiresTool();

        Block registeredBlock = Registry.register(Registries.BLOCK, blockKey, new standardBlock(blockSettings));

        Item.Settings itemSettings = new Item.Settings()
                .registryKey(itemKey)
                .useBlockPrefixedTranslationKey();

        Registry.register(
                Registries.ITEM,
                itemKey,
                new BlockItem(registeredBlock, itemSettings)
        );

        return registeredBlock;
    }

    public static void initializeBlocks() {
        STANDARD_BLOCK = registerBlockWithItem("standard_block");  // 수정: 여기서 등록 & 할당
        skannamuMod.LOGGER.info("Registered Standard Block successfully!");
    }
}