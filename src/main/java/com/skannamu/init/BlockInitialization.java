package com.skannamu.init;

import com.skannamu.item.block.standardBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.item.ItemGroups; // 크리에이티브 기본 탭

public class BlockInitialization {
    public static final Block STANDARD_BLOCK = new standardBlock();

    public static void register() {
        // 1. 블럭 등록
        Registry.register(Registries.BLOCK, new Identifier("skannamu", "standard_block"), STANDARD_BLOCK);

        // 2. 블럭 아이템 등록
        BlockItem blockItem = new BlockItem(STANDARD_BLOCK, new Item.Settings());
        Registry.register(Registries.ITEM, new Identifier("skannamu", "standard_block"), blockItem);

        // 3. 크리에이티브 탭에 추가
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(blockItem);
        });
    }
}
