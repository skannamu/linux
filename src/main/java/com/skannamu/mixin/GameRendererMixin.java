package com.skannamu.mixin;

import com.skannamu.client.ClientExploitManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(
            method = "render",
            at = @At(value = "HEAD")
    )
    private void skannamu$overrideCameraPitch(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (!ClientExploitManager.isExploitActive()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            float tickDelta = tickCounter.getDynamicDeltaTicks();
            float newPitch = ClientExploitManager.updateAndGetCameraPitch(client.player.getPitch(), tickDelta);
            client.player.setPitch(newPitch);
        }
    }
}
