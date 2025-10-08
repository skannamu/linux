package com.skannamu.server;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TerminalCommands {

    public static Map<String, String> FAKE_FILESYSTEM = null;
    public static Map<String, String> FAKE_DIRECTORIES = null;
    private static String ACTIVATION_KEY = null;

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

        switch (commandName.toLowerCase()) {
            case "ls":
                return handleLsCommand(player, argument);
            case "cat":
                return handleCatCommand(player, argument);
            case "cd":
                return handleCdCommand(player, argument);
            case "decrypt":
                return handleDecryptCommand(argument);
            case "calc":
                return handleCalcCommand(argument);
            case "key":
                return handleKeyCommand(player, argument);
            default:
                return "Error: Command '" + commandName + "' not found. Type 'cat help.txt' for usage.";
        }
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

    private static ServerCommandProcessor.PlayerState getPlayerState(UUID playerId) {
        return ServerCommandProcessor.getPlayerState(playerId);
    }

    private static String getCurrentPlayerPath(ServerPlayerEntity player) {
        return getPlayerState(player.getUuid()).getCurrentPath();
    }

    private static String getAbsolutePath(ServerPlayerEntity player, String path) {
        if (path.startsWith("/")) {
            return normalizePath(path);
        }

        String currentPath = getCurrentPlayerPath(player);
        String base = currentPath.equals("/") ? "" : currentPath;
        return normalizePath(base + "/" + path);
    }

    private static String handleLsCommand(ServerPlayerEntity player, String path) {
        String targetPath = path.isEmpty() ? getCurrentPlayerPath(player) : getAbsolutePath(player, path);

        if (FAKE_DIRECTORIES.containsKey(targetPath)) {
            return "Contents of " + targetPath + ":\n" + FAKE_FILESYSTEM.get(targetPath);
        } else {
            return "Error: Directory or path not found: " + targetPath;
        }
    }

    private static String handleCatCommand(ServerPlayerEntity player, String path) {
        if (path.isBlank()) {
            return "Usage: cat <file_path>";
        }

        String targetPath = getAbsolutePath(player, path);

        if (FAKE_FILESYSTEM.containsKey(targetPath)) {
            if (FAKE_DIRECTORIES.containsKey(targetPath)) {
                return "Error: Cannot cat a directory: " + targetPath;
            }
            return FAKE_FILESYSTEM.get(targetPath);
        }

        return "Error: File not found or unreadable: " + path;
    }

    private static String handleCdCommand(ServerPlayerEntity player, String argument) {
        if (argument.isBlank()) {
            return "Error: Usage: cd <directory_path>";
        }

        ServerCommandProcessor.PlayerState state = getPlayerState(player.getUuid());
        String currentPath = state.getCurrentPath();
        String targetPath;

        if (argument.equals("..")) {
            if (currentPath.equals("/")) {
                return "Cannot move up from root directory.";
            }

            String path = currentPath;
            int lastSlash = path.lastIndexOf('/');
            targetPath = (lastSlash <= 0) ? "/" : path.substring(0, lastSlash);

        } else {
            targetPath = getAbsolutePath(player, argument);
        }

        targetPath = normalizePath(targetPath);

        if (FAKE_DIRECTORIES.containsKey(targetPath)) {
            state.setCurrentPath(targetPath);
            return "Directory changed to: " + targetPath;
        } else {
            if (FAKE_FILESYSTEM.containsKey(targetPath)) {
                return "Error: Cannot change directory to a file: " + argument;
            }
            return "Error: Directory not found: " + argument;
        }
    }

    private static String handleDecryptCommand(String argument) {
        if (argument.isBlank()) {
            return "Usage: decrypt [base64_string]";
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(argument);
            String decodedString = new String(decodedBytes);
            return "Decryption successful:\n" + decodedString;
        } catch (IllegalArgumentException e) {
            return "Error: Invalid Base64 format or corrupted data.";
        }
    }

    private static String handleCalcCommand(String argument) {
        if (argument.isBlank()) {
            return "Usage: calc <num1>[+-*/]<num2>...";
        }

        String expression = argument.replaceAll("\\s+", "");

        Pattern numberPattern = Pattern.compile("[-+]?\\d+(\\.\\d+)?");
        Matcher numberMatcher = numberPattern.matcher(expression);

        Pattern operatorPattern = Pattern.compile("[+\\-*/]");
        Matcher operatorMatcher = operatorPattern.matcher(expression);

        if (!numberMatcher.find()) return "Error: No numbers found in expression.";

        try {
            double result = Double.parseDouble(numberMatcher.group());

            while (operatorMatcher.find() && numberMatcher.find(operatorMatcher.end())) {
                String operator = operatorMatcher.group();
                double nextNum = Double.parseDouble(numberMatcher.group());

                switch (operator) {
                    case "+": result += nextNum; break;
                    case "-": result -= nextNum; break;
                    case "*": result *= nextNum; break;
                    case "/":
                        if (nextNum == 0) return "Error: Division by zero.";
                        result /= nextNum;
                        break;
                    default: return "Error: Unsupported operator found.";
                }
            }

            return "Result: " + String.format("%.2f", result).replaceAll("\\.00$", "");

        } catch (Exception e) {
            return "Error: Failed to parse expression. Check format.";
        }
    }

    private static String handleKeyCommand(ServerPlayerEntity player, String argument) {
        if (argument.isBlank()) {
            return "Usage: key [activation_code]";
        }
        UUID playerId = player.getUuid();
        if (argument.equals(ACTIVATION_KEY)) {
            ServerCommandProcessor.PlayerState state = getPlayerState(playerId);
            state.setHackerActive(true);
            state.setActivationTime(System.currentTimeMillis());

            player.sendAbilitiesUpdate();

            return "Key accepted. System access level upgraded to: ACTIVE.\n(You may now use the company terminal.)";
        } else {
            return "Key rejected. Incorrect or expired code.";
        }
    }
}