package com.skannamu.init;

import com.skannamu.item.block.VaultBlock;
import com.skannamu.item.block.standardBlock;
import com.skannamu.skannamuMod;
import com.skannamu.tooltip.standardBlockToolTip;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem; // 💡 BlockItem 임포트 추가
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockInitialization {

    public static Block STANDARD_BLOCK;
    public static Block VAULT_BLOCK; // 💡 VaultBlock 필드 추가

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

        Registry.register(
                Registries.ITEM,
                itemKey,
                new standardBlockToolTip(registeredBlock, itemSettings)
        );
        return registeredBlock;
    }

    // 💡 VaultBlock 등록 메서드 추가
    private static Block registerVaultBlock(String name) {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, name);
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);

        Block.Settings blockSettings = Block.Settings.create()
                .registryKey(blockKey)
                .strength(3.0f, 6.0f)
                .nonOpaque();

        // 💡 VaultBlock 등록 (BlockWithEntity를 상속받은 VaultBlock 사용)
        Block registeredBlock = Registry.register(Registries.BLOCK, blockKey, new VaultBlock(blockSettings));

        // BlockItem 등록
        Item.Settings itemSettings = new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id));
        Registry.register(
                Registries.ITEM,
                RegistryKey.of(RegistryKeys.ITEM, id),
                new BlockItem(registeredBlock, itemSettings) // BlockItem 사용
        );

        return registeredBlock;
    }

    public static void initializeBlocks() {
        STANDARD_BLOCK = registerBlockWithItem("standard_block");

        // 💡 VaultBlock 등록 추가
        VAULT_BLOCK = registerVaultBlock("vault_block");

        skannamuMod.LOGGER.info("Registered Standard and Vault Blocks successfully!");
    }
}