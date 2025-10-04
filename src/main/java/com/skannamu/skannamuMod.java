package com.skannamu;


import com.skannamu.init.BlockInitialization;
import com.skannamu.item.tool.PortableTerminalItem;
import com.skannamu.tooltip.standardBlockToolTip; // 👈 새로 추가된 임포트
import com.skannamu.network.TerminalCommandPayload;
import com.skannamu.server.DataLoader;
import com.skannamu.server.ServerCommandProcessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class skannamuMod implements ModInitializer {
    public static final String MOD_ID = "skannamu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // ✨ 모든 아이템 필드를 static으로 선언만 합니다.
    public static Item PORTABLE_TERMINAL;
    public static Item STANDARD_BLOCK_ITEM; // 👈 블록 아이템 필드 추가

    @Override
    public void onInitialize() {
        // 1. 블록만 먼저 초기화하여 BlockInitialization.STANDARD_BLOCK 필드에 블록 객체를 할당합니다.
        // BlockItem에 필요한 Block 객체를 먼저 만듭니다.
        BlockInitialization.initializeBlocks();

        // 2. 모든 아이템을 안전하게 레지스트리에 등록합니다.

        // Portable Terminal Item 등록
        PORTABLE_TERMINAL = Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "portable_terminal"),
                new PortableTerminalItem(new Item.Settings().maxCount(1))
        );

        // standard_block의 BlockItem (standardBlockToolTip) 등록
        STANDARD_BLOCK_ITEM = Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "standard_block"),
                new standardBlockToolTip(BlockInitialization.STANDARD_BLOCK, new Item.Settings())
        );

        // 3. 나머지 초기화 로직 (순서 유지)
        DataLoader.registerDataLoaders();
        ServerPlayNetworking.registerGlobalReceiver(TerminalCommandPayload.ID,
                (TerminalCommandPayload payload, ServerPlayNetworking.Context context) -> {
                    MinecraftServer server = context.player().getServer();
                    final ServerPlayerEntity player = context.player();
                    String command = payload.command();
                    server.execute(() -> {
                        ServerCommandProcessor.processCommand(player, command);
                    });
                });
        LOGGER.info("skannamuMod initialized!");
    }
}