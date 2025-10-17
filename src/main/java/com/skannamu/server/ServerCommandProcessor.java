package com.skannamu.server;

import com.skannamu.network.TerminalOutputPayload;
import com.skannamu.network.ModuleActivationPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ServerCommandProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger("skannamu-ServerCommandProcessor");
    private static Map<UUID, PlayerState> PLAYER_STATES = new HashMap<>();

    public static void registerPayloadsAndHandlers() {
        LOGGER.info("Registering ModuleActivationPayload handler...");
        PayloadTypeRegistry.playC2S().register(ModuleActivationPayload.ID, ModuleActivationPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ModuleActivationPayload.ID, (payload, context) -> {
            String commandName = payload.commandName().toLowerCase();
            ServerPlayerEntity player = context.player();
            MinecraftServer server = context.server();
            LOGGER.info("Received ModuleActivationPayload for command: {} from player: {}", commandName, player.getGameProfile().getName());

            server.execute(() -> handleModuleActivation(player, commandName));
        });
    }

    public static void handleModuleActivation(ServerPlayerEntity player, String commandName) {
        PlayerState state = getPlayerState(player.getUuid());
        LOGGER.info("Handling activation for player: {}, command: {}, availableCommands before: {}",
                player.getGameProfile().getName(), commandName, state.availableCommands);

        if (state.isCommandAvailable(commandName)) {
            player.sendMessage(Text.literal("Terminal: Command '" + commandName + "' is already active."), true);
            return;
        }
        if (commandName.equals("exploit") || commandName.equals("auxiliary")) {
            state.addCommand(commandName);
            LOGGER.info("Added command: {} to player: {}, availableCommands after: {}",
                    commandName, player.getGameProfile().getName(), state.availableCommands);
            player.sendMessage(Text.literal("Terminal: New module '" + commandName + "' activated. Use '" + commandName + " -h' for details."), true);
        } else {
            player.sendMessage(Text.literal("Terminal: Failed to activate unknown command module: " + commandName), true);
        }
    }

    public static void setFilesystem(Map<String, String> allFiles, Map<String, String> directoriesOnly) {
        TerminalCommands.setFilesystem(allFiles, directoriesOnly);
    }

    public static void setActivationKey(String key) {
        TerminalCommands.setActivationKey(key);
    }

    public static void processCommand(ServerPlayerEntity player, String fullCommand) {
        String output;

        PlayerState state = getPlayerState(player.getUuid());

        if (state.getCurrentCommandState() != PlayerState.CommandState.INACTIVE) {
            output = TerminalCommands.handlePromptInput(player, fullCommand.trim());
            sendOutputToClient(player, output);
            return;
        }

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
        PlayerState state = getPlayerState(playerId);
        return state.isHackerActive();
    }

    public static class PlayerState {
        private boolean isHackerActive = false;
        private long activationTime = 0;
        private String currentPath = "/";

        private final Set<String> availableCommands = new HashSet<>();

        private boolean isHacked = false;
        private long hackedUntilTick = 0;

        public enum CommandState {
            INACTIVE, EMP_RANGE_PROMPT, EMP_DURATION_PROMPT, EMP_IFF_PROMPT
        }
        private CommandState currentCommandState = CommandState.INACTIVE;
        private int empRange = 0;
        private int empDuration = 0;

        public PlayerState() {
            availableCommands.add("ls");
            availableCommands.add("cd");
            availableCommands.add("pwd");
            availableCommands.add("help");
        }

        public boolean isHackerActive() { return isHackerActive; }
        public void setHackerActive(boolean hackerActive) { isHackerActive = hackerActive; }
        public long getActivationTime() { return activationTime; }
        public void setActivationTime(long activationTime) { this.activationTime = activationTime; }
        public String getCurrentPath() { return currentPath; }
        public void setCurrentPath(String currentPath) { this.currentPath = currentPath; }
        public boolean isCommandAvailable(String commandName) { return availableCommands.contains(commandName.toLowerCase()); }
        public void addCommand(String commandName) { availableCommands.add(commandName.toLowerCase()); }
        public boolean isHacked() { return isHacked; }

        public void setHacked(boolean hacked, int durationInTicks, MinecraftServer server) {
            this.isHacked = hacked;
            if (hacked) {
                long currentTick = server.getTicks();
                this.hackedUntilTick = currentTick + durationInTicks;
            } else {
                this.hackedUntilTick = 0;
            }
        }
        public long getHackedUntilTick() { return hackedUntilTick; }

        public CommandState getCurrentCommandState() { return currentCommandState; }
        public void setCurrentCommandState(CommandState state) { this.currentCommandState = state; }

        public int getEmpRange() { return empRange; }
        public void setEmpRange(int empRange) { this.empRange = empRange; }
        public int getEmpDuration() { return empDuration; }
        public void setEmpDuration(int empDuration) { this.empDuration = empDuration; }
    }
}