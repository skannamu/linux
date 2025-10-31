package com.skannamu.server;

import com.skannamu.server.command.*;
import com.skannamu.network.HackedStatusPayload;
import com.skannamu.network.TerminalOutputPayload;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TerminalCommands {

    public static Map<String, String> FAKE_FILESYSTEM = null;
    public static Map<String, String> FAKE_DIRECTORIES = null;
    public static String ACTIVATION_KEY = null;
    private static FilesystemService FILE_SERVICE = null;
    private static final Map<String, ICommand> COMMAND_REGISTRY = new HashMap<>();

    private static final Set<String> INTERNAL_COMMANDS = new HashSet<>();

    public static void initializeCommands() {
        if (!COMMAND_REGISTRY.isEmpty()) return;

        registerCommand(new LsCommand());
        registerCommand(new CatCommand());
        registerCommand(new CdCommand());
        registerCommand(new DecryptCommand());
        registerCommand(new CalcCommand());
        registerCommand(new KeyCommand());
        registerCommand(new PwdCommand());
        registerCommand(new EchoCommand());
        registerCommand(new MkdirCommand());
        registerCommand(new RmCommand());
    }
    public static void setFilesystemService(FilesystemService fileService){
        FILE_SERVICE = fileService;

    }

    public static FilesystemService getFileService(){
        return FILE_SERVICE;
    }

    public static void registerCommand(ICommand command) {
        String name = command.getName().toLowerCase();
        COMMAND_REGISTRY.put(name, command);
        INTERNAL_COMMANDS.add(name);
    }
    public static Set<String> getAllCommandNames() {
        return COMMAND_REGISTRY.keySet();
    }

    public static void setActivationKey(String key) {
        ACTIVATION_KEY = key;
    }
    public static void sendCwdUpdate(ServerPlayerEntity player, String newPath) {
        ServerPlayNetworking.send(player, new TerminalOutputPayload("@@CWD:" + newPath));
    }

    public static String handleCommand(ServerPlayerEntity player, String commandName, String argument) {

        if (FILE_SERVICE == null) {
            return "Error: Terminal system data is not initialized. Please notify the administrator.";
        }

        String lowerCommand = commandName.toLowerCase();
        String fullCommand = commandName + (argument.isEmpty() ? "" : " " + argument);

        if (INTERNAL_COMMANDS.contains(lowerCommand)) {
            ICommand command = COMMAND_REGISTRY.get(lowerCommand);

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

        else {
            if (player.hasPermissionLevel(3)) {
                return executeOSCommand(fullCommand);
            } else {
                return "Error: Command '" + commandName + "' not found.\nAccess to external shell commands is denied (Insufficient Privileges).";
            }
        }
    }

    private static String executeOSCommand(String command) {
        StringBuilder output = new StringBuilder();

        String os = System.getProperty("os.name").toLowerCase();
        List<String> commandList = new ArrayList<>();

        if (os.contains("win")) {
            commandList.add("wsl");
            commandList.add("sh");
            commandList.add("-c");
            commandList.add(command);
        } else {
            commandList.add("sh");
            commandList.add("-c");
            commandList.add(command);
        }

        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(commandList);
            pb.redirectErrorStream(true);
            process = pb.start();
            String encoding = "UTF-8";

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), encoding))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return "OS Error: Command timed out after 5 seconds.";
            }
        } catch (IOException e) {
            return "OS Error: Failed to execute command.\nDetails: " + e.getMessage() +
                    (os.contains("win") ? "\n(HINT: Check if 'wsl' is installed and in your system PATH.)" : "");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) process.destroyForcibly();
            return "OS Error: Command execution interrupted.";
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }

        String result = output.toString().trim();
        return result.isEmpty() ? "" : result;
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