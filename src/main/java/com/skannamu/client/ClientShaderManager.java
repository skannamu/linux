package com.skannamu.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectPass;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.ClosableFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ClientShaderManager {

    private static final Identifier EXPLOIT_VISION_ID = Identifier.of("skannamu", "shaders/post/exploit_vision");
    private static final Identifier GLITCH_EFFECT_ID = Identifier.of("skannamu", "shaders/post/glitch_effect");

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

    public static void updateAndRenderShaders(MinecraftClient client, float tickDelta) {
        if (client.world != null) {
            currentTime = (float) (client.world.getTime() + tickDelta);
        }
        boolean shouldVisionRender = shouldRenderExploitVision(client);
        exploitVisionShader = manageShader(client, exploitVisionShader, EXPLOIT_VISION_ID, shouldVisionRender);

        if (exploitVisionShader != null) {
            updateShaderTime(exploitVisionShader);
            if (shouldVisionRender) {
                // OpenGL 렌더링 파이프라인에 쉐이더 효과를 적용합니다.
                exploitVisionShader.render(client.getFramebuffer(), OBJECT_ALLOCATOR);
            }
        }
        boolean shouldGlitchRender = shouldRenderGlitchEffect(client);
        glitchEffectShader = manageShader(client, glitchEffectShader, GLITCH_EFFECT_ID, shouldGlitchRender);

        if (glitchEffectShader != null) {
            updateShaderTime(glitchEffectShader);
            if (shouldGlitchRender) {
                // OpenGL 렌더링 파이프라인에 쉐이더 효과를 적용합니다.
                glitchEffectShader.render(client.getFramebuffer(), OBJECT_ALLOCATOR);
            }
        }
    }

    private static boolean shouldRenderExploitVision(MinecraftClient client) {
        if (!ClientExploitManager.isExploitActive()) return false;

        if (ClientExploitManager.isWaitingForTrigger()) {
            return true;
        }
        if (client.world != null) {
            // ClientExploitManager의 필드에 대한 접근이 가능하다고 가정합니다.
            long elapsedTicks = client.world.getTime() - ClientExploitManager.startTick;
            return elapsedTicks < ClientExploitManager.FADE_OUT_1_END_TICK;
        }
        return false;
    }
    private static boolean shouldRenderGlitchEffect(MinecraftClient client) {
        if (ClientExploitManager.isWaitingForTrigger()) return false;

        if (ClientExploitManager.isExploitActive() && client.world != null) {
            long elapsedTicks = client.world.getTime() - ClientExploitManager.startTick;
            return elapsedTicks >= 0 && elapsedTicks < ClientExploitManager.FADE_OUT_1_END_TICK;
        }
        return false;
    }
    @Nullable
    private static PostEffectProcessor manageShader(MinecraftClient client, @Nullable PostEffectProcessor shader, Identifier id, boolean shouldRender) {
        if (shouldRender) {
            if (shader == null) {
                System.out.println("--- Starting Shader Load Debug for: " + id + " ---");
                try {
                    ShaderLoader shaderLoader = client.getShaderLoader();
                    System.out.println("[DEBUG] ShaderLoader retrieved: " + shaderLoader);
                    Identifier resourcePath = id.withPath(id.getPath() + ".json");
                    boolean jsonResourceExists = client.getResourceManager().getResource(resourcePath).isPresent();
                    System.out.println("[DEBUG] Full JSON Resource exists for " + resourcePath + ": " + jsonResourceExists);
                    client.getResourceManager().findResources("shaders/program", path -> path.getPath().endsWith(".vsh") || path.getPath().endsWith(".fsh") || path.getPath().endsWith(".json")).forEach((path, resource) -> {
                        System.out.println("[DEBUG] Found shader program resource: " + path);
                    });
                    if (jsonResourceExists) {
                        try (InputStream stream = client.getResourceManager().getResource(resourcePath).get().getInputStream()) {
                            String jsonContent = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                            System.out.println("[DEBUG] Shader JSON content START:");
                            System.out.println(jsonContent);
                            System.out.println("[DEBUG] Shader JSON content END.");
                        } catch (IOException e) {
                            System.out.println("[ERROR] Failed to read shader resource stream: " + e.getMessage());
                        }
                    } else {
                        System.out.println("[ERROR] JSON file not found in resource manager. Check path/case.");
                    }

                    // 쉐이더 프로그램 JSON 경로를 명시적으로 지정
                    Identifier programJsonPath = Identifier.of(id.getNamespace(), "shaders/program/exploit_program.json");
                    boolean programJsonExists = client.getResourceManager().getResource(programJsonPath).isPresent();
                    System.out.println("[DEBUG] Program JSON exists: " + programJsonPath + " -> " + programJsonExists);

                    Identifier vshPath = Identifier.of(id.getNamespace(), "shaders/program/exploit_vision.vsh");
                    Identifier fshPath = Identifier.of(id.getNamespace(), "shaders/program/exploit_vision.fsh");
                    boolean vshExists = client.getResourceManager().getResource(vshPath).isPresent();
                    boolean fshExists = client.getResourceManager().getResource(fshPath).isPresent();
                    System.out.println("[DEBUG] Vertex shader exists: " + vshPath + " -> " + vshExists);
                    System.out.println("[DEBUG] Fragment shader exists: " + fshPath + " -> " + fshExists);

                    Identifier blitVshPath = Identifier.of("skannamu", "shaders/program/blit.vsh");
                    Identifier blitFshPath = Identifier.of("skannamu", "shaders/program/blit.fsh");
                    Identifier blitJsonPath = Identifier.of("skannamu", "shaders/program/blit.json");
                    boolean blitVshExists = client.getResourceManager().getResource(blitVshPath).isPresent();
                    boolean blitFshExists = client.getResourceManager().getResource(blitFshPath).isPresent();
                    boolean blitJsonExists = client.getResourceManager().getResource(blitJsonPath).isPresent();
                    System.out.println("[DEBUG] Custom blit vertex shader exists: " + blitVshPath + " -> " + blitVshExists);
                    System.out.println("[DEBUG] Custom blit fragment shader exists: " + blitFshPath + " -> " + blitFshExists);
                    System.out.println("[DEBUG] Custom blit JSON exists: " + blitJsonPath + " -> " + blitJsonExists);

                    System.out.println("[DEBUG] Attempting to load PostEffectProcessor for: " + id);

                    PostEffectProcessor loadedShader = shaderLoader.loadPostEffect(id, Collections.emptySet());

                    if (loadedShader == null) {
                        System.out.println("[ERROR] Shader loading failed. The Post Chain JSON is likely malformed or references an invalid Program ID.");
                        client.inGameHud.getChatHud().addMessage(Text.literal("§c[Shader Error] Post effect not found: " + id));
                    } else {
                        System.out.println("[INFO] Shader loaded successfully!");
                    }
                    return loadedShader;
                } catch (Exception e) {
                    System.out.println("[FATAL ERROR] Exception during shader loading process for: " + id);
                    System.out.println("[FATAL ERROR] Message: " + e.getMessage());
                    System.out.println("[FATAL ERROR] Stacktrace:");
                    e.printStackTrace();
                    client.inGameHud.getChatHud().addMessage(Text.literal("§c[Shader Error] Failed to load shader: " + id));
                    return null;
                } finally {
                    System.out.println("--- Ending Shader Load Debug for: " + id + " ---");
                }
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

    private static void updateShaderTime(PostEffectProcessor processor) {
        /*List<PostEffectPass> passes = processor.getShaders();
        if (passes.isEmpty()) return;

        PostEffectPass firstPass = passes.get(0);

        ShaderProgram program = (ShaderProgram)firstPass.getProgram();

        GlUniform timeUniform = program.getUniform("Time");
        if (timeUniform != null) {
            timeUniform.set(currentTime);
        }

        if (processor == exploitVisionShader) {
            GlUniform targetingUniform = program.getUniform("Targeting");
            if (targetingUniform != null) {
                targetingUniform.set(1.0f);
            }
        }*/
    }

    /**
     * 클라이언트가 종료될 때 모든 활성 쉐이더를 닫고 정리합니다.
     */
    public static void close() {
        if (exploitVisionShader != null) {
            exploitVisionShader.close();
            exploitVisionShader = null;
        }
        if (glitchEffectShader != null) {
            glitchEffectShader.close();
            glitchEffectShader = null;
        }
    }
}
