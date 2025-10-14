package com.skannamu;

import com.skannamu.client.ClientExploitManager;
import com.skannamu.client.gui.TerminalScreen;
import com.skannamu.network.ExploitSequencePayload;
import com.skannamu.network.TerminalOutputPayload;
import com.skannamu.tooltip.standardBlockToolTip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
// 셰이더 로직 제거에 따라 다음 import들은 주석 처리하거나 제거 가능
// import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
// import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
// import net.minecraft.resource.ResourceReloader;
// import net.minecraft.resource.ResourceManager;
// import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
// import net.minecraft.util.Identifier;

// import java.util.concurrent.CompletableFuture;
// import java.util.concurrent.Executor;

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

        // 셰이더 로드 코드 제거
        // ClientExploitManager.loadShader(MinecraftClient.getInstance());

        // 리소스 재로드 리스너 (셰이더 관련) 제거
        /*
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            public Identifier getIdentifier() {
                return Identifier.of("skannamu", "shader_reloader");
            }
            public Identifier getFabricId() {
                return this.getIdentifier();
            }
            @Override
            public CompletableFuture<Void> reload(
                    ResourceReloader.Synchronizer synchronizer,
                    ResourceManager resourceManager,
                    Executor prepareExecutor,
                    Executor applyExecutor
            ) {
                return synchronizer.whenPrepared(null).thenCompose(voided -> {
                    return CompletableFuture.runAsync(() -> {
                        ClientExploitManager.loadShader(MinecraftClient.getInstance());
                    }, applyExecutor);
                });
            }
        });
        */

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