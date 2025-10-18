package com.skannamu.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class FilesystemService {

    private final MissionData missionData;

    public FilesystemService(MissionData data) {
        this.missionData = data;
    }

    public String redirectOutput(String currentDir, String targetPath, String content, String operator) {

        String absolutePath = resolvePath(currentDir, targetPath);

        if (absolutePath.isEmpty() || absolutePath.endsWith("/")) {
            return "Error: Cannot redirect output to a directory: " + targetPath;
        }

        String parentDir = getParentDirectory(absolutePath);
        if (!missionData.filesystem.directories.containsKey(parentDir)) {
            return "Error: Parent directory does not exist: " + parentDir;
        }

        Map<String, String> files = missionData.filesystem.files;
        Map<String, String> directories = missionData.filesystem.directories;

        String currentContent = files.getOrDefault(absolutePath, "");
        String newContent;

        if (operator.equals(">")) {
            newContent = content;
        } else if (operator.equals(">>")) {
            newContent = currentContent.isEmpty()
                    ? content
                    : currentContent + "\n" + content;
        } else {
            return "Internal Error: Invalid operator " + operator;
        }

        files.put(absolutePath, newContent);

        if (!currentContent.isEmpty()) {
        } else {
            String fileName = getFileName(absolutePath);
            String dirContent = directories.get(parentDir);
            directories.put(parentDir, dirContent.isEmpty() ? fileName : dirContent + "\n" + fileName);
        }


        return ""; // 파일 I/O 성공 시에는 터미널에 아무것도 출력하지 않음
    }

    private String resolvePath(String currentDir, String targetPath) {

        if (targetPath.startsWith("/")) {
            return targetPath.replaceAll("/+", "/"); // // -> / 로 정규화
        } else {
            String resolved = currentDir + (currentDir.endsWith("/") ? "" : "/") + targetPath;
            return resolved.replaceAll("/+", "/");
        }
    }

    private String getParentDirectory(String absolutePath) {
        if (absolutePath.equals("/")) return null; // Root has no parent
        int lastSlash = absolutePath.lastIndexOf('/');
        if (lastSlash == 0) return "/"; // /file.txt 의 부모는 /
        return absolutePath.substring(0, lastSlash);
    }

    private String getFileName(String absolutePath) {
        int lastSlash = absolutePath.lastIndexOf('/');
        return absolutePath.substring(lastSlash + 1);
    }
}