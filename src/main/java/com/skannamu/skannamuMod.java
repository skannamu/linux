package com.skannamu;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.skannamu.init.BlockInitialization;
import com.skannamu.item.tool.PortableTerminalItem;
import com.skannamu.network.TerminalCommandPayload;
import com.skannamu.server.DataLoader;
import com.skannamu.server.MissionData;
import com.skannamu.server.ServerCommandProcessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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

import java.util.HashMap;
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

        ServerLifecycleEvents.SERVER_STARTED.register(this::initializeTerminalSystem);

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

    private void initializeTerminalSystem(MinecraftServer server) {
        JsonElement jsonElement = DataLoader.INSTANCE.getMissionData();
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            LOGGER.error("Failed to load or parse mission_data.json. Using default configuration.");
            return;
        }

        Gson gson = new Gson();
        MissionData missionData = gson.fromJson(jsonElement, MissionData.class);

        // 1. 파일 시스템 Map 분리 및 통합
        Map<String, String> allFilesystem = new HashMap<>(); // 모든 경로 (파일 내용 + 디렉토리 ls 내용)
        Map<String, String> directoriesOnly = new HashMap<>(); // 디렉토리 경로만 (cd/ls 확인용)

        if (missionData.filesystem != null) {
            if (missionData.filesystem.directories != null) {
                // 디렉토리 경로: directoriesOnly 맵과 allFilesystem 맵 모두에 추가
                directoriesOnly.putAll(missionData.filesystem.directories);
                allFilesystem.putAll(missionData.filesystem.directories);
            }
            if (missionData.filesystem.files != null) {
                // 파일 경로: allFilesystem 맵에만 추가 (cat 가능)
                allFilesystem.putAll(missionData.filesystem.files);
            }
        }

        // 2. CommandProcessor에 설정합니다. (두 개의 맵 전달)
        ServerCommandProcessor.setFilesystem(allFilesystem, directoriesOnly);

        // 3. 활성화 키를 설정합니다.
        if (missionData.terminal_settings != null) {
            ServerCommandProcessor.setActivationKey(missionData.terminal_settings.activation_key);
        } else {
            ServerCommandProcessor.setActivationKey(null);
        }

        LOGGER.info("Terminal FAKE_FILESYSTEM and ACTIVATION_KEY initialized from JSON.");
    }
}