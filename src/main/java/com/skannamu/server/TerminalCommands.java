package com.skannamu.server;

import com.skannamu.server.command.*; // ICommand와 모든 명령어 클래스 임포트
// Fabric 명령어 등록을 위한 임포트 추가
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.CommandRegistryAccess;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.*;

public class TerminalCommands {

    public static Map<String, String> FAKE_FILESYSTEM = null;
    public static Map<String, String> FAKE_DIRECTORIES = null;
    public static String ACTIVATION_KEY = null;

    private static final Map<String, ICommand> COMMAND_REGISTRY = new HashMap<>();

    static {
        // 모든 명령어 인스턴스 등록
        registerCommand(new LsCommand());
        registerCommand(new CatCommand());
        registerCommand(new CdCommand());
        registerCommand(new DecryptCommand());
        registerCommand(new CalcCommand());
        registerCommand(new KeyCommand());
        registerCommand(new PwdCommand());
        registerCommand(new ExploitCommand());
    }

    public static void registerCommand(ICommand command) {
        COMMAND_REGISTRY.put(command.getName().toLowerCase(), command);
    }

    public static Set<String> getAllCommandNames() {
        return COMMAND_REGISTRY.keySet();
    }

    public static void setFilesystem(Map<String, String> allFiles, Map<String, String> directoriesOnly) {
        FAKE_FILESYSTEM = allFiles;
        FAKE_DIRECTORIES = directoriesOnly;

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

    public static String handleCommand(ServerPlayerEntity player, String commandName, String argument) {

        if (FAKE_FILESYSTEM == null || FAKE_DIRECTORIES == null) {
            return "Error: Terminal system data is not initialized. Please notify the administrator.";
        }

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
        StringBuilder argBuilder = new StringBuilder(); // 순수 인수를 모을 빌더

        for (String part : parts) {
            if (part.startsWith("-") && part.length() > 1) {

                for (char optionChar : part.substring(1).toCharArray()) {
                    options.add(String.valueOf(optionChar));
                }
            } else if (!part.isBlank()) {
                // 옵션이 아닌 나머지 부분 (순수 인수)만 다시 모읍니다.
                if (argBuilder.length() > 0) argBuilder.append(" ");
                argBuilder.append(part);
            }
        }
        String remainingArgument = argBuilder.toString().trim();

        if (options.contains("h")) {
            return command.getUsage(); // 여기서 즉시 반환하여 execute 호출을 방지합니다.
        }
        return command.execute(player, options, remainingArgument);
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
        // ServerCommandProcessor 클래스가 이 패키지에 존재한다고 가정합니다.
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