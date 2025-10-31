package com.skannamu;

import com.skannamu.init.BlockInitialization;
import com.skannamu.init.ModItems;
import com.skannamu.init.VaultBlockEntities;
import com.skannamu.network.HackedStatusPayload;
import com.skannamu.network.ExploitSequencePayload;
import com.skannamu.network.ExploitTriggerPayload;
import com.skannamu.network.ModuleActivationPayload;
import com.skannamu.network.TerminalOutputPayload;
import com.skannamu.network.TerminalCommandPayload;
import com.skannamu.server.DatabaseService;
import com.skannamu.server.FilesystemService;
import com.skannamu.server.MissionData;
import com.skannamu.server.DataLoader;
import com.skannamu.server.ServerCommandProcessor;
import com.skannamu.server.ExploitScheduler;
import com.skannamu.server.TerminalCommands;
import com.skannamu.server.ServerPacketHandler;
import com.skannamu.server.command.ExploitCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class skannamuMod implements ModInitializer {
    public static final String MOD_ID = "skannamu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Item PORTABLE_TERMINAL;
    public static Item STANDARD_BLOCK_ITEM;

    private static DatabaseService databaseService;
    private static FilesystemService filesystemService;

    @Override
    public void onInitialize() {
        LOGGER.info("[skannamuMod] Initializing...");

        BlockInitialization.initializeBlocks();
        ModItems.initializeItems();
        VaultBlockEntities.registerBlockEntities();

        // Payload 등록 (S2C 및 C2S)
        PayloadTypeRegistry.playS2C().register(TerminalOutputPayload.ID, TerminalOutputPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TerminalCommandPayload.ID, TerminalCommandPayload.CODEC);
        ServerPacketHandler.registerPayloads();
        ServerPacketHandler.registerHandlers();
        ServerCommandProcessor.registerPayloadsAndHandlers();

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

        ServerLifecycleEvents.SERVER_STARTING.register(skannamuMod::onServerStarting);
        DataLoader.registerDataLoaders();
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(skannamuMod::onDataPackReloadEnd);

        ServerPlayConnectionEvents.JOIN.register(skannamuMod::onPlayerJoin);

        TerminalCommands.initializeCommands();

        PORTABLE_TERMINAL = ModItems.PORTABLE_TERMINAL;
        STANDARD_BLOCK_ITEM = Registries.ITEM.get(Identifier.of(MOD_ID, "standard_block"));

        ServerTickEvents.END_SERVER_TICK.register(new ExploitScheduler());

    }

    private static void onServerStarting(MinecraftServer server) {
        LOGGER.info("[skannamuMod] Server starting: Initializing DatabaseService...");

        databaseService = new DatabaseService(server.getSavePath(WorldSavePath.ROOT));
        databaseService.createTables();

        filesystemService = new FilesystemService(new MissionData(), databaseService);
        TerminalCommands.setFilesystemService(filesystemService);

        filesystemService.initializeGlobalPaths();
        ServerTickEvents.END_SERVER_TICK.register(skannamuMod::onServerTickEnd);
    }

    private static void onDataPackReloadEnd(MinecraftServer server, net.minecraft.resource.ResourceManager resourceManager, boolean success) {

        if (databaseService == null || filesystemService == null) {
            LOGGER.error("[skannamuMod] Database or Filesystem Service is NULL! Cannot reload data.");
            return;
        }

        MissionData missionData = DataLoader.INSTANCE.getMissionDataInstance();

        if (missionData != null) {
            filesystemService = new FilesystemService(missionData, databaseService);
            TerminalCommands.setFilesystemService(filesystemService);

            if (missionData.terminal_settings != null) {
                TerminalCommands.setActivationKey(missionData.terminal_settings.activation_key);
            } else {
                TerminalCommands.setActivationKey("DEFAULT_KEY");
            }

        } else {
        }
    }

    private static void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        ServerPlayerEntity player = handler.player;

        ServerCommandProcessor.PlayerState state = ServerCommandProcessor.getPlayerState(player);

        if (TerminalCommands.getFileService() != null) {
            TerminalCommands.getFileService().createDirectory(player.getUuid(), state.getCurrentPath());
        } else {
            LOGGER.warn("[skannamuMod] Player {} joined before FSS was fully initialized. Terminal commands may fail.", player.getGameProfile().getName());
        }
    }


    private static void onServerTickEnd(MinecraftServer server) {
        long currentTick = server.getTicks();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerCommandProcessor.PlayerState state = ServerCommandProcessor.getPlayerState(player);
            if (state.isHacked() && state.getHackedUntilTick() <= currentTick) {
                state.setHacked(false, 0, server);
                ServerPlayNetworking.send(player, new HackedStatusPayload(false));
                player.sendMessage(Text.literal("✅ Terminal systems restored."), true);
            }
        }
    }
}