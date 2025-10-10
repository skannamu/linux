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
import java.util.Map;
// import java.util.Base64;

public class skannamuMod implements ModInitializer {
    public static final String MOD_ID = "skannamu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Item PORTABLE_TERMINAL;
    public static Item STANDARD_BLOCK_ITEM;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing skannamuMod...");

        BlockInitialization.initializeBlocks();
        Identifier portableTerminalId = Identifier.of(MOD_ID, "portable_terminal");
        RegistryKey<Item> portableTerminalKey = RegistryKey.of(RegistryKeys.ITEM, portableTerminalId);
        PORTABLE_TERMINAL = Registry.register(
                Registries.ITEM,
                portableTerminalKey,
                new PortableTerminalItem(new Item.Settings().maxCount(1).registryKey(portableTerminalKey))
        );

        STANDARD_BLOCK_ITEM = Registries.ITEM.get(Identifier.of(MOD_ID, "standard_block"));

        // ⚡️ 변경: DataLoader 등록을 먼저 해야 initializeTerminalSystem에서 데이터를 사용할 수 있음
        DataLoader.registerDataLoaders();
        initializeTerminalSystem();

        PayloadTypeRegistry.playC2S().register(TerminalCommandPayload.ID, TerminalCommandPayload.CODEC);
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

    private void initializeTerminalSystem() {
        // ⚡️ 변경: DataLoader에서 JSON 파일의 데이터를 로드하여 사용

        // 1. 통합된 파일 시스템 데이터 로드 (디렉토리 목록 + 파일 내용)
        Map<String, String> initialFilesystem = DataLoader.getFilesystemData();

        // 2. 활성화 키 로드
        String actualKey = DataLoader.getActivationKey();

        ServerCommandProcessor.setFilesystem(initialFilesystem);
        ServerCommandProcessor.setActivationKey(actualKey);

        LOGGER.info("Terminal FAKE_FILESYSTEM and ACTIVATION_KEY initialized from external data.");
    }
}