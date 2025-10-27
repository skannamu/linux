package com.skannamu.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.opengl.GlStateManager; // GlStateManager의 _viewport는 그대로 사용 가능
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.ClosableFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import net.minecraft.client.gl.WindowFramebuffer;

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
        close();

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

        WindowFramebuffer mainTarget = (WindowFramebuffer) client.getFramebuffer();

        // ⭐️ LOG 1: 뷰포트 변경 전 상태 (디버깅 목적)
        System.out.println("[DEBUG] [Pre-Render] Main Target Size: W=" + mainTarget.textureWidth + ", H=" + mainTarget.textureHeight);

        // 1단계: 기존 렌더링 상태(ModelView & Projection)를 스택에 저장합니다.
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.backupProjectionMatrix(); // ⭐️ 투영 행렬 백업 추가
        System.out.println("[DEBUG] [Push] All Matrix states pushed.");

        // 2단계: 뷰포트를 전체 화면 크기로 명시적으로 재설정합니다.
        GlStateManager._viewport(0, 0, mainTarget.textureWidth, mainTarget.textureHeight);
        System.out.println("[DEBUG] [Viewport Set] Set viewport to (0, 0, " + mainTarget.textureWidth + ", " + mainTarget.textureHeight + ")");


        boolean shouldVisionRender = shouldRenderExploitVision(client);
        if (exploitVisionShader != null && shouldVisionRender) {
            System.out.println("[DEBUG] Rendering Exploit Vision Shader...");
            exploitVisionShader.render(mainTarget, OBJECT_ALLOCATOR);
            System.out.println("[DEBUG] Exploit Vision Shader Rendered.");
        }


        boolean shouldGlitchRender = shouldRenderGlitchEffect(client);
        if (glitchEffectShader != null && shouldGlitchRender) {
            System.out.println("[DEBUG] Rendering Glitch Effect Shader...");
            glitchEffectShader.render(mainTarget, OBJECT_ALLOCATOR);
            System.out.println("[DEBUG] Glitch Effect Shader Rendered.");
        }

        // 3단계: 셰이더 렌더링이 끝난 후 저장된 이전 상태를 복원합니다.
        RenderSystem.restoreProjectionMatrix(); // ⭐️ 투영 행렬 복원 추가
        RenderSystem.getModelViewStack().popMatrix();

        // ⭐️ LOG 3: 뷰포트 복원 후 상태 확인
        System.out.println("[DEBUG] [Pop] All Matrix states popped. Restored previous rendering state.");
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
            System.out.println("[INFO] Closing exploitVisionShader on application close or resource reload.");
            exploitVisionShader.close();
            exploitVisionShader = null;
        }
        if (glitchEffectShader != null) {
            System.out.println("[INFO] Closing glitchEffectShader on application close or resource reload.");
            glitchEffectShader.close();
            glitchEffectShader = null;
        }
    }
}