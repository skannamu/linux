package com.skannamu;

import com.skannamu.client.ClientExploitManager;
import com.skannamu.client.ClientHackingState;
import com.skannamu.client.ClientShaderManager;
import com.skannamu.client.gui.TerminalScreen;
import com.skannamu.network.*;
import com.skannamu.tooltip.standardBlockToolTip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public class skannamuModClient implements ClientModInitializer {

    public static boolean isPlayerActive = false;
    private static final Identifier SHADER_RELOADER_ID = Identifier.of(skannamuMod.MOD_ID, "exploit_shader_reloader");

    @Override
    public void onInitializeClient() {
        skannamuMod.LOGGER.info("skannamuMod Client initialized!");

        // 1. 셰이더 초기화 리스너 등록 (CLIENT_STARTED 제거. 초기화는 리소스 리로더가 담당)
        // ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
        //     skannamuMod.LOGGER.info("Client fully started. Initializing ClientShaderManager.");
        //     ClientShaderManager.initShaders(client);
        // });

        // 2. 리소스 리로드 리스너 등록 (F3+T, 초기 리소스 로드 완료 후 초기화 모두 담당)
        // 리소스 리로더 등록은 게임 시작 시 초기 리소스 로드가 완료된 후 자동으로 실행됩니다.
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new ExploitShaderReloader());

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

        // 셰이더 정리 (종료 시)
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ClientShaderManager.close();
        });
    }

    /**
     * IdentifiableResourceReloadListener와 ResourceReloader를 구현하는 전용 클래스입니다.
     * 이 리로더는 게임 시작 시 (초기 리소스 로드 완료 후) 그리고 F3+T 리로드 시 모두 셰이더 초기화를 담당합니다.
     */
    private static class ExploitShaderReloader implements IdentifiableResourceReloadListener, ResourceReloader {

        @Override
        public Identifier getFabricId() {
            return SHADER_RELOADER_ID;
        }

        /**
         * 리소스 리로드 로직을 처리합니다.
         * 이 메서드는 게임이 시작될 때 (초기 리소스 로드 후)와 리소스가 다시 로드될 때 호출됩니다.
         */
        @Override
        public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
            // 리소스 준비 작업이 끝난 후 (whenPrepared), GL 호출을 메인 스레드에 보장하는 applyExecutor를 사용하여 셰이더를 다시 초기화합니다.
            return synchronizer.whenPrepared(null).thenRunAsync(() -> {
                skannamuMod.LOGGER.info("Client resources fully loaded (via ExploitShaderReloader). Initializing ClientShaderManager.");
                MinecraftClient client = MinecraftClient.getInstance();
                // GL 호출은 메인 스레드에서 이루어져야 하므로 execute()를 사용합니다.
                client.execute(() -> ClientShaderManager.initShaders(client));

                // 초기 로드 성공 메시지를 채팅창에 추가하여 확인을 용이하게 합니다.
                if (client.inGameHud != null) {
                    client.inGameHud.getChatHud().addMessage(Text.literal("§a[Skannamu] Initial Shaders loaded successfully."));
                }
            }, applyExecutor);
        }
    }
}
