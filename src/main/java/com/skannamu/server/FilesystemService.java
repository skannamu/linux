package com.skannamu.server;

import com.skannamu.skannamuMod;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class FilesystemService {

    private final MissionData missionData;
    private final DatabaseService databaseService;
    private final Map<String, String> globalFiles;
    private final Map<String, String> globalDirectories;

    public FilesystemService(MissionData data, DatabaseService dbService) {
        this.missionData = data;
        this.databaseService = dbService;

        // MissionData.filesystem이 null일 경우 안전하게 빈 맵으로 초기화
        if (data != null && data.filesystem != null) {
            this.globalFiles = data.filesystem.files;
            this.globalDirectories = data.filesystem.directories;
        } else {
            // 데이터 팩 로드 전 안전한 기본값으로 초기화 (크래시 방지)
            this.globalFiles = Map.of();
            this.globalDirectories = Map.of();
        }
    }

    public String checkPathType(UUID playerUuid, String normalizedPath) {
        // 1. 플레이어 소유 DB 항목 확인
        String type = getDbType(playerUuid, normalizedPath);
        if (!type.equals("not found")) {
            return type; // 'file' 또는 'dir' (개인 파일)
        }

        // 2. 💡 수정: 글로벌 DB 항목 확인 (ex: /home)
        // Global UUID (0000...)를 사용하여 DB를 조회합니다.
        UUID globalUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        String globalDbType = getDbType(globalUuid, normalizedPath);
        if (!globalDbType.equals("not found")) {
            return globalDbType; // 'file' 또는 'dir' (글로벌 DB 항목)
        }

        // 3. MissionData의 Global Map 확인 (JSON 파일로 로드된 시스템 경로)
        if (globalDirectories.containsKey(normalizedPath)) {
            return "dir";
        }
        if (globalFiles.containsKey(normalizedPath)) {
            return "file";
        }

        return "not found";
    }

    private String getDbType(UUID playerUuid, String normalizedPath) {
        boolean isGlobalCheck = playerUuid.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        String sql = "SELECT type FROM filesystem WHERE path = ?";
        if (!isGlobalCheck) {
            sql += " AND player_uuid = ?";
        }

        try (Connection conn = databaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, normalizedPath);
            if (!isGlobalCheck) {
                pstmt.setString(2, playerUuid.toString());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("type");
                }
            }
        } catch (SQLException e) {
            skannamuMod.LOGGER.error("DB Error checking path type: {}", normalizedPath, e);
        }
        return "not found";
    }

    public String getEntryContent(UUID playerUuid, String normalizedPath) {
        String type = checkPathType(playerUuid, normalizedPath);

        if (type.equals("dir")) {
            return "Error: Cannot cat a directory: " + normalizedPath;
        }

        if (type.equals("file")) {
            // DB 항목 조회 시 플레이어 UUID나 글로벌 UUID를 사용해야 함.
            UUID queryUuid = UUID.fromString("00000000-0000-0000-0000-000000000000"); // 기본값으로 글로벌 UUID 설정

            // 만약 플레이어 소유라면 플레이어 UUID를 사용
            String playerDbType = getDbType(playerUuid, normalizedPath);
            if (!playerDbType.equals("not found")) {
                queryUuid = playerUuid;
            }
            // 💡 주의: 이 로직은 MissionData나 글로벌 DB에 있는 파일을 찾지 못할 수 있습니다.
            // checkPathType이 이미 파일 타입을 알려주었으므로, 이 시점에서 DB를 한 번 더 쿼리하는 것은 비효율적입니다.
            // 하지만 현재 구조상 content는 DB에 있거나 (player or global) MissionData에 있습니다.

            // DB에 있다면 가져옵니다.
            String sql = "SELECT content FROM filesystem WHERE player_uuid = ? AND path = ? AND type = 'file'";
            try (Connection conn = databaseService.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, queryUuid.toString());
                pstmt.setString(2, normalizedPath);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("content");
                    }
                }
            } catch (SQLException e) {
                skannamuMod.LOGGER.error("DB Error reading file content: {}", normalizedPath, e);
                return "Internal Error: Could not read file content.";
            }
        }

        // MissionData의 글로벌 파일을 확인합니다.
        if (globalFiles.containsKey(normalizedPath)) {
            return globalFiles.get(normalizedPath);
        }

        return "Error: File not found or unreadable: " + normalizedPath;
    }


    public String getDirectoryContents(UUID playerUuid, String normalizedPath) {

        if (!checkPathType(playerUuid, normalizedPath).equals("dir")) {
            return "Error: '" + normalizedPath + "' is not a directory.";
        }

        String pathPrefix = normalizedPath.equals("/") ? "/" : normalizedPath + "/";

        // 플레이어 소유 파일 목록 조회
        String sql = "SELECT path, type FROM filesystem WHERE player_uuid = ? AND path LIKE ? AND path != ?";

        Set<String> contents = new HashSet<>();
        try (Connection conn = databaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, pathPrefix + "%");
            pstmt.setString(3, normalizedPath);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String fullPath = rs.getString("path");
                    String entryName = fullPath.substring(pathPrefix.length());
                    // 현재 디렉토리 바로 아래 엔트리만 추출
                    if (!entryName.contains("/")) {
                        contents.add(entryName);
                    }
                }
            }
        } catch (SQLException e) {
            skannamuMod.LOGGER.error("DB Error listing directory contents: {}", normalizedPath, e);
        }

        // 💡 글로벌 DB의 파일 목록 조회 (MissionData에 없는 /home 등 시스템 디렉토리의 내부 항목)
        UUID globalUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        String globalSql = "SELECT path, type FROM filesystem WHERE player_uuid = ? AND path LIKE ? AND path != ?";
        try (Connection conn = databaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(globalSql)) {

            pstmt.setString(1, globalUuid.toString());
            pstmt.setString(2, pathPrefix + "%");
            pstmt.setString(3, normalizedPath);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String fullPath = rs.getString("path");
                    String entryName = fullPath.substring(pathPrefix.length());
                    if (!entryName.contains("/")) {
                        if (!contents.contains(entryName)) { // 개인 파일과 이름이 중복되지 않는 경우만 추가
                            contents.add(entryName);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            skannamuMod.LOGGER.error("DB Error listing global directory contents: {}", normalizedPath, e);
        }


        // MissionData에 정의된 글로벌 파일 및 디렉토리 확인
        // 이 부분은 MissionData의 `directories` 맵의 content 필드가 하위 목록을 나타내는 경우에 유효합니다.
        // 현재는 directories 맵의 키가 디렉토리 경로이므로, 하위 항목을 직접 조회해야 합니다.
        // 현재 로직은 MissionData.directories의 value를 목록 문자열로 가정하고 있으므로 그대로 유지합니다.
        if (globalDirectories.containsKey(normalizedPath)) {
            String globalContent = globalDirectories.get(normalizedPath);
            if (globalContent != null && !globalContent.isBlank()) {
                for(String item : globalContent.split("[\\s\\n]+")) {
                    if (!item.isBlank()) {
                        // 개인 파일에 동일한 이름이 없으면 추가
                        String fullGlobalPath = TerminalCommands.normalizePath(pathPrefix + item);
                        if(getDbType(playerUuid, fullGlobalPath).equals("not found")) {
                            contents.add(item);
                        }
                    }
                }
            }
        }

        // MissionData의 글로벌 파일 중 해당 디렉토리의 하위 파일/디렉토리를 직접 찾습니다.
        // MissionData는 현재 `path:content` 형태이므로, 하위 항목을 명시적으로 찾아야 합니다.
        // 이 부분은 MissionData의 구조에 따라 다르지만, 파일/디렉토리 키를 순회하여 하위 항목을 찾는 것이 정확합니다.

        // MissionData 내에서 하위 디렉토리 찾기
        for (String globalDir : globalDirectories.keySet()) {
            if (globalDir.startsWith(pathPrefix) && !globalDir.equals(normalizedPath)) {
                String entryName = globalDir.substring(pathPrefix.length());
                if (!entryName.contains("/")) { // 바로 아래 자식만
                    if (!contents.contains(entryName)) {
                        contents.add(entryName);
                    }
                }
            }
        }

        // MissionData 내에서 하위 파일 찾기
        for (String globalFile : globalFiles.keySet()) {
            if (globalFile.startsWith(pathPrefix) && !globalFile.equals(normalizedPath)) {
                String entryName = globalFile.substring(pathPrefix.length());
                if (!entryName.contains("/")) { // 바로 아래 자식만
                    if (!contents.contains(entryName)) {
                        contents.add(entryName);
                    }
                }
            }
        }

        return String.join("\n", contents);
    }

    public String redirectOutput(UUID playerUuid, String currentDir, String targetPath, String content, String operator) {

        String absolutePath = TerminalCommands.normalizePath(resolvePath(currentDir, targetPath));

        if (absolutePath.isEmpty() || absolutePath.endsWith("/")) {
            return "Error: Cannot redirect output to a directory: " + targetPath;
        }

        String parentDir = getParentDirectory(absolutePath);

        // 💡 상위 디렉토리가 존재하지 않는지 확인합니다.
        // /home/user/file.txt -> parentDir = /home/user.
        // /home/user가 디렉토리가 아니라면 오류가 발생합니다.
        if (!checkPathType(playerUuid, parentDir).equals("dir")) {
            return "Error: Parent directory does not exist: " + parentDir;
        }

        if (globalFiles.containsKey(absolutePath) || globalDirectories.containsKey(absolutePath)) {
            return "Error: Cannot write to a read-only system file: " + absolutePath;
        }

        String currentContent = getEntryContent(playerUuid, absolutePath);

        if (currentContent.startsWith("Error: Cannot cat a directory:")) {
            return "Error: Cannot redirect output to a directory: " + targetPath;
        }

        boolean fileExists = !currentContent.startsWith("Error:");

        String newContent;
        if (operator.equals(">")) {
            newContent = content;
        } else if (operator.equals(">>")) {
            newContent = fileExists && !currentContent.isEmpty()
                    ? currentContent + "\n" + content
                    : content;
        } else {
            return "Internal Error: Invalid operator " + operator;
        }

        // 💡 생성/덮어쓰기/추가는 모두 플레이어 소유로 저장합니다.
        String sql = "INSERT OR REPLACE INTO filesystem (player_uuid, path, type, content) VALUES (?, ?, 'file', ?)";
        try (Connection conn = databaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, absolutePath);
            pstmt.setString(3, newContent);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            skannamuMod.LOGGER.error("DB Error writing file content: {}", absolutePath, e);
            return "Internal Error: Failed to write to file.";
        }

        return "";
    }

    public String createDirectory(UUID playerUuid, String normalizedPath) {
        if (normalizedPath.equals("/")) {
            return "Error: Cannot create root directory.";
        }

        String existingType = checkPathType(playerUuid, normalizedPath);
        if (existingType.equals("dir")) {
            return "Error: Directory already exists: " + normalizedPath;
        }
        if (existingType.equals("file")) {
            return "Error: A file with the same name already exists: " + normalizedPath;
        }

        String parentDir = getParentDirectory(normalizedPath);
        // 💡 상위 디렉토리가 존재하며 디렉토리 타입인지 확인합니다. (checkPathType이 글로벌 경로까지 확인하므로 안전합니다.)
        if (!checkPathType(playerUuid, parentDir).equals("dir")) {
            return "Error: Parent directory does not exist or is not a directory: " + parentDir;
        }

        String sql = "INSERT INTO filesystem (player_uuid, path, type, content) VALUES (?, ?, 'dir', NULL)";
        try (Connection conn = databaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, normalizedPath);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            skannamuMod.LOGGER.error("DB Error creating directory: {}", normalizedPath, e);
            if (e.getErrorCode() == 19) {
                return "Error: Entry already exists: " + normalizedPath;
            }
            return "Internal Error: Failed to create directory.";
        }
        return ""; // 성공
    }

    public String deleteEntry(UUID playerUuid, String normalizedPath) {
        String type = checkPathType(playerUuid, normalizedPath);

        if (type.equals("not found")) {
            return "Error: File or directory not found: " + normalizedPath;
        }

        // 글로벌 DB 또는 MissionData 경로인지 확인하여 삭제를 방지합니다.
        UUID globalUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if (!getDbType(globalUuid, normalizedPath).equals("not found") ||
                globalFiles.containsKey(normalizedPath) ||
                globalDirectories.containsKey(normalizedPath)) {
            return "Error: Cannot delete system path: " + normalizedPath;
        }

        if (type.equals("dir")) {
            String contents = getDirectoryContents(playerUuid, normalizedPath);
            if (!contents.isBlank()) {
                return "Error: Directory not empty.";
            }
        }

        String sql = "DELETE FROM filesystem WHERE player_uuid = ? AND path = ?";
        try (Connection conn = databaseService.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, normalizedPath);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            skannamuMod.LOGGER.error("DB Error deleting entry: {}", normalizedPath, e);
            return "Internal Error: Failed to delete entry.";
        }
        return ""; // 성공
    }

    public void initializeGlobalPaths() {
        UUID globalUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        if (getDbType(globalUuid, "/home").equals("not found")) {
            String sql = "INSERT INTO filesystem (player_uuid, path, type, content) VALUES (?, ?, 'dir', NULL)";
            try (Connection conn = databaseService.connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, globalUuid.toString());
                pstmt.setString(2, "/home");
                pstmt.executeUpdate();

            } catch (SQLException e) {
                skannamuMod.LOGGER.error("Failed to initialize /home directory in DB:", e);
            }
        }
    }

    private String resolvePath(String currentDir, String targetPath) {
        if (targetPath.startsWith("/")) {
            return targetPath.replaceAll("/+", "/");
        } else {
            String resolved = currentDir + (currentDir.endsWith("/") ? "" : "/") + targetPath;
            return resolved.replaceAll("/+", "/");
        }
    }

    private String getParentDirectory(String absolutePath) {
        if (absolutePath.equals("/")) return "/";
        int lastSlash = absolutePath.lastIndexOf('/');
        if (lastSlash == 0) return "/";
        return absolutePath.substring(0, lastSlash);
    }
}