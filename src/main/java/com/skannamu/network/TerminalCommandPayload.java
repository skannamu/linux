package com.skannamu.network;

import com.skannamu.skannamuMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;


public record TerminalCommandPayload(String command) implements CustomPayload {

    public static final CustomPayload.Id<TerminalCommandPayload> ID =
            new CustomPayload.Id<>(Identifier.of(skannamuMod.MOD_ID, "terminal_command"));


    public static final PacketCodec<RegistryByteBuf, TerminalCommandPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING,
                    TerminalCommandPayload::command,
                    TerminalCommandPayload::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}