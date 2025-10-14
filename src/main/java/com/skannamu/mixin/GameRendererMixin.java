/*package com.skannamu.mixin;

import net.minecraft.client.render.RenderSystem;
import net.minecraft.client.render.program.PositionColorProgram;
import com.skannamu.client.ClientExploitManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.*;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(
            method = "render",
            at = @At(value = "RETURN")
    )
    private void skannamu$renderExploitShader(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {

        ClientExploitManager.SequenceState currentState = ClientExploitManager.getCurrentState();
        MinecraftClient client = MinecraftClient.getInstance();

        float tickDelta = tickCounter.getDynamicDeltaTicks();

        if (currentState == ClientExploitManager.SequenceState.TARGETING) {
            PostEffectProcessor shader = ClientExploitManager.getExploitShader();
            if (shader != null) {
                shader.render(tickDelta);
            }
        }
        if (currentState == ClientExploitManager.SequenceState.FADE_TO_ATTACK ||
                currentState == ClientExploitManager.SequenceState.FADE_FROM_RETURN) {
            PostEffectProcessor shader = ClientExploitManager.getGlitchShader();
            if (shader != null) {
                shader.render(tickDelta);
            }
        }

        float fadeAlpha = ClientExploitManager.getFadeAlpha();

        if (fadeAlpha > 0.0f) {

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram); // 색상 쉐이더 설정

            float alpha = fadeAlpha;
            float red = 0.0F;
            float green = 0.0F;
            float blue = 0.0F;

            RenderSystem.getModelViewStack().push();
            Matrix4f matrix = RenderSystem.getModelViewStack().peek().getPositionMatrix(); // 변수 자체는 유지 (peek()의 결과 저장용)

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();

            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            bufferBuilder.vertex(0.0F, screenHeight, -90.0F).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(screenWidth, screenHeight, -90.0F).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(screenWidth, 0.0F, -90.0F).color(red, green, blue, alpha).next();
            bufferBuilder.vertex(0.0F, 0.0F, -90.0F).color(red, green, blue, alpha).next();

            tessellator.draw();

            RenderSystem.getModelViewStack().pop();
        }
    }
}*/