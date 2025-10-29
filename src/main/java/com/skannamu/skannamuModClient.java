package com.skannamu;

import com.skannamu.client.ClientExploitManager;
import com.skannamu.client.ClientHackingState;
import com.skannamu.client.ClientShaderManager;
import com.skannamu.client.gui.TerminalScreen;
import com.skannamu.client.renderer.PortableTerminalRenderer; // 💡 새로 추가된 렌더러 임포트
import com.skannamu.client.renderer.NanoBladeRenderer; // 기존 렌더러 임포트
import com.skannamu.init.ModItems;
import com.skannamu.network.*;
import com.skannamu.tooltip.standardBlockToolTip;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


public class skannamuModClient implements ClientModInitializer {

    public static boolean isPlayerActive = false;
    private static final Identifier SHADER_RELOADER_ID = Identifier.of(skannamuMod.MOD_ID, "exploit_shader_reloader");

    @Override
    public void onInitializeClient() {
        skannamuMod.LOGGER.info("skannamuMod Client initialized!");
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

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ClientShaderManager.close();
        });
    }

    private static class ExploitShaderReloader implements IdentifiableResourceReloadListener, ResourceReloader {

        @Override
        public Identifier getFabricId() {
            return SHADER_RELOADER_ID;
        }

        @Override
        public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
            return synchronizer.whenPrepared(null).thenRunAsync(() -> {
                skannamuMod.LOGGER.info("Client resources fully loaded (via ExploitShaderReloader). Initializing ClientShaderManager.");
                MinecraftClient client = MinecraftClient.getInstance();
                client.execute(() -> ClientShaderManager.initShaders(client));

                if (client.inGameHud != null) {
                    client.inGameHud.getChatHud().addMessage(Text.literal("§a[Skannamu] Initial Shaders loaded successfully."));
                }
            }, applyExecutor);
        }
    }
}
