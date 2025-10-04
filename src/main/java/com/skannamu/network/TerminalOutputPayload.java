// src/main/java/com/skannamu/network/TerminalOutputPayload.java (최종 권장 수정본)

package com.skannamu.network;

import com.skannamu.skannamuMod;
import net.minecraft.network.RegistryByteBuf; // PacketByteBuf 대신 RegistryByteBuf를 사용하는 것이 최신 표준입니다.
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs; // ⚡️ 이 헬퍼 클래스를 사용합니다.
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TerminalOutputPayload(String output) implements CustomPayload {

    // 페이로드 ID 정의
    public static final CustomPayload.Id<TerminalOutputPayload> ID =
            new CustomPayload.Id<>(Identifier.of(skannamuMod.MOD_ID, "terminal_output"));

    // ⚡️ 오류 해결: PacketCodecs.STRING을 사용하여 코덱을 간결하게 정의합니다.
    public static final PacketCodec<RegistryByteBuf, TerminalOutputPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, // String 타입에 대한 기본 인코더/디코더 사용
            TerminalOutputPayload::output, // 레코드에서 output 필드를 가져오는 메서드 레퍼런스
            TerminalOutputPayload::new     // 레코드를 생성하는 생성자 레퍼런스
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}