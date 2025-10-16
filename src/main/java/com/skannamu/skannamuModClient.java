package com.skannamu;

import com.skannamu.client.ClientExploitManager;
import com.skannamu.client.gui.TerminalScreen;
import com.skannamu.network.ExploitSequencePayload;
import com.skannamu.network.ExploitTriggerPayload; // C2S 패킷이지만 클라이언트에서 전송할 때 필요
import com.skannamu.network.TerminalCommandPayload; // C2S 패킷이지만 클라이언트에서 전송할 때 필요
import com.skannamu.network.TerminalOutputPayload;
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
        com.skannamu.skannamuMod.LOGGER.info("skannamuMod Client initialized!");

        // 🚨 C2S 패킷 등록 로직을 skannamuMod.java로 옮김 (중복 등록 크래시 방지)

        // S2C ExploitSequencePayload 수신 핸들러 등록
        ClientPlayNetworking.registerGlobalReceiver(ExploitSequencePayload.ID,
                (ExploitSequencePayload payload, ClientPlayNetworking.Context context) -> {
                    MinecraftClient client = context.client();
                    if (client != null && client.player != null) {
                        client.execute(() -> ClientExploitManager.startSequence(payload.targetUuid(), payload.durationInTicks()));
                    }
                });

        // 클라이언트 틱 이벤트 핸들러 등록
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                if (ClientExploitManager.isWaitingForTrigger()) {
                    // 공격 키(좌클릭) 상태 확인
                    boolean isPressed = client.options.attackKey.isPressed();
                    ClientExploitManager.setLmbDown(isPressed);
                }
                // Exploit Manager 틱 로직 실행
                ClientExploitManager.clientTick(client);
            }
        });

        // S2C TerminalOutputPayload 수신 핸들러 등록
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

        // 툴팁 이벤트 핸들러 등록
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (stack.getItem() instanceof standardBlockToolTip) {
                lines.add(Text.literal("Standard Block Tooltip"));
            }
        });
    }
}