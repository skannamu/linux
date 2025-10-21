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

    @Inject(method = "reloadResources(ZLnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;Ljava/util/concurrent/CompletableFuture;)Ljava/util/concurrent/CompletableFuture;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/Util;method_18174(Ljava/lang/Object;)Ljava/lang/Object;",
                    shift = At.Shift.AFTER))
    private void skannamu$onPostResourceReload(boolean bl, ResourceManager resourceManager, Profiler profiler, CompletableFuture<Void> asyncTasks, CallbackInfoReturnable<CompletableFuture<Void>> info) {
        info.getReturnValue().thenRun(() -> {
            MinecraftClient client = (MinecraftClient)(Object)this;
            ClientShaderManager.initShaders(client);
        });
    }
}