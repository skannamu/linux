// src/main/java/com/skannamu/server/DataLoader.java

package com.skannamu.server;

import com.google.gson.Gson;
import com.skannamu.skannamuMod;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DataLoader implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID = Identifier.of(skannamuMod.MOD_ID, "data_loader");
    // mission_data.json은 'data/skannamu/mission_data.json' 경로에 있어야 함
    private static final Identifier MISSION_DATA_ID = Identifier.of(skannamuMod.MOD_ID, "mission_data.json");

    public static final DataLoader INSTANCE = new DataLoader();

    // ⚡️ 변경: MissionData 객체로 저장
    private static @Nullable MissionData loadedMissionData = null;

    public static void registerDataLoaders() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(INSTANCE);
        skannamuMod.LOGGER.info("Registered Data Loader for server data.");
    }

    // ⚡️ 오류 수정: getFabricId() 구현
    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        // ⚡️ 변경: MissionData 객체를 로드하도록 수정
        loadedMissionData = loadMissionData(manager);
        if (loadedMissionData != null) {
            skannamuMod.LOGGER.info("Successfully loaded mission data.");
        } else {
            skannamuMod.LOGGER.error("Failed to load MissionData. Check logs for details.");
        }
    }

    private MissionData loadMissionData(ResourceManager manager) { // 반환 타입을 MissionData로 변경

        // ResourcePack에서 mission_data.json 파일을 찾음 (경로: data/skannamu/mission_data.json)
        Collection<Identifier> resourceIds = manager.findResources(MISSION_DATA_ID.getPath(), path -> true)
                .keySet();

        if (resourceIds.isEmpty()) {
            skannamuMod.LOGGER.warn("Could not find mission data file: {}", MISSION_DATA_ID);
            return null;
        }

        Identifier resourceId = resourceIds.iterator().next();

        try {
            Optional<Resource> resourceOptional = manager.getResource(resourceId);
            if (resourceOptional.isEmpty()) {
                throw new IOException("Resource not found for ID: " + resourceId);
            }
            Resource resource = resourceOptional.get();

            // Resource.getInputStream()은 AutoCloseable
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    resource.getInputStream(),
                    StandardCharsets.UTF_8
            ))) {
                Gson gson = new Gson();
                // ⚡️ 변경: MissionData 클래스를 사용하여 파싱
                return gson.fromJson(reader, MissionData.class);

            } catch (IOException e) {
                skannamuMod.LOGGER.error("Failed to read mission data from resource: {}", resourceId, e);
            }
        } catch (IOException e) {
            skannamuMod.LOGGER.error("Failed to get resource for mission data: {}", resourceId, e);
        }
        return null;
    }

    // ⚡️ 추가: ServerCommandProcessor가 사용할 통합 파일 시스템 Map을 제공하는 메서드
    public static Map<String, String> getFilesystemData() {
        if (loadedMissionData == null || loadedMissionData.filesystem == null) {
            skannamuMod.LOGGER.error("Filesystem data is not available. Using error message.");
            return Map.of("/", "Error: Filesystem data not loaded.");
        }

        // 디렉토리 구조와 파일 내용을 하나의 Map으로 통합
        Map<String, String> combinedFilesystem = new HashMap<>();

        if (loadedMissionData.filesystem.directories != null) {
            combinedFilesystem.putAll(loadedMissionData.filesystem.directories);
        }

        // 파일 내용
        if (loadedMissionData.filesystem.files != null) {
            combinedFilesystem.putAll(loadedMissionData.filesystem.files);
        }
        return combinedFilesystem;
    }

    // ⚡️ 추가: 활성화 키를 제공하는 메서드
    public static String getActivationKey() {
        if (loadedMissionData == null || loadedMissionData.terminal_settings == null || loadedMissionData.terminal_settings.activation_key == null) {
            skannamuMod.LOGGER.warn("Activation key not found in JSON. Using fallback key.");
            return "DEFAULT_FALLBACK_KEY";
        }
        return loadedMissionData.terminal_settings.activation_key;
    }
}