/*package com.skannamu.mixin;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.shaders.ShaderType;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(ShaderLoader.class)
public interface IShaderLoader {

    @Invoker("loadShaderSource")
    static void invokeLoadShaderSource(
            Identifier id,
            Resource resource,
            ShaderType type,
            Map<Identifier, Resource> allResources,
            ImmutableMap.Builder<?, String> builder
    ) {
        throw new AssertionError();
    }
}*/