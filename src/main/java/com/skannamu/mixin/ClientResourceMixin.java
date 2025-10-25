/*package com.skannamu.mixin;

import com.skannamu.client.ClientShaderManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public abstract class ClientResourceMixin {

    @Inject(
            method = "reloadResources",
            at = @At("RETURN")
    )
    private void skannamu$onResourceReloadStart(
            CallbackInfoReturnable<CompletableFuture<Void>> cir
    ) {
        cir.getReturnValue().thenRun(() -> {
            MinecraftClient client = (MinecraftClient)(Object)this;

            client.execute(() -> {
                System.out.println("[Skannamu|ResourceMixin] Resource reload complete. Initializing shaders on Main Thread.");
                ClientShaderManager.initShaders(client);

                if (client.inGameHud != null) {
                    client.inGameHud.getChatHud().addMessage(net.minecraft.text.Text.literal("§a[Skannamu] Shaders reloaded successfully."));
                }
            });
        });
    }
}*/
