package com.skannamu.server;

import com.skannamu.item.block.VaultBlockEntity;
import com.skannamu.network.VaultSliderPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class ServerPacketHandler {

    public static void registerPayloads() {
        PayloadTypeRegistry.playC2S().register(VaultSliderPayload.SLIDER_UPDATE_ID, VaultSliderPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(VaultSliderPayload.SLIDER_SUBMIT_ID, VaultSliderPayload.CODEC);
    }

    public static void registerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(VaultSliderPayload.SLIDER_UPDATE_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            World world = player.getWorld();

            if (world.getBlockEntity(payload.pos()) instanceof VaultBlockEntity entity) {
                entity.updateSliderValue(payload.sliderIndex(), payload.value());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(VaultSliderPayload.SLIDER_SUBMIT_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            World world = player.getWorld();

            if (world.getBlockEntity(payload.pos()) instanceof VaultBlockEntity entity) {
                entity.checkAndOpenVault(player);
            }
        });
    }
}