package com.skannamu;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.skannamu.init.BlockInitialization;
import com.skannamu.init.ModItems;
import com.skannamu.network.ExploitSequencePayload;
import com.skannamu.network.ExploitTriggerPayload;
import com.skannamu.network.TerminalOutputPayload;
import com.skannamu.network.TerminalCommandPayload;
import com.skannamu.server.DataLoader;
import com.skannamu.server.MissionData;
import com.skannamu.server.ServerCommandProcessor;
import com.skannamu.server.ExploitScheduler;
import com.skannamu.server.TerminalCommands;
import com.skannamu.server.command.ExploitCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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
        LOGGER.info("[skannamuMod] Initializing...");

        // 1. Payload Type Registrations (모든 패킷 타입을 여기서 등록합니다)

        // S2C (Server to Client)
        PayloadTypeRegistry.playS2C().register(ExploitSequencePayload.ID, ExploitSequencePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TerminalOutputPayload.ID, TerminalOutputPayload.CODEC);

        // C2S (Client to Server)
        PayloadTypeRegistry.playC2S().register(TerminalCommandPayload.ID, TerminalCommandPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ExploitTriggerPayload.ID, ExploitTriggerPayload.CODEC);

        // 2. C2S Network Handler Registrations

        // TerminalCommandPayload 수신 핸들러 등록
        ServerPlayNetworking.registerGlobalReceiver(TerminalCommandPayload.ID,
                (payload, context) -> {
                    MinecraftServer serverInstance = context.server();
                    if (serverInstance != null) {
                        ServerPlayerEntity player = context.player();
                        String command = payload.command();
                        // 서버 메인 스레드에서 명령어 처리 실행
                        serverInstance.execute(() -> ServerCommandProcessor.processCommand(player, command));
                    }
                });

        // ExploitTriggerPayload 수신 핸들러 등록 (ExploitScheduler 내부의 핸들러 등록 함수 호출)
        // ExploitScheduler.registerHandlers()는 이제 타입 등록 없이 핸들러만 등록합니다.
        ExploitScheduler.registerHandlers();

        // 3. Mod Initializations
        BlockInitialization.initializeBlocks();
        ModItems.initializeItems();
        TerminalCommands.initializeCommands();
        ExploitCommand.registerDamageType();


        PORTABLE_TERMINAL = ModItems.PORTABLE_TERMINAL;
        STANDARD_BLOCK_ITEM = Registries.ITEM.get(Identifier.of(MOD_ID, "standard_block"));

        DataLoader.registerDataLoaders();

        ServerLifecycleEvents.SERVER_STARTED.register(this::initializeTerminalSystem);

        CommandRegistrationCallback.EVENT.register(TerminalCommands::registerCommands);

        ServerTickEvents.END_SERVER_TICK.register(new ExploitScheduler());

        LOGGER.info("[skannamuMod] Initialized successfully! (Exploit features enabled)");
    }

    private void initializeTerminalSystem(MinecraftServer server) {
        JsonElement jsonElement = DataLoader.INSTANCE.getMissionData();

        if (jsonElement == null || !jsonElement.isJsonObject()) {
            LOGGER.error("[skannamuMod] Failed to load mission_data.json. Using defaults.");
            return;
        }

        Gson gson = new Gson();
        MissionData missionData = gson.fromJson(jsonElement, MissionData.class);

        Map<String, String> allFilesystem = new HashMap<>();
        Map<String, String> directoriesOnly = new HashMap<>();

        if (missionData.filesystem != null) {
            if (missionData.filesystem.directories != null) {
                directoriesOnly.putAll(missionData.filesystem.directories);
                allFilesystem.putAll(missionData.filesystem.directories);
            }
            if (missionData.filesystem.files != null) {
                allFilesystem.putAll(missionData.filesystem.files);
            }
        }

        ServerCommandProcessor.setFilesystem(allFilesystem, directoriesOnly);

        if (missionData.terminal_settings != null) {
            ServerCommandProcessor.setActivationKey(missionData.terminal_settings.activation_key);
        } else {
            ServerCommandProcessor.setActivationKey(null);
        }

        LOGGER.info("[skannamuMod] Terminal FAKE_FILESYSTEM and ACTIVATION_KEY initialized from JSON.");
    }
}