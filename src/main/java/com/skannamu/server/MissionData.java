package com.skannamu.server;

import java.util.List;
import java.util.Map;

public class MissionData {
    public TerminalSettings terminal_settings;
    public FilesystemData filesystem;
    public Map<String, List<Double>> teleport_locations;
    public VaultSettings vault_settings;
    public static class TerminalSettings {
        public String activation_key;
    }
    public static class FilesystemData {
        public Map<String, String> directories; // 디렉토리 경로 -> 포함된 파일/디렉토리 이름 목록
        public Map<String, String> files;       // 파일 경로 -> 파일 내용
    }
    public static class VaultSettings {
        public List<Integer> correct_sequence;
        public int max_dial_value;
    }
}