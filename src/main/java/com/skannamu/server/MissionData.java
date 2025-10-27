package com.skannamu.server;

import java.util.HashMap;
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
        // Gson 로딩 전에도 Null이 아닌 빈 Map을 가지도록 초기화
        public Map<String, String> directories = new HashMap<>();
        public Map<String, String> files = new HashMap<>();
    }

    public static class VaultSettings {
        public List<Integer> correct_values;
        public int max_dial_value;
    }
}