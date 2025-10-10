package com.skannamu.server;

import java.util.Map;

public class MissionData {
    public TerminalSettings terminal_settings;
    public FilesystemData filesystem;

    public static class TerminalSettings {
        public String activation_key;
    }
    // 파일 시스템 구조에 대응하는 클래스임
    public static class FilesystemData {
        public Map<String, String> directories;
        public Map<String, String> files;
    }
}