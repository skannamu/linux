package com.skannamu.mixin;

import com.skannamu.client.ClientExploitManager;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(
            method = "renderWorld",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZFF)V", shift = At.Shift.AFTER)
    )
    private void skannamu$injectCameraShake(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        float shake = ClientExploitManager.getCameraShakeAmount();

        if (shake > 0.0f) {
            float pitchShake = (float) (Math.random() * shake * 2 - shake);
            float yawShake = (float) (Math.random() * shake * 2 - shake);

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitchShake));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yawShake));
        }
    }
    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V", shift = At.Shift.AFTER)
    )
    private void skannamu$renderExploitShader(float tickDelta, long startTime, MatrixStack matrices, CallbackInfo ci) {

        ClientExploitManager.SequenceState currentState = ClientExploitManager.getCurrentState();

        if (currentState == ClientExploitManager.SequenceState.TARGETING) {
            ShaderEffect shader = ClientExploitManager.getExploitShader();
            if (shader != null) {
                shader.render(tickDelta);
            }
        }
        if (currentState == ClientExploitManager.SequenceState.FADE_TO_ATTACK ||
                currentState == ClientExploitManager.SequenceState.FADE_FROM_RETURN) {
            ShaderEffect shader = ClientExploitManager.getGlitchShader();
            if (shader != null) {
                // 글리치 셰이더 렌더링
                shader.render(tickDelta);
            }
        }
    }
}