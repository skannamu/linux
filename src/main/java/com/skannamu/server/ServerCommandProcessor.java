// src/main/java/com/skannamu/server/ServerCommandProcessor.java

package com.skannamu.server;

import com.skannamu.network.TerminalOutputPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerCommandProcessor {

    private static Map<String, String> FAKE_FILESYSTEM = null;
    private static String ACTIVATION_KEY = null;
    private static Map<UUID, PlayerState> PLAYER_STATES = new HashMap<>();

    private static final String CURRENT_PATH = "/";

    public static void setFilesystem(Map<String, String> newFilesystem) {
        FAKE_FILESYSTEM = newFilesystem;
        if (FAKE_FILESYSTEM == null) {
            FAKE_FILESYSTEM = new HashMap<>();
            FAKE_FILESYSTEM.put("/", "Error loading filesystem. Check server logs.");
        }
    }

    public static void setActivationKey(String key) {
        ACTIVATION_KEY = key;
        if (ACTIVATION_KEY == null || ACTIVATION_KEY.isEmpty()) {
            ACTIVATION_KEY = "DEFAULT_KEY";
        }
    }

    public static void processCommand(ServerPlayerEntity player, String fullCommand) {
        String output;

        if (FAKE_FILESYSTEM == null) {
            output = "Error: Terminal system data is not initialized. Please notify the administrator.";
            sendOutputToClient(player, output);
            return;
        }

        String[] parts = fullCommand.trim().split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String argument = parts.length > 1 ? parts[1].trim() : "";

        switch (command) {
            case "ls":
                output = handleLsCommand(argument);
                break;
            case "cat":
                output = handleCatCommand(argument);
                break;
            case "decrypt":
                output = handleDecryptCommand(argument);
                break;
            case "calc":
                output = handleCalcCommand(argument);
                break;
            case "key":
                output = handleKeyCommand(player, argument);
                break;
            default:
                output = "Error: Command '" + command + "' not found. Type 'cat help.txt' for usage.";
                break;
        }

        sendOutputToClient(player, output);
    }

    private static String handleLsCommand(String path) {
        String targetPath = path.isEmpty() ? CURRENT_PATH : path;

        if (FAKE_FILESYSTEM.containsKey(targetPath)) {
            return "Contents of " + targetPath + ":\n" + FAKE_FILESYSTEM.get(targetPath);
        } else {
            return "Error: Directory or path not found: " + targetPath;
        }
    }

    private static String handleCatCommand(String path) {
        if (FAKE_FILESYSTEM.containsKey(path)) {
            return FAKE_FILESYSTEM.get(path);
        }

        String fullPath = CURRENT_PATH + path;
        if (FAKE_FILESYSTEM.containsKey(fullPath)) {
            return FAKE_FILESYSTEM.get(fullPath);
        }

        return "Error: File not found or unreadable: " + path;
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
            PlayerState state = PLAYER_STATES.computeIfAbsent(playerId, k -> new PlayerState());
            state.setHackerActive(true);
            state.setActivationTime(System.currentTimeMillis());

            player.sendAbilitiesUpdate();

            return "Key accepted. System access level upgraded to: ACTIVE.\n(You may now use the company terminal.)";
        } else {
            return "Key rejected. Incorrect or expired code.";
        }
    }

    private static void sendOutputToClient(ServerPlayerEntity player, String output) {
        TerminalOutputPayload payload = new TerminalOutputPayload(output);
        ServerPlayNetworking.send(player, payload);
    }

    // 플레이어 상태를 조회하는 public 메서드 추가
    public static boolean isPlayerActive(UUID playerId) {
        PlayerState state = PLAYER_STATES.get(playerId);
        return state != null && state.isHackerActive();
    }

    // 플레이어 상태를 초기화하거나 가져오는 메서드 (선택 사항)
    public static void setPlayerState(UUID playerId, boolean isActive, long activationTime) {
        PlayerState state = PLAYER_STATES.computeIfAbsent(playerId, k -> new PlayerState());
        state.setHackerActive(isActive);
        state.setActivationTime(activationTime);
    }

    // 플레이어 상태를 저장하는 내부 클래스
    private static class PlayerState {
        private boolean isHackerActive;
        private long activationTime;

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
    }
}