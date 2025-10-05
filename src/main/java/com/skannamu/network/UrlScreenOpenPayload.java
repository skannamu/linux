/*package com.skannamu.network;

import com.skannamu.skannamuMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UrlScreenOpenPayload() implements CustomPayload {

    public static final CustomPayload.Id<UrlScreenOpenPayload> ID =
            new CustomPayload.Id<>(Identifier.of(skannamuMod.MOD_ID, "open_url_screen"));

    public static final PacketCodec<RegistryByteBuf, UrlScreenOpenPayload> CODEC =
            PacketCodec.of(
                    (payload, buf) -> {
                        // 인코더: 보낼 데이터가 없으므로 아무 작업도 하지 않습니다.
                    },
                    (buf) -> {
                        // 디코더: 받을 데이터가 없으므로 새 인스턴스를 반환합니다.
                        return new UrlScreenOpenPayload();
                    }
            );
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}*/