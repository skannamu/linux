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

    // ⚡️ 삭제: 모든 플레이어의 경로를 관리해야 하므로 정적 필드 삭제
    // private static final String CURRENT_PATH = "/";

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

    // ⚡️ 추가: 플레이어의 현재 경로를 가져오는 헬퍼 메서드
    private static String getCurrentPath(UUID playerId) {
        return PLAYER_STATES.computeIfAbsent(playerId, k -> new PlayerState()).getCurrentPath();
    }

    // ⚡️ 추가: 플레이어의 현재 경로를 설정하는 헬퍼 메서드
    private static void setCurrentPath(UUID playerId, String path) {
        PLAYER_STATES.computeIfAbsent(playerId, k -> new PlayerState()).setCurrentPath(path);
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

        // ⚡️ 현재 경로를 가져옴
        String currentPath = getCurrentPath(player.getUuid());

        switch (command) {
            case "ls":
                output = handleLsCommand(currentPath, argument); // 현재 경로 인자 추가
                break;
            case "cat":
                output = handleCatCommand(currentPath, argument); // 현재 경로 인자 추가
                break;
            case "cd": // ⚡️ cd 명령 추가
                output = handleCdCommand(player, currentPath, argument);
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

    // ⚡️ 수정: ls 명령 핸들러 (현재 경로 사용)
    private static String handleLsCommand(String currentPath, String pathArgument) {
        // 인자가 없으면 현재 경로, 있으면 인자를 정규화된 경로로 변환
        String targetPath = pathArgument.isEmpty() ? currentPath : normalizePath(currentPath, pathArgument);

        // FAKE_FILESYSTEM은 디렉토리 경로를 키로 가지고, ls 결과를 값(문자열)으로 가지고 있음
        if (FAKE_FILESYSTEM.containsKey(targetPath)) {
            // 값이 파일 내용이면 (개행 문자가 없으면 파일로 가정)
            if (!FAKE_FILESYSTEM.get(targetPath).contains("\n")) {
                return "Error: " + targetPath + " is a file, not a directory.";
            }
            return "Contents of " + targetPath + ":\n" + FAKE_FILESYSTEM.get(targetPath);
        } else {
            return "Error: Directory or path not found: " + targetPath;
        }
    }

    // ⚡️ 수정: cat 명령 핸들러 (현재 경로 사용)
    private static String handleCatCommand(String currentPath, String pathArgument) {
        if (pathArgument.isBlank()) {
            return "Usage: cat <file_path>";
        }

        String targetPath = normalizePath(currentPath, pathArgument);

        // FAKE_FILESYSTEM에 파일 경로가 있는지 확인
        if (FAKE_FILESYSTEM.containsKey(targetPath)) {
            // 값이 디렉토리 내용이면 (개행 문자가 포함되어 있으면 디렉토리로 가정)
            if (FAKE_FILESYSTEM.get(targetPath).contains("\n")) {
                return "Error: " + targetPath + " is a directory.";
            }
            return FAKE_FILESYSTEM.get(targetPath);
        }

        return "Error: File not found or unreadable: " + pathArgument;
    }

    // ⚡️ 추가: cd 명령 핸들러
    private static String handleCdCommand(ServerPlayerEntity player, String currentPath, String pathArgument) {
        if (pathArgument.isBlank()) {
            // 인자가 없으면 루트 (/)로 이동
            setCurrentPath(player.getUuid(), "/");
            return ""; // 출력 없음
        }

        String newPath = normalizePath(currentPath, pathArgument);

        // 이동할 경로가 FAKE_FILESYSTEM에 존재하는지 확인
        if (FAKE_FILESYSTEM.containsKey(newPath)) {
            // 값이 디렉토리 내용인지 확인 (개행 문자가 포함되어 있으면 디렉토리로 가정)
            if (FAKE_FILESYSTEM.get(newPath).contains("\n")) {
                setCurrentPath(player.getUuid(), newPath);
                return ""; // 출력 없음
            } else {
                return "Error: Not a directory: " + pathArgument;
            }
        } else {
            return "Error: Directory not found: " + pathArgument;
        }
    }

    // --- 경로 처리 헬퍼 메서드 추가 ---

    // ⚡️ 추가: 경로를 정규화하여 절대 경로를 반환 (상대 경로, 절대 경로, .. 처리)
    private static String normalizePath(String currentPath, String pathArgument) {
        String newPath;

        // 1. 경로 병합
        if (pathArgument.startsWith("/")) {
            // 절대 경로
            newPath = pathArgument;
        } else if (pathArgument.equals("..")) {
            // 상위 디렉토리
            newPath = getParentPath(currentPath);
        } else {
            // 상대 경로
            newPath = currentPath + (currentPath.endsWith("/") ? "" : "/") + pathArgument;
        }

        // 2. 경로 정리 (여러 개의 연속된 슬래시 제거, /./ 제거)
        newPath = newPath.replaceAll("//+", "/"); // // -> /
        newPath = newPath.replaceAll("/\\./", "/"); // /./ -> /

        // 3. 상위 디렉토리(..) 처리 (단순한 정규화)
        while (newPath.contains("/../")) {
            // 'dir/../' 패턴을 제거 ('/dir/file'일 경우 'dir'까지 포함하여 처리)
            newPath = newPath.replaceFirst("/[^/]+/\\.\\./", "/");
        }

        // 4. 마지막 슬래시 제거 (루트 제외)
        if (newPath.length() > 1 && newPath.endsWith("/")) {
            newPath = newPath.substring(0, newPath.length() - 1);
        }

        // 5. 경로가 비어있으면 루트(/)로 처리
        if (newPath.isEmpty()) {
            newPath = "/";
        }

        return newPath;
    }

    // ⚡️ 추가: 상위 디렉토리 경로를 반환 (e.g., /logs/archive -> /logs, /logs -> /)
    private static String getParentPath(String path) {
        if (path.equals("/")) {
            return "/";
        }

        // 마지막 슬래시가 없는 상태에서 마지막 / 위치를 찾음
        int lastSlash = path.lastIndexOf('/');

        if (lastSlash <= 0) {
            return "/"; // 루트의 파일이나 루트 자신인 경우
        }

        // 마지막 /까지 자름
        String parent = path.substring(0, lastSlash);
        if (parent.isEmpty()) return "/";
        return parent;
    }


    // --- 기존 명령 핸들러 (변경 없음) ---

    private static String handleDecryptCommand(String argument) {
        // ... (내용 생략)
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
        // ... (내용 생략)
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
        // ... (내용 생략)
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

    // ⚡️ 수정: 플레이어 상태를 저장하는 내부 클래스
    private static class PlayerState {
        private boolean isHackerActive;
        private long activationTime;
        // ⚡️ 추가: 플레이어별 현재 경로
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

        // ⚡️ 현재 경로 게터/세터 추가
        public String getCurrentPath() {
            return currentPath;
        }

        public void setCurrentPath(String currentPath) {
            this.currentPath = currentPath;
        }
    }
}