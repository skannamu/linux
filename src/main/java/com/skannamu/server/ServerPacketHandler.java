package com.skannamu.server;

import com.skannamu.item.block.VaultBlockEntity;
import com.skannamu.network.VaultSliderPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.math.BlockPos;

public class ServerPacketHandler {

    public static void registerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(VaultSliderPayload.SLIDER_UPDATE_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            int sliderIndex = buf.readInt();
            int newValue = buf.readInt();

            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof VaultBlockEntity entity) {
                    entity.updateSliderValue(sliderIndex, newValue);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(VaultSliderPayload.SLIDER_SUBMIT_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();

            server.execute(() -> {
                if (player.getWorld().getBlockEntity(pos) instanceof VaultBlockEntity entity) {
                    entity.checkAndOpenVault();
                }
            });
        });
    }
}