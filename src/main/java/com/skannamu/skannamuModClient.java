package com.skannamu;

import com.skannamu.client.ClientExploitManager;
import com.skannamu.client.gui.TerminalScreen;
import com.skannamu.network.ExploitSequencePayload;
import com.skannamu.network.ExploitTriggerPayload;
import com.skannamu.network.TerminalCommandPayload;
import com.skannamu.network.TerminalOutputPayload;
import com.skannamu.tooltip.standardBlockToolTip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class skannamuModClient implements ClientModInitializer {

    public static boolean isPlayerActive = false;

    @Override
    public void onInitializeClient() {
        com.skannamu.skannamuMod.LOGGER.info("skannamuMod Client initialized!");

        PayloadTypeRegistry.playC2S().register(TerminalCommandPayload.ID, TerminalCommandPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ExploitTriggerPayload.ID, ExploitTriggerPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ExploitSequencePayload.ID,
                (ExploitSequencePayload payload, ClientPlayNetworking.Context context) -> {
                    MinecraftClient client = context.client();
                    if (client != null && client.player != null) {
                        ClientExploitManager.startSequence(payload.targetUuid(), payload.durationInTicks());
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

        ClientPlayNetworking.registerGlobalReceiver(TerminalOutputPayload.ID,
                (TerminalOutputPayload payload, ClientPlayNetworking.Context context) -> {
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
    }
}