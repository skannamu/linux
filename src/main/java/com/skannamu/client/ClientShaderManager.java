package com.skannamu.client;

import com.skannamu.mixin.MinecraftClientAccessor; // 🌟 새로 추가된 Accessor 임포트
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.util.ObjectAllocator; // ObjectAllocator 임포트
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;

public class ClientShaderManager {

    private static final Identifier EXPLOIT_VISION_ID = Identifier.of("skannamu", "exploit_vision");
    private static final Identifier GLITCH_EFFECT_ID = Identifier.of("skannamu", "glitch_effect");
    @Nullable
    private static PostEffectProcessor exploitVisionShader = null;
    @Nullable
    private static PostEffectProcessor glitchEffectShader = null;
    private static float currentTime = 0.0f;
    public static void initShaders(MinecraftClient client) {
        if (exploitVisionShader == null) {
            exploitVisionShader = loadShader(client, EXPLOIT_VISION_ID);
        }
        if (glitchEffectShader == null) {
            glitchEffectShader = loadShader(client, GLITCH_EFFECT_ID);
        }
    }
    @Nullable
    private static PostEffectProcessor loadShader(MinecraftClient client, Identifier id) {
        System.out.println("--- Starting Shader Load for: " + id + " ---");
        try {
            PostEffectProcessor loadedShader = client.getShaderLoader().loadPostEffect(id, Collections.emptySet());

            if (loadedShader == null) {
                System.out.println("[ERROR] Shader loading failed. Could not find Post Chain ID: " + id);
                client.inGameHud.getChatHud().addMessage(Text.literal("§c[Shader Error] Post effect not found: " + id));
            } else {
                System.out.println("[INFO] Shader loaded successfully: " + id);
            }
            return loadedShader;
        } catch (Exception e) {
            System.out.println("[FATAL ERROR] Exception during shader loading for: " + id);
            System.out.println("[FATAL ERROR] Message: " + e.getMessage());
            e.printStackTrace();
            client.inGameHud.getChatHud().addMessage(Text.literal("§c[Shader Error] Failed to load shader: " + id));
            return null;
        } finally {
            System.out.println("--- Ending Shader Load for: " + id + " ---");
        }
    }
    public static void updateAndRenderShaders(MinecraftClient client, float tickDelta) {
        if (client.world != null) {
            currentTime = (float) (client.world.getTime() + tickDelta);
        }

        ObjectAllocator allocator = ((MinecraftClientAccessor) client).getObjectAllocator();
        boolean shouldVisionRender = shouldRenderExploitVision(client);
        if (exploitVisionShader != null) {
            if (shouldVisionRender) {
                exploitVisionShader.render(client.getFramebuffer(), allocator); // 🌟 수정
            }
        }
        boolean shouldGlitchRender = shouldRenderGlitchEffect(client);
        if (glitchEffectShader != null) {
            if (shouldGlitchRender) {
                glitchEffectShader.render(client.getFramebuffer(), allocator); // 🌟 수정
            }
        }
        if (!shouldVisionRender && exploitVisionShader != null) {
            exploitVisionShader.close();
            exploitVisionShader = null;
        }
        if (!shouldGlitchRender && glitchEffectShader != null) {
            glitchEffectShader.close();
            glitchEffectShader = null;
        }
    }
    private static boolean shouldRenderExploitVision(MinecraftClient client) {
        // if (!ClientExploitManager.isExploitActive()) return false;
        return false;
    }
    private static boolean shouldRenderGlitchEffect(MinecraftClient client) {
        return true;
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