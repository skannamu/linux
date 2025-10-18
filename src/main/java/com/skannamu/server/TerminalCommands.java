package com.skannamu.server;

import com.skannamu.server.command.*;
import com.skannamu.network.HackedStatusPayload;
import com.skannamu.init.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.*;

public class TerminalCommands {

    public static Map<String, String> FAKE_FILESYSTEM = null;
    public static Map<String, String> FAKE_DIRECTORIES = null;
    public static String ACTIVATION_KEY = null;
    private static FilesystemService FILE_SERVICE = null;
    private static final Map<String, ICommand> COMMAND_REGISTRY = new HashMap<>();

    public static void initializeCommands() {
        if (!COMMAND_REGISTRY.isEmpty()) return;

        registerCommand(new LsCommand());
        registerCommand(new CatCommand());
        registerCommand(new CdCommand());
        registerCommand(new DecryptCommand());
        registerCommand(new CalcCommand());
        registerCommand(new KeyCommand());
        registerCommand(new PwdCommand());
        registerCommand(new ExploitCommand());
        registerCommand(new AuxiliaryCommand());
        registerCommand(new EchoCommand());
    }

    public static void setFilesystemService(MissionData missionData){
        if(missionData != null){
            FILE_SERVICE = new FilesystemService(missionData);

            // 💡 핵심 수정: FAKE_FILESYSTEM과 FAKE_DIRECTORIES가 MissionData의 맵을 직접 참조하도록 설정
            FAKE_FILESYSTEM = missionData.filesystem.files;
            FAKE_DIRECTORIES = missionData.filesystem.directories;

            setActivationKey(missionData.terminal_settings.activation_key);
        }
        else {}
    }

    public static FilesystemService getFileService(){
        return FILE_SERVICE;
    }

    public static void registerCommand(ICommand command) {
        COMMAND_REGISTRY.put(command.getName().toLowerCase(), command);
    }

    public static Set<String> getAllCommandNames() {
        return COMMAND_REGISTRY.keySet();
    }

    // 💡 주의: 이 메서드는 이제 FAKE_FILESYSTEM을 직접 할당하지 않고,
    // FAKE_FILESYSTEM이 null일 때 초기 에러 메시지를 설정하는 안전 장치 역할만 합니다.
    public static void setFilesystem(Map<String, String> allFiles, Map<String, String> directoriesOnly) {
        // 이전에 FAKE_FILESYSTEM = allFiles; 이 있었으나, setFilesystemService에서
        // missionData의 맵을 직접 할당하는 방식으로 변경되었으므로 이 부분은 필요 없어졌습니다.

        if (FAKE_FILESYSTEM == null || FAKE_DIRECTORIES == null) {
            FAKE_FILESYSTEM = new HashMap<>();
            FAKE_DIRECTORIES = new HashMap<>();
            FAKE_FILESYSTEM.put("/", "Error loading filesystem. Check server logs.");
            FAKE_DIRECTORIES.put("/", "help.txt");
        }
    }

    public static void setActivationKey(String key) {
        ACTIVATION_KEY = key;
    }

    private static boolean hasIffModule(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        for (ItemStack stack : inventory.getMainStacks()) {
            if (!stack.isEmpty() && stack.getItem() == ModItems.EMP_IFF_MODULE) {
                return true;
            }
        }
        return false;
    }


    public static String handleCommand(ServerPlayerEntity player, String commandName, String argument) {

        if (FAKE_FILESYSTEM == null || FAKE_DIRECTORIES == null) {
            return "Error: Terminal system data is not initialized. Please notify the administrator.";
        }
        // ... (나머지 handleCommand 메서드 내용 생략) ...
        String lowerCommand = commandName.toLowerCase();
        ICommand command = COMMAND_REGISTRY.get(lowerCommand);

        if (command == null) {
            return "Error: Command '" + commandName + "' not found. Type 'cat help.txt' for usage.";
        }

        ServerCommandProcessor.PlayerState state = getPlayerState(player.getUuid());
        if (!state.isCommandAvailable(lowerCommand)) {
            return "Error: Command '" + commandName + "' module is missing. Find and 'install' the binary module.";
        }

        List<String> options = new ArrayList<>();
        String rawArgument = argument.trim();

        String[] parts = rawArgument.split("\\s+");
        StringBuilder argBuilder = new StringBuilder();

        for (String part : parts) {
            if (part.startsWith("-") && part.length() > 1) {

                for (char optionChar : part.substring(1).toCharArray()) {
                    options.add(String.valueOf(optionChar));
                }
            } else if (!part.isBlank()) {
                if (argBuilder.length() > 0) argBuilder.append(" ");
                argBuilder.append(part);
            }
        }
        String remainingArgument = argBuilder.toString().trim();

        if (options.contains("h")) {
            return command.getUsage();
        }
        return command.execute(player, options, remainingArgument);
    }

