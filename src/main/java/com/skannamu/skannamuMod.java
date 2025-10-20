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
import com.skannamu.server.DataLoader;
import com.skannamu.server.ServerCommandProcessor;
import com.skannamu.server.ExploitScheduler;
import com.skannamu.server.TerminalCommands;
import com.skannamu.server.ServerPacketHandler;
import com.skannamu.server.command.ExploitCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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

public class skannamuMod implements ModInitializer {
    public static final String MOD_ID = "skannamu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Item PORTABLE_TERMINAL;
    public static Item STANDARD_BLOCK_ITEM;

    @Override
    public void onInitialize() {
        LOGGER.info("[skannamuMod] Initializing...");

        // --- 1. 블록 및 아이템 등록 (Block Entity Type 등록보다 먼저) ---
        BlockInitialization.initializeBlocks();
        ModItems.initializeItems();

        // 🟢 2. Block Entity 타입 등록 (이제 BlockInitialization.VAULT_BLOCK을 안전하게 참조 가능)
        VaultBlockEntities.registerBlockEntities();

        // --- 3. 페이로드 등록 ---
        PayloadTypeRegistry.playS2C().register(ExploitSequencePayload.ID, ExploitSequencePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TerminalOutputPayload.ID, TerminalOutputPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HackedStatusPayload.ID, HackedStatusPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TerminalCommandPayload.ID, TerminalCommandPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ExploitTriggerPayload.ID, ExploitTriggerPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModuleActivationPayload.ID, ModuleActivationPayload.CODEC);

        ServerPacketHandler.registerPayloads(); // VaultSliderPayload의 ID와 Codec 등록

        // --- 4. 네트워킹 핸들러 등록 ---
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

        ServerPacketHandler.registerHandlers();

        // --- 5. 기타 등록 및 초기화 ---
        ExploitScheduler.registerHandlers();
        TerminalCommands.initializeCommands();
        ExploitCommand.registerDamageType();

        PORTABLE_TERMINAL = ModItems.PORTABLE_TERMINAL;
        STANDARD_BLOCK_ITEM = Registries.ITEM.get(Identifier.of(MOD_ID, "standard_block"));

        DataLoader.registerDataLoaders();

        CommandRegistrationCallback.EVENT.register(TerminalCommands::registerCommands);
        ServerTickEvents.END_SERVER_TICK.register(new ExploitScheduler());
        LOGGER.info("[skannamuMod] Initializing complete.");
    }
}
