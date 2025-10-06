package com.skannamu.network;

import com.skannamu.skannamuMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TerminalOutputPayload(String output) implements CustomPayload {

    public static final CustomPayload.Id<TerminalOutputPayload> ID =
            new CustomPayload.Id<>(Identifier.of(skannamuMod.MOD_ID, "terminal_output"));


    public static final PacketCodec<RegistryByteBuf, TerminalOutputPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            TerminalOutputPayload::output,
            TerminalOutputPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}