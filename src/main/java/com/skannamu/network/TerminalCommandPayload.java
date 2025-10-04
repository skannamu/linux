// src/main/java/com/skannamu/network/TerminalCommandPayload.java

package com.skannamu.network;

import com.skannamu.skannamuMod;
import net.minecraft.network.RegistryByteBuf; // ⚡️ PacketByteBuf 대신 RegistryByteBuf 사용
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs; // ⚡️ 코덱 헬퍼 클래스 사용
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

// 클라이언트 -> 서버 명령어 전송을 위한 Custom Payload
public record TerminalCommandPayload(String command) implements CustomPayload {

    // 패킷 ID 정의 (Identifier.of 사용)
    public static final CustomPayload.Id<TerminalCommandPayload> ID =
            new CustomPayload.Id<>(Identifier.of(skannamuMod.MOD_ID, "terminal_command"));

    // ⚡️ 오류 해결: PacketCodecs.STRING을 사용하여 코덱을 간결하게 정의합니다.
    public static final PacketCodec<RegistryByteBuf, TerminalCommandPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING, // String 타입에 대한 기본 인코더/디코더 사용
                    TerminalCommandPayload::command, // 레코드에서 command 필드를 가져오는 메서드 레퍼런스
                    TerminalCommandPayload::new     // 레코드를 생성하는 생성자 레퍼런스
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}