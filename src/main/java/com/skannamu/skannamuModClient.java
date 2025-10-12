package com.skannamu;

import com.skannamu.client.ClientExploitManager;
import com.skannamu.client.gui.TerminalScreen;
import com.skannamu.init.ModItems;
import com.skannamu.network.ExploitSequencePayload;
import com.skannamu.network.TerminalOutputPayload;
import com.skannamu.tooltip.standardBlockToolTip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientResourceReloadEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class skannamuModClient implements ClientModInitializer {

    public static boolean isPlayerActive = false;

    @Override
    public void onInitializeClient() {
        com.skannamu.skannamuMod.LOGGER.info("skannamuMod Client initialized!");


        PayloadTypeRegistry.playS2C().register(TerminalOutputPayload.ID, TerminalOutputPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ExploitSequencePayload.ID, ExploitSequencePayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ExploitSequencePayload.ID,
                (ExploitSequencePayload payload, ClientPlayNetworking.Context context) -> {
                    context.client().execute(()-> {
                        ClientPlayerEntity player = context.player();
                        if (player != null){
                            ClientExploitManager.startSequence(payload.targetUuid(), payload.durationInTicks());
                        }
                    });
                }
        );
        ClientExploitManager.loadShader(MinecraftClient.getInstance());
        ClientResourceReloadEvents.END.register((resourceManager, packManager) -> {
            ClientExploitManager.loadShader(MinecraftClient.getInstance());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                ClientExploitManager.clientTick(client);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(TerminalOutputPayload.ID,
                (TerminalOutputPayload payload, ClientPlayNetworking.Context context) -> {
                    String output = payload.output();
                    context.client().execute(() -> {
                        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
                        if (currentScreen instanceof TerminalScreen terminalScreen) {
                            terminalScreen.appendOutput(output);
                        }
                        if (output.contains("Key accepted")) {
                            isPlayerActive = true;
                        }
                    });
                });

        HudRenderCallback.EVENT.register(this::renderExploitOverlay);
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (stack.getItem() instanceof standardBlockToolTip) {
                lines.add(Text.literal("Standard Block Tooltip"));
            }
        });
    }

    private void renderExploitOverlay(DrawContext context, float tickDelta) {
        if (!ClientExploitManager.isExploitActive()) {
            return;
        }

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        float fadeAlpha = ClientExploitManager.getFadeAlpha();
        if (fadeAlpha > 0.0f) {
            int color = ((int)(fadeAlpha * 255.0f) << 24) | 0x000000;
            context.fill(0, 0, screenWidth, screenHeight, color);
        }
    }
}