package com.skannamu;

import com.skannamu.client.gui.TerminalScreen;
import com.skannamu.network.TerminalOutputPayload;
import com.skannamu.tooltip.standardBlockToolTip;
import net.fabricmc.api.ClientModInitializer;
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

        PayloadTypeRegistry.playS2C().register(TerminalOutputPayload.ID, TerminalOutputPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(TerminalOutputPayload.ID,
                (TerminalOutputPayload payload, ClientPlayNetworking.Context context) -> {
                    String output = payload.output();
                    context.client().execute(() -> {
                        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
                        if (currentScreen instanceof TerminalScreen terminalScreen) {
                            terminalScreen.appendOutput(output); // 서버 응답 추가
                            // **수정:** 다음 프롬프트를 수동으로 추가하는 로직을 제거했습니다.
                            // 프롬프트는 이제 TerminalScreen.handleCommand 및 appendOutput에서 자동으로 관리됩니다.
                        }
                        // 활성화 성공 메시지 확인 후 클라이언트 로컬 상태 업데이트
                        if (output.contains("Key accepted")) {
                            isPlayerActive = true;
                        }
                    });
                });

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (stack.getItem() instanceof standardBlockToolTip) {
                lines.add(Text.literal("Standard Block Tooltip")); // 예시 툴팁
            }
        });
    }
}