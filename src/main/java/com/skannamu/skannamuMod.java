// src/main/java/com/skannamu/skannamuMod.java (오류 수정)

package com.skannamu;

import com.skannamu.init.BlockInitialization;
import com.skannamu.init.ItemInitialization;
import com.skannamu.network.TerminalCommandPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import com.skannamu.server.DataLoader;
import com.skannamu.server.ServerCommandProcessor;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class skannamuMod implements ModInitializer {
    public static final String MOD_ID = "skannamu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ItemInitialization.initializeItems();
        BlockInitialization.initializeBlocks();
        DataLoader.registerDataLoaders();
        ServerPlayNetworking.registerGlobalReceiver(TerminalCommandPayload.ID,
                (TerminalCommandPayload payload, ServerPlayNetworking.Context context) -> {
                    MinecraftServer server = context.player().getServer();
                    final ServerPlayerEntity player = context.player();
                    String command = payload.command();
                    server.execute(() -> {
                        ServerCommandProcessor.processCommand(player, command);
                    });
                });
        LOGGER.info("skannamuMod initialized!");
    }
}