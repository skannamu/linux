package com.skannamu.server;

import com.skannamu.network.TerminalOutputPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerCommandProcessor {

    private static Map<UUID, PlayerState> PLAYER_STATES = new HashMap<>();


    public static void setFilesystem(Map<String, String> allFiles, Map<String, String> directoriesOnly) {
        TerminalCommands.setFilesystem(allFiles, directoriesOnly);
    }


    public static void setActivationKey(String key) {
        TerminalCommands.setActivationKey(key);
    }

    public static void processCommand(ServerPlayerEntity player, String fullCommand) {
        String output;

        String[] parts = fullCommand.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String argument = parts.length > 1 ? parts[1].trim() : "";
        output = TerminalCommands.handleCommand(player, commandName, argument);
        sendOutputToClient(player, output);
    }

    private static void sendOutputToClient(ServerPlayerEntity player, String output) {
        TerminalOutputPayload payload = new TerminalOutputPayload(output);
        ServerPlayNetworking.send(player, payload);
    }

    public static PlayerState getPlayerState(UUID playerId) {
        return PLAYER_STATES.computeIfAbsent(playerId, k -> new PlayerState());
    }

    public static boolean isPlayerActive(UUID playerId) {
        PlayerState state = PLAYER_STATES.get(playerId);
        return state != null && state.isHackerActive();
    }

    public static void setPlayerState(UUID playerId, boolean isActive, long activationTime) {
        PlayerState state = getPlayerState(playerId);
        state.setHackerActive(isActive);
        state.setActivationTime(activationTime);
    }

    public static class PlayerState {
        private boolean isHackerActive;
        private long activationTime;
        private String currentPath = "/";

        public boolean isHackerActive() {
            return isHackerActive;
        }

        public void setHackerActive(boolean hackerActive) {
            isHackerActive = hackerActive;
        }

        public long getActivationTime() {
            return activationTime;
        }

        public void setActivationTime(long activationTime) {
            this.activationTime = activationTime;
        }

        public String getCurrentPath() {
            return currentPath;
        }

        public void setCurrentPath(String currentPath) {
            this.currentPath = currentPath;
        }
    }
}