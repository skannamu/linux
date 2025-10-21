package com.skannamu.mixin;

import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("objectAllocator") // 이 필드 이름은 1.21.8의 중간 이름(Intermediary Name)일 수 있으며,
    ObjectAllocator getObjectAllocator();
}