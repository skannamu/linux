package com.skannamu.mixin;

import com.skannamu.client.ClientExploitManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void skannamu$renderExploitFadeOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        float alpha = ClientExploitManager.getFadeAlpha();

        if (alpha > 0.0f) {
            int alphaInt = (int) (alpha * 255.0F);
            int color = (alphaInt << 24) | 0x000000;

            int screenWidth = context.getScaledWindowWidth();
            int screenHeight = context.getScaledWindowHeight();

            context.fill(0, 0, screenWidth, screenHeight, color);
        }
    }
}
