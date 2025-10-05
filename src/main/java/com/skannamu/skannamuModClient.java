// src/main/java/com/skannamu/skannamuModClient.java

package com.skannamu;

import com.skannamu.client.gui.TerminalScreen;
import com.skannamu.network.TerminalOutputPayload;
import com.skannamu.tooltip.standardBlockToolTip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;


public class skannamuModClient implements ClientModInitializer {

    public static boolean isPlayerActive = false;

    @Override
    public void onInitializeClient() {
        skannamuMod.LOGGER.info("skannamuMod Client initialized!");

        // 1. 터미널 출력 패킷 수신 리스너 (기존 로직 유지)
        ClientPlayNetworking.registerGlobalReceiver(TerminalOutputPayload.ID,
                (TerminalOutputPayload payload, ClientPlayNetworking.Context context) -> {

                    String output = payload.output();
                    context.client().execute(() -> {
                        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
                        if (currentScreen instanceof TerminalScreen terminalScreen) {
                            terminalScreen.appendOutput(output);
                        }
                        // 활성화 성공 메시지 확인 후 클라이언트 로컬 상태 업데이트
                        if (output.contains("Key accepted")) {
                            isPlayerActive = true;
                        }
                    });
                });

        // UrlScreenOpenPayload 리스너 제거 (더 이상 사용하지 않음)

        // 툴팁 리스너 (기존 로직 유지)
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (stack.getItem() instanceof standardBlockToolTip) {
                // 툴팁 로직 (생략)
            }
        });
    }
}