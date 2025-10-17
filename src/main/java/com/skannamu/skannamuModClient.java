package com.skannamu;

import com.skannamu.client.ClientExploitManager;
import com.skannamu.client.ClientHackingState;
import com.skannamu.client.gui.TerminalScreen;
import com.skannamu.network.*;
import com.skannamu.tooltip.standardBlockToolTip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class skannamuModClient implements ClientModInitializer {

    public static boolean isPlayerActive = false;

    @Override
    public void onInitializeClient() {
        skannamuMod.LOGGER.info("skannamuMod Client initialized!");

        // ModuleActivationPayload 등록 제거 (서버에서 처리)
        // PayloadTypeRegistry.playC2S().register(ModuleActivationPayload.ID, ModuleActivationPayload.CODEC); // 주석 처리 또는 삭제

        ClientPlayNetworking.registerGlobalReceiver(ExploitSequencePayload.ID, (payload, context) -> {
            MinecraftClient client = context.client();
            if (client != null && client.player != null) {
                client.execute(() -> ClientExploitManager.startSequence(payload.targetUuid(), payload.durationInTicks()));
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                if (ClientExploitManager.isWaitingForTrigger()) {
                    boolean isPressed = client.options.attackKey.isPressed();
                    ClientExploitManager.setLmbDown(isPressed);
                }
                ClientExploitManager.clientTick(client);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(TerminalOutputPayload.ID, (payload, context) -> {
            MinecraftClient client = context.client();
            if (client != null) {
                String output = payload.output();
                client.execute(() -> {
                    Screen currentScreen = client.currentScreen;
                    if (currentScreen instanceof TerminalScreen terminalScreen) {
                        terminalScreen.appendOutput(output);
                    }
                    if (output.contains("Key accepted")) {
                        isPlayerActive = true;
                    }
                });
            }
        });

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (stack.getItem() instanceof standardBlockToolTip) {
                lines.add(Text.literal("Standard Block Tooltip"));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(HackedStatusPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientHackingState.setHacked(payload.isHacked());
            });
        });
    }
}