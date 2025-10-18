package com.skannamu.server;

import java.util.List;
import java.util.Map;

public class MissionData {
    public TerminalSettings terminal_settings;
    public FilesystemData filesystem;
    public Map<String, List<Double>> teleport_locations;
    public VaultSettings vault_settings; // 💡 금고 설정 추가

    public static class TerminalSettings {
        public String activation_key;
    }

    public static class FilesystemData {
        public Map<String, String> directories;
        public Map<String, String> files;
    }

    public static class VaultSettings {
        public List<Integer> correct_values;
        public int max_dial_value;
    }
}