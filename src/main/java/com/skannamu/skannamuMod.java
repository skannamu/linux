package com.skannamu;

import com.google.gson.Gson;
import com.google.gson.JsonElement; // 사용되지 않음
import com.skannamu.init.BlockInitialization;
import com.skannamu.init.ModItems;
import com.skannamu.network.HackedStatusPayload;
import com.skannamu.network.ExploitSequencePayload;
import com.skannamu.network.ExploitTriggerPayload;
import com.skannamu.network.ModuleActivationPayload;
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

        // S2C 페이로드 등록
        PayloadTypeRegistry.playS2C().register(ExploitSequencePayload.ID, ExploitSequencePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TerminalOutputPayload.ID, TerminalOutputPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HackedStatusPayload.ID, HackedStatusPayload.CODEC);

        // C2S 페이로드 등록
        PayloadTypeRegistry.playC2S().register(TerminalCommandPayload.ID, TerminalCommandPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ExploitTriggerPayload.ID, ExploitTriggerPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModuleActivationPayload.ID, ModuleActivationPayload.CODEC);

        // 네트워킹 핸들러 등록
        ServerPlayNetworking.registerGlobalReceiver(TerminalCommandPayload.ID, (payload, context) -> {
            MinecraftServer serverInstance = context.server();
            if (serverInstance != null) {
                ServerPlayerEntity player = context.player();
                String command = payload.command();
                serverInstance.execute(() -> ServerCommandProcessor.processCommand(player, command));
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ModuleActivationPayload.ID, (payload, context) -> {
            String commandName = payload.commandName().toLowerCase();
            ServerPlayerEntity player = context.player();
            MinecraftServer server = context.server();
            LOGGER.info("[skannamuMod] Received ModuleActivationPayload for command: {} from player: {}", commandName, player.getGameProfile().getName());
            server.execute(() -> ServerCommandProcessor.handleModuleActivation(player, commandName));
        });

        ExploitScheduler.registerHandlers();
        BlockInitialization.initializeBlocks();
        ModItems.initializeItems();
        TerminalCommands.initializeCommands();
        ExploitCommand.registerDamageType();

        PORTABLE_TERMINAL = ModItems.PORTABLE_TERMINAL;
        STANDARD_BLOCK_ITEM = Registries.ITEM.get(Identifier.of(MOD_ID, "standard_block"));

        DataLoader.registerDataLoaders();

        // 💡 기존 initializeTerminalSystem 호출을 제거하거나, 비워둡니다.
        // 데이터 로딩 및 TerminalCommands 초기화는 DataLoader.reload()에서 이미 수행됩니다.
        // ServerLifecycleEvents.SERVER_STARTED.register(this::initializeTerminalSystem); // 제거

        CommandRegistrationCallback.EVENT.register(TerminalCommands::registerCommands);
        ServerTickEvents.END_SERVER_TICK.register(new ExploitScheduler());
        LOGGER.info("[skannamuMod] Initializing complete.");
    }

    /**
     * 💡 MissionData 초기화 로직은 DataLoader.reload()로 이동했으므로,
     * 이 메서드는 더 이상 필요하지 않습니다. 주석 처리하거나 제거합니다.
     */
    /*
    private void initializeTerminalSystem(MinecraftServer server) {
        JsonElement jsonElement = DataLoader.INSTANCE.getMissionData(); // 이 부분이 오류를 일으킴

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
    */
}