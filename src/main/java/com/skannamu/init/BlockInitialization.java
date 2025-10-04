// BlockInitialization.java (최종 수정안)
package com.skannamu.init;

import com.skannamu.item.block.standardBlock;
import com.skannamu.skannamuMod;
import net.minecraft.block.Block;
import net.minecraft.item.Item; // Item Import는 유지
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockInitialization {

    public static Block STANDARD_BLOCK; // 블록 객체만 저장
    public static void initializeBlocks() {
        // Identifier를 여기서 생성합니다.
        Identifier id = Identifier.of(skannamuMod.MOD_ID, "standard_block");

        // Block Settings를 정의합니다.
        Block.Settings blockSettings = Block.Settings.create()
                .strength(1.5f)
                .requiresTool();

        // 1. STANDARD_BLOCK을 initializeBlocks 내부에서 바로 등록합니다.
        STANDARD_BLOCK = Registry.register(
                Registries.BLOCK,
                id,
                new standardBlock(blockSettings)
        );

        // 아이템 등록 코드는 skannamuMod.java로 옮겼으므로 여기에 없습니다.

        skannamuMod.LOGGER.info("Registered Standard Block successfully!");
    }
}