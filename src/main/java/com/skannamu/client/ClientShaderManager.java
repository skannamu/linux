package com.skannamu.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.ClosableFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set; // Set을 사용하기 위해 import 유지

public class ClientShaderManager {

    private static final Identifier EXPLOIT_VISION_ID = Identifier.of("skannamu", "post_effect/exploit_vision_v2.json");
    private static final Identifier GLITCH_EFFECT_ID = Identifier.of("skannamu", "post_effect/glitch_effect_v2.json");

    private static final Set<Identifier> REQUIRED_EXTERNAL_TARGETS = Set.of(
            PostEffectProcessor.MAIN
    );

    @Nullable
    private static PostEffectProcessor exploitVisionShader = null;
    @Nullable
    private static PostEffectProcessor glitchEffectShader = null;

    private static final ObjectAllocator OBJECT_ALLOCATOR = new ObjectAllocator() {
        @Override
        public <T> T acquire(ClosableFactory<T> factory) {
            T object = factory.create();
            factory.prepare(object);
            return object;
        }
        @Override
        public <T> void release(ClosableFactory<T> factory, T value) {
            factory.close(value);
        }
    };

    private static float currentTime = 0.0f;

    @Nullable
    private static PostEffectProcessor loadShader(MinecraftClient client, Identifier id) {
        String path = id.getPath();

        String fileNameWithExt = path.substring(path.lastIndexOf('/') + 1);

        String baseName = fileNameWithExt.endsWith(".json")
                ? fileNameWithExt.substring(0, fileNameWithExt.length() - 5)
                : fileNameWithExt;

        Identifier loaderId = Identifier.of(id.getNamespace(), baseName);

        System.out.println("[INFO] Attempting to load shader resource: " + id);
        System.out.println("[INFO] Loader ID (Filename only): " + loaderId);
        System.out.println("--- Starting Shader Load for: " + id + " ---");

        try {
            // ➡️ 수정된 부분: REQUIRED_EXTERNAL_TARGETS가 빈 Set으로 전달됩니다.
            PostEffectProcessor loadedShader = client.getShaderLoader().loadPostEffect(loaderId, REQUIRED_EXTERNAL_TARGETS);

            if (loadedShader == null) {
                System.out.println("[ERROR] Shader loading failed. Could not find Post Chain ID: " + loaderId);
                client.inGameHud.getChatHud().addMessage(Text.literal("§c[Shader Error] Post effect not found: " + loaderId));
            } else {
                System.out.println("[INFO] Shader loaded successfully: " + loaderId);
            }
            return loadedShader;
        } catch (Exception e) {
            System.out.println("[FATAL ERROR] Exception during shader loading for: " + loaderId);
            System.out.println("[FATAL ERROR] Message: " + e.getMessage());
            e.printStackTrace();
            client.inGameHud.getChatHud().addMessage(Text.literal("§c[Shader Error] Failed to load shader: " + loaderId + " - See console for details."));
            return null;
        } finally {
            System.out.println("--- Ending Shader Load for: " + id + " ---");
        }
    }

    public static void initShaders(MinecraftClient client) {
        if (exploitVisionShader == null) {
            exploitVisionShader = loadShader(client, EXPLOIT_VISION_ID);
        }
        if (glitchEffectShader == null) {
            glitchEffectShader = loadShader(client, GLITCH_EFFECT_ID);
        }
    }

    public static void updateAndRenderShaders(MinecraftClient client, float tickDelta) {
        if (client.world != null) {
            currentTime = (float) (client.world.getTime() + tickDelta);
        }

        boolean shouldVisionRender = shouldRenderExploitVision(client);
        exploitVisionShader = manageShader(client, exploitVisionShader, EXPLOIT_VISION_ID, shouldVisionRender);

        if (exploitVisionShader != null && shouldVisionRender) {
            exploitVisionShader.render(client.getFramebuffer(), OBJECT_ALLOCATOR);
        }

        boolean shouldGlitchRender = shouldRenderGlitchEffect(client);
        glitchEffectShader = manageShader(client, glitchEffectShader, GLITCH_EFFECT_ID, shouldGlitchRender);

        if (glitchEffectShader != null && shouldGlitchRender) {
            glitchEffectShader.render(client.getFramebuffer(), OBJECT_ALLOCATOR);
        }
    }

    @Nullable
    private static PostEffectProcessor manageShader(MinecraftClient client, @Nullable PostEffectProcessor shader, Identifier id, boolean shouldRender) {
        if (shouldRender) {
            if (shader == null) {
                return loadShader(client, id);
            }
            return shader;
        } else {
            if (shader != null) {
                System.out.println("[INFO] Closing shader: " + id);
                shader.close();
            }
            return null;
        }
    }

    private static boolean shouldRenderExploitVision(MinecraftClient client) {
        if (!ClientExploitManager.isExploitActive()) {
            return false;
        }

        if (ClientExploitManager.currentState == ClientExploitManager.SequenceState.FADE_TO_ATTACK) {
            long currentTick = client.world.getTime();
            int elapsedTicks = (int) (currentTick - ClientExploitManager.startTick);

            return elapsedTicks < ClientExploitManager.FADE_OUT_1_END_TICK;
        }

        return ClientExploitManager.currentState == ClientExploitManager.SequenceState.TARGETING ||
                ClientExploitManager.currentState == ClientExploitManager.SequenceState.WAITING_FOR_TRIGGER;
    }

    private static boolean shouldRenderGlitchEffect(MinecraftClient client) {
        if (!ClientExploitManager.isExploitActive()) {
            return false;
        }

        if (ClientExploitManager.currentState == ClientExploitManager.SequenceState.WAITING_FOR_TRIGGER) {
            return false;
        }

        if (ClientExploitManager.currentState == ClientExploitManager.SequenceState.FADE_TO_ATTACK) {
            long currentTick = client.world.getTime();
            int elapsedTicks = (int) (currentTick - ClientExploitManager.startTick);

            return elapsedTicks >= ClientExploitManager.TARGETING_END_TICK &&
                    elapsedTicks < ClientExploitManager.FADE_OUT_1_END_TICK;
        }

        return false;
    }

    public static void close() {
        if (exploitVisionShader != null) {
            System.out.println("[INFO] Closing exploitVisionShader on application close.");
            exploitVisionShader.close();
            exploitVisionShader = null;
        }
        if (glitchEffectShader != null) {
            System.out.println("[INFO] Closing glitchEffectShader on application close.");
            glitchEffectShader.close();
            glitchEffectShader = null;
        }
    }
}