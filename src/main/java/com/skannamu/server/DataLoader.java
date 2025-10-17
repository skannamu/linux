package com.skannamu.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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


public class DataLoader implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID = Identifier.of(skannamuMod.MOD_ID, "data_loader");
    private static final Identifier MISSION_DATA_ID = Identifier.of(skannamuMod.MOD_ID, "mission_data.json");

    public static final DataLoader INSTANCE = new DataLoader();

    private @Nullable JsonElement loadedMissionData = null;

    public static void registerDataLoaders() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(INSTANCE);
        skannamuMod.LOGGER.info("Registered Data Loader for server data.");
    }

    // ⚡️ 오류 수정: getFabricId() 구현 추가
    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        this.loadedMissionData = loadMissionData(manager);
        skannamuMod.LOGGER.info("Successfully loaded mission data.");
    }

    private JsonElement loadMissionData(ResourceManager manager) {
        // manager.findResources()는 이제 Map<Identifier, Resource>를 반환합니다.
        Collection<Identifier> resourceIds = manager.findResources(MISSION_DATA_ID.getPath(), path -> true)
                .keySet();

        if (resourceIds.isEmpty()) {
            skannamuMod.LOGGER.warn("Could not find mission data file: {}", MISSION_DATA_ID);
            return null;
        }

        Identifier resourceId = resourceIds.iterator().next();

        try {
            Resource resource = manager.getResource(resourceId).orElseThrow(
                    () -> new IOException("Resource not found for ID: " + resourceId)
            );

            // Resource.getInputStream()은 AutoCloseable이므로 try-with-resources 사용 가능
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    resource.getInputStream(),
                    StandardCharsets.UTF_8
            ))) {
                Gson gson = new Gson();
                return gson.fromJson(reader, JsonElement.class);

            } catch (IOException e) {
                skannamuMod.LOGGER.error("Failed to read mission data from resource: {}", resourceId, e);
            }
        } catch (IOException e) {
            skannamuMod.LOGGER.error("Failed to get resource for mission data: {}", resourceId, e);
        }

        return new Gson().fromJson("{}", JsonElement.class);
    }
    public @Nullable JsonElement getMissionData() {
        return this.loadedMissionData;
    }
}