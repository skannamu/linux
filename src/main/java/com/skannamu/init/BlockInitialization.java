package com.skannamu.init;

import com.skannamu.item.block.standardBlock;
import com.skannamu.skannamuMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockInitialization {

    public static Block STANDARD_BLOCK;

    public static void initializeBlocks() {
        Identifier id = Identifier.of(skannamuMod.MOD_ID, "standard_block");

        // Block.Settings 설정
        AbstractBlock.Settings blockSettings = AbstractBlock.Settings.create()
                .strength(1.5f);

        // Registries.BLOCK에 register 호출 (Supplier<Block> 사용)
        STANDARD_BLOCK = Registries.register(Registries.BLOCK, id, () -> new standardBlock(blockSettings));

        skannamuMod.LOGGER.info("Registered Standard Block successfully: {}", id);
    }
}