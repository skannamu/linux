package com.skannamu;

import com.skannamu.client.ClientExploitManager;
import com.skannamu.client.gui.TerminalScreen;
import com.skannamu.gecko.renderer.NanoBladeRenderer; // 3D 렌더러 임포트
import com.skannamu.init.ModItems; // 아이템 등록 클래스 임포트
import com.skannamu.network.ExploitSequencePayload;
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
import software.bernie.geckolib.renderer.GeoItemRenderer; // GeckoLib 렌더러 등록 임포트

public class skannamuModClient implements ClientModInitializer {

    public static boolean isPlayerActive = false;

    @Override
    public void onInitializeClient() {
        com.skannamu.skannamuMod.LOGGER.info("skannamuMod Client initialized!");

        GeoItemRenderer.registerItemRenderer(
                ModItems.NANO_BLADE,
                new NanoBladeRenderer()
        );

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

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (stack.getItem() instanceof standardBlockToolTip) {
                lines.add(Text.literal("Standard Block Tooltip"));
            }
        });
    }
}
