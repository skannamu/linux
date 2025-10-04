// src/main/java/com/skannamu/skannamuModClient.java

package com.skannamu;

import com.skannamu.client.gui.TerminalScreen;
import com.skannamu.client.gui.UrlInputScreen; // ⚡️ UrlInputScreen Import
import com.skannamu.network.TerminalOutputPayload;
import com.skannamu.network.UrlScreenOpenPayload; // ⚡️ 신규 패킷 Import
import com.skannamu.tooltip.standardBlockToolTip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;


public class skannamuModClient implements ClientModInitializer {

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
                    });
                });

        // ⚡️ 2. URL 입력창 열기 명령 패킷 수신 리스너 (StandardBlock 동작의 핵심)
        ClientPlayNetworking.registerGlobalReceiver(UrlScreenOpenPayload.ID,
                (UrlScreenOpenPayload payload, ClientPlayNetworking.Context context) -> {
                    context.client().execute(() -> {
                        // 서버에서 권한을 확인하고 보낸 명령에 따라 UI를 엽니다.
                        context.client().setScreen(new UrlInputScreen());
                    });
                });


        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (stack.getItem() instanceof standardBlockToolTip) {
                // 툴팁 로직 (생략)
            }
        });
    }
}