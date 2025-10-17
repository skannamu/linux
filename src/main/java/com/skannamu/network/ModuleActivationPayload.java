package com.skannamu.network;

import com.skannamu.skannamuMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ModuleActivationPayload(String commandName) implements CustomPayload {

    public static final CustomPayload.Id<ModuleActivationPayload> ID =
            new CustomPayload.Id<>(Identifier.of(skannamuMod.MOD_ID, "module_activation"));

    public static final PacketCodec<RegistryByteBuf, ModuleActivationPayload> CODEC = new PacketCodec<>() {
        @Override
        public ModuleActivationPayload decode(RegistryByteBuf buf) {
            return new ModuleActivationPayload(PacketCodecs.STRING.decode(buf));
        }
        @Override
        public void encode(RegistryByteBuf buf, ModuleActivationPayload payload) {
            PacketCodecs.STRING.encode(buf, payload.commandName());
        }
    };
    public ModuleActivationPayload(RegistryByteBuf buf) {
        this(PacketCodecs.STRING.decode(buf));
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}