    public static String handlePromptInput(ServerPlayerEntity player, String input) {
        ServerCommandProcessor.PlayerState state = getPlayerState(player.getUuid());

        switch (state.getCurrentCommandState()) {
            case EMP_RANGE_PROMPT:
                if (input.toLowerCase().startsWith("set range ")) {
                    try {
                        int range = Integer.parseInt(input.substring(10).trim());
                        if (range < 5 || range > 30) {
                            return "Error: Range must be between 5 and 30 blocks. Enter again.";
                        }
                        state.setEmpRange(range);
                        state.setCurrentCommandState(ServerCommandProcessor.PlayerState.CommandState.EMP_DURATION_PROMPT);
                        return "Range set to " + range + ". Enter duration (5-60 seconds): >> set duration <value>";
                    } catch (NumberFormatException e) {
                        return "Error: Invalid range value. Enter again. >> set range <value>";
                    }
                }
                return "Invalid command. Expected 'set range <value>'.";

            case EMP_DURATION_PROMPT:
                if (input.toLowerCase().startsWith("set duration ")) {
                    try {
                        int duration = Integer.parseInt(input.substring(13).trim());
                        if (duration < 5 || duration > 60) {
                            return "Error: Duration must be between 5 and 60 seconds. Enter again.";
                        }
                        state.setEmpDuration(duration);
                        state.setCurrentCommandState(ServerCommandProcessor.PlayerState.CommandState.EMP_IFF_PROMPT);

                        // IFF 모듈 소지 여부에 따라 다른 메시지 출력
                        if (hasIffModule(player)) {
                            return "Duration set to " + duration + "s. IFF module detected. Enable friendly fire avoidance? (y/n) >>";
                        } else {
                            return "Duration set to " + duration + "s. IFF module NOT detected. Enable friendly fire avoidance? (n only) >>";
                        }
                    } catch (NumberFormatException e) {
                        return "Error: Invalid duration value. Enter again. >> set duration <value>";
                    }
                }
                return "Invalid command. Expected 'set duration <value>'.";

            case EMP_IFF_PROMPT:
                return executeEmp(player, input.trim().toLowerCase(), state);

            case INACTIVE:
            default:
                return "Error: System state internal error. Command sequence reset.";
        }
    }

    private static String executeEmp(ServerPlayerEntity player, String input, ServerCommandProcessor.PlayerState state) {

        boolean wantsIff = input.equals("y");
        boolean hasIff = hasIffModule(player);

        // 💡 IFF 모듈이 없는데 'y'를 입력한 경우 (무한 루프 방지 로직 유지)
        if (wantsIff && !hasIff) {
            return "Error: IFF module not detected. Cannot enable friendly fire avoidance.\nEnable friendly fire avoidance? (n only) >>";
        }

        if (!input.equals("y") && !input.equals("n")) {
            return "Invalid input. Please enter 'y' or 'n'.\nEnable friendly fire avoidance? (y/n) >>";
        }


        int range = state.getEmpRange();
        int durationSeconds = state.getEmpDuration();
        int durationTicks = durationSeconds * 20;

        performEmp(player, range, durationTicks, wantsIff);

        state.setEmpRange(0);
        state.setEmpDuration(0);
        state.setCurrentCommandState(ServerCommandProcessor.PlayerState.CommandState.INACTIVE);

        return String.format("EMP burst initiated. Range: %d blocks, Duration: %d seconds. IFF: %s",
                range, durationSeconds, wantsIff ? "Enabled" : "Disabled");
    }

    private static void performEmp(ServerPlayerEntity empInitiator, int range, int durationTicks, boolean wantsIff) {
        MinecraftServer server = empInitiator.getServer();
        for (ServerPlayerEntity targetPlayer : empInitiator.getServer().getPlayerManager().getPlayerList()) {

            if (targetPlayer.equals(empInitiator)) {
                continue;
            }

            if (empInitiator.distanceTo(targetPlayer) <= range) {

                if (wantsIff && hasIffModule(empInitiator)) {}

                ServerCommandProcessor.PlayerState targetState = getPlayerState(targetPlayer.getUuid());
                targetState.setHacked(true, durationTicks, server);

                HackedStatusPayload payload = new HackedStatusPayload(true);
                ServerPlayNetworking.send(targetPlayer, payload);

                targetPlayer.sendMessage(Text.literal("❗️ EMP incoming... Terminal systems shutting down."), true);
            }
        }
    }


    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
    }

    public static String normalizePath(String path) {
        if (path.isEmpty()) return "/";
        String normalized = path.replaceAll("//+", "/");
        if (normalized.length() > 1 && normalized.endsWith("/") && !normalized.equals("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.isEmpty() ? "/" : normalized;
    }
    public static ServerCommandProcessor.PlayerState getPlayerState(UUID playerId) {
        return ServerCommandProcessor.getPlayerState(playerId);
    }
    public static String getCurrentPlayerPath(ServerPlayerEntity player) {
        return getPlayerState(player.getUuid()).getCurrentPath();
    }
    public static String getAbsolutePath(ServerPlayerEntity player, String path) {
        if (path.startsWith("/")) {
            return normalizePath(path);
        }
        String currentPath = getCurrentPlayerPath(player);
        String base = currentPath.equals("/") ? "" : currentPath;
        return normalizePath(base + "/" + path);
    }
}