package com.skannamu;

import com.skannamu.init.BlockInitialization;
import com.skannamu.item.tool.PortableTerminalItem;
import com.skannamu.tooltip.standardBlockToolTip;
import com.skannamu.network.TerminalCommandPayload;
import com.skannamu.server.DataLoader;
import com.skannamu.server.ServerCommandProcessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;  // 추가: Items.register 사용
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class skannamuMod implements ModInitializer {
    public static final String MOD_ID = "skannamu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Item PORTABLE_TERMINAL;
    public static Item STANDARD_BLOCK_ITEM;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing skannamuMod...");

        // 블록 초기화 (ID 설정 포함)
        BlockInitialization.initializeBlocks();

        // 아이템 등록
        PORTABLE_TERMINAL = Registry.register(
                Registries.ITEM,
                Identifier.of(MOD_ID, "portable_terminal"),
                new PortableTerminalItem(new Item.Settings().maxCount(1))
        );

        // 블록 아이템 자동 등록 (Blocks.register와 연계)
        STANDARD_BLOCK_ITEM = Items.register(BlockInitialization.STANDARD_BLOCK);  // Items.register로 블록 아이템 생성

        // 나머지 로직
        DataLoader.registerDataLoaders();
        ServerPlayNetworking.registerGlobalReceiver(TerminalCommandPayload.ID,
                (payload, context) -> {
                    MinecraftServer server = context.server();
                    ServerPlayerEntity player = context.player();
                    String command = payload.command();
                    server.execute(() -> {
                        ServerCommandProcessor.processCommand(player, command);
                    });
                });

        LOGGER.info("skannamuMod initialized successfully!");
    }
}