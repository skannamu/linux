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
        // 1. 초기 파일 시스템 데이터 정의
        Map<String, String> initialFilesystem = Map.of(
                "/", "help.txt\nprograms/\nsecrets.dat", // ls / 명령 결과
                "help.txt", "사용법: ls, cat, decrypt, calc, key [code]를 입력하세요. \n터미널 접근 권한을 얻으려면 'key' 명령을 사용하세요.",
                "programs/", "run.exe",
                "secrets.dat", "ZmxhZ190ZXh0X1NPTUVUSElORw=="
        );

        ServerCommandProcessor.setFilesystem(initialFilesystem);

        ServerCommandProcessor.setActivationKey("12345"); // 예시 활성화 키

        LOGGER.info("Terminal FAKE_FILESYSTEM and ACTIVATION_KEY initialized.");
    }
}