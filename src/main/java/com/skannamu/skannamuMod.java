package com.skannamu;

import com.skannamu.init.BlockInitialization;
import com.skannamu.item.tool.PortableTerminalItem;
import com.skannamu.network.TerminalCommandPayload;
import com.skannamu.server.DataLoader;
import com.skannamu.server.ServerCommandProcessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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

        BlockInitialization.initializeBlocks();

        // PORTABLE_TERMINAL 등록 수정: registryKey 추가
        Identifier portableTerminalId = Identifier.of(MOD_ID, "portable_terminal");
        RegistryKey<Item> portableTerminalKey = RegistryKey.of(RegistryKeys.ITEM, portableTerminalId);
        PORTABLE_TERMINAL = Registry.register(
                Registries.ITEM,
                portableTerminalKey,
                new PortableTerminalItem(new Item.Settings().maxCount(1).registryKey(portableTerminalKey))
        );

        STANDARD_BLOCK_ITEM = Registries.ITEM.get(Identifier.of(MOD_ID, "standard_block"));

        DataLoader.registerDataLoaders();

        // Payload 타입 등록 추가: C2S (클라이언트 → 서버) 방향
        PayloadTypeRegistry.playC2S().register(TerminalCommandPayload.ID, TerminalCommandPayload.CODEC);

        // 네트워크 리시버 등록
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