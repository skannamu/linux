package com.skannamu.mixin;

import com.skannamu.client.ClientShaderManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public class ClientResourceMixin {

    @Inject(method = "reloadResources",
            at = @At("HEAD")) // 메서드 시작 지점에 인젝션합니다.
    private void skannamu$onResourceReloadStart(boolean bl, ResourceManager resourceManager, Profiler profiler, CompletableFuture<Void> asyncTasks, CallbackInfoReturnable<CompletableFuture<Void>> info) {

        info.getReturnValue().thenRun(() -> {
            MinecraftClient client = (MinecraftClient)(Object)this;
            client.execute(() -> {
                ClientShaderManager.initShaders(client);
            });
        });
    }
}