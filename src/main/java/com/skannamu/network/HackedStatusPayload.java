package com.skannamu.network;

import com.skannamu.skannamuMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record HackedStatusPayload(boolean isHacked) implements CustomPayload {

    public static final CustomPayload.Id<HackedStatusPayload> ID =
            new CustomPayload.Id<>(Identifier.of(skannamuMod.MOD_ID, "hacked_status"));

    public static final PacketCodec<RegistryByteBuf, HackedStatusPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.BOOLEAN,
                    HackedStatusPayload::isHacked,
                    HackedStatusPayload::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}