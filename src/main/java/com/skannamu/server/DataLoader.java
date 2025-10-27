package com.skannamu.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
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
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;


public class DataLoader implements SimpleSynchronousResourceReloadListener {

    private static final Identifier ID = Identifier.of(skannamuMod.MOD_ID, "data_loader");
    private static final Identifier MISSION_DATA_ID = Identifier.of(skannamuMod.MOD_ID, "mission_data.json");

    public static final DataLoader INSTANCE = new DataLoader();
    private @Nullable MissionData loadedMissionDataInstance = null;

    public static void registerDataLoaders() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(INSTANCE);
        skannamuMod.LOGGER.info("Registered Data Loader for server data.");
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        JsonElement jsonElement = loadMissionDataJson(manager);
        if (jsonElement != null && !jsonElement.isJsonNull()) {
            Gson gson = new Gson();
            Type missionDataType = new TypeToken<MissionData>(){}.getType();
            try {
                this.loadedMissionDataInstance = gson.fromJson(jsonElement, missionDataType);
                skannamuMod.LOGGER.info("Successfully parsed MissionData object.");


            } catch (Exception e) {
                skannamuMod.LOGGER.error("Failed to deserialize MissionData:", e);
                this.loadedMissionDataInstance = null;
            }
        } else {
            this.loadedMissionDataInstance = null;
        }

        skannamuMod.LOGGER.info("Completed mission data loading and terminal initialization.");
    }

    private JsonElement loadMissionDataJson(ResourceManager manager) {
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

        return null;
    }

    public @Nullable MissionData getMissionDataInstance() {
        return this.loadedMissionDataInstance;
    }

    public @Nullable MissionData.VaultSettings getVaultSettings() {
        if (this.loadedMissionDataInstance != null && this.loadedMissionDataInstance.vault_settings != null) {
            return this.loadedMissionDataInstance.vault_settings;
        }
        return null;
    }
}