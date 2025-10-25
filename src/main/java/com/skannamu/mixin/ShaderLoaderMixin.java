/*package com.skannamu.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.logging.LogUtils;
import com.skannamu.mixin.IShaderLoader;
import net.minecraft.client.gl.ShaderLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;

@Mixin(ShaderLoader.class)
public abstract class ShaderLoaderMixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    // 이 Mixin은 셰이더 소스 파일이 Resource Loader에 의해 누락되거나,
    // 짧은 이름 키 매핑이 실패하는 경우(현재 문제)를 해결하기 위해
    // Vanilla 로직의 간섭을 완전히 막고 수동으로 주입하는 공격적인 방식입니다.

    @Inject(
            method = "prepare",
            at = @At(
                    value = "INVOKE",
                    // Map.entrySet()이 호출되기 직전에 실행하여
                    // Vanilla 로직이 sourceBuilder를 채우기 전에 우리가 원하는 소스를 삽입합니다.
                    target = "Ljava/util/Map;entrySet()Ljava/util/Set;",
                    shift = At.Shift.BEFORE
            )
    )
    private void skannamu$purgeAndForceInjectShaderSources(
            ResourceManager resourceManager,
            Profiler profiler,
            ShaderType type, // prepare 메서드의 세 번째 인자(ShaderType)를 캡처
            CallbackInfoReturnable<ShaderLoader.Definitions> cir,
            // 캡처된 로컬 변수:
            @Local(ordinal = 0) ImmutableMap.Builder<String, String> sourceBuilder,
            @Local Map<Identifier, Resource> allResources // 키가 Resource Path인 맵 (Vanilla loop의 소스)
    ) {
        // 주입해야 할 셰이더 리소스의 실제 경로와 타입 정의
        // 이 경로는 modid:shaders/program/*.vsh / *.fsh 형식이어야 합니다.
        Map<Identifier, ShaderType> requiredShaders = Map.of(
                Identifier.of("skannamu", "shaders/program/exploit_vision_v.vsh"), ShaderType.VERTEX,
                Identifier.of("skannamu", "shaders/program/exploit_vision_f.fsh"), ShaderType.FRAGMENT,
                Identifier.of("skannamu", "shaders/program/glitch_effect_v.vsh"), ShaderType.VERTEX,
                Identifier.of("skannamu", "shaders/program/glitch_effect_f.fsh"), ShaderType.FRAGMENT
        );

        LOGGER.info("[Skannamu|ShaderFix] Starting PURGE-AND-INJECT: Preventing Vanilla conflicts for custom shaders. Current Type: {}", type);

        for (Map.Entry<Identifier, ShaderType> entry : requiredShaders.entrySet()) {
            Identifier resourcePath = entry.getKey(); // 전체 경로
            ShaderType shaderType = entry.getValue();

            // 현재 prepare 호출의 ShaderType과 일치하는 셰이더만 처리합니다. (필수 수정)
            if (shaderType != type) {
                continue;
            }

            // 짧은 이름 (JSON 셰이더 정의에서 참조하는 이름)을 계산합니다.
            String shortName = resourcePath.getPath()
                    .replace("shaders/program/", "")
                    .replace(".vsh", "")
                    .replace(".fsh", "");
            Identifier injectionKey = Identifier.of(resourcePath.getNamespace(), shortName); // 짧은 이름 ID

            try {
                // 1. ResourceManager를 사용하여 리소스가 실제로 존재하는지 확인합니다.
                Resource resource = resourceManager.getResource(resourcePath).orElse(null);

                if (resource != null) {
                    // 2. Vanilla의 로직이 이 리소스를 중복 로드하거나 잘못된 키로 변환하지 못하도록,
                    //    allResources 맵에서 해당 전체 경로 엔트리를 제거합니다.
                    if (allResources.containsKey(resourcePath)) {
                        allResources.remove(resourcePath);
                        LOGGER.warn("[Skannamu|ShaderFix] PURGED: Removed full path {} from Vanilla's map to prevent duplicate/bad key mapping.", resourcePath);
                    }

                    // 3. 우리가 계산한 정확한 짧은 이름 키로 sourceBuilder에 강제 주입합니다.
                    LOGGER.info("[Skannamu|ShaderFix] MANUAL INJECT START: Injecting {} with correct short key {}.", resourcePath, injectionKey);

                    IShaderLoader.invokeLoadShaderSource(
                            injectionKey,       // 짧은 이름 (sourceBuilder의 키)
                            resource,
                            shaderType,
                            allResources,       // full path가 키로 다시 추가됩니다. (loadShaderSource 내부 동작)
                            sourceBuilder       // short name이 키로 추가됩니다.
                    );

                    LOGGER.info("[Skannamu|ShaderFix] PURGE-AND-INJECT SUCCESS: {} injected.", injectionKey);

                } else {
                    // 이 로그는 리소스 파일 자체가 모드 jar에 없음을 의미합니다.
                    LOGGER.error("[Skannamu|ShaderFix] CRITICAL FAILURE: Required file NOT found by ResourceManager: {}", resourcePath);
                }
            } catch (UnsupportedOperationException e) {
                LOGGER.error("[Skannamu|ShaderFix] FATAL ERROR: Cannot modify allResources map (it is immutable). Resource loading fix failed for {}! Full path entry remains.", resourcePath);
            } catch (Exception e) {
                // 이 예외는 주로 sourceBuilder에 중복 키를 넣으려 했을 때 발생합니다.
                // 이 경우, 제거 로직이 실패했거나 예상치 못한 다른 모드와의 충돌일 수 있습니다.
                LOGGER.error("[Skannamu|ShaderFix] MANUAL INJECT FAIL (Unexpected): Failed to inject shader {} with key {}: {}",
                        resourcePath, injectionKey, e.getMessage());
            }
        }
    }
}*/
