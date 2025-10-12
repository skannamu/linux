package com.skannamu;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.skannamu.init.BlockInitialization;
import com.skannamu.init.ModItems;
import com.skannamu.network.TerminalCommandPayload;
import com.skannamu.network.ExploitSequencePayload;
import com.skannamu.server.DataLoader;
import com.skannamu.server.MissionData;
import com.skannamu.server.ServerCommandProcessor;
import com.skannamu.server.ExploitScheduler;
import com.skannamu.server.TerminalCommands;
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

        BlockInitialization.initializeBlocks();
        ModItems.initializeItems();

        PORTABLE_TERMINAL = ModItems.PORTABLE_TERMINAL;
        STANDARD_BLOCK_ITEM = Registries.ITEM.get(Identifier.of(MOD_ID, "standard_block"));

        DataLoader.registerDataLoaders();

        ServerLifecycleEvents.SERVER_STARTED.register(this::initializeTerminalSystem);

        PayloadTypeRegistry.playC2S().register(TerminalCommandPayload.ID, TerminalCommandPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(TerminalCommandPayload.ID,
                (payload, context) -> {
                    MinecraftServer server = context.server();
                    ServerPlayerEntity player = context.player();
                    String command = payload.command();
                    server.execute(() -> ServerCommandProcessor.processCommand(player, command));
                });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                TerminalCommands.registerCommands(dispatcher, registryAccess, environment));

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