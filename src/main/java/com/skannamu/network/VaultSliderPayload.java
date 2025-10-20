package com.skannamu.network;

import com.skannamu.skannamuMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record VaultSliderPayload(BlockPos pos, int sliderIndex, int value) implements CustomPayload {
    public static final Id<VaultSliderPayload> SLIDER_UPDATE_ID = new Id<>(Identifier.of(skannamuMod.MOD_ID, "slider_update"));
    public static final Id<VaultSliderPayload> SLIDER_SUBMIT_ID = new Id<>(Identifier.of(skannamuMod.MOD_ID, "slider_submit"));

    private static final PacketCodec<RegistryByteBuf, BlockPos> BLOCK_POS = new PacketCodec<>() {
        @Override
        public BlockPos decode(RegistryByteBuf buf) { return buf.readBlockPos(); }
        @Override
        public void encode(RegistryByteBuf buf, BlockPos pos) { buf.writeBlockPos(pos); }
    };

    public static final PacketCodec<RegistryByteBuf, VaultSliderPayload> CODEC = PacketCodec.tuple(
            BLOCK_POS, VaultSliderPayload::pos,
            PacketCodecs.INTEGER, VaultSliderPayload::sliderIndex,
            PacketCodecs.INTEGER, VaultSliderPayload::value,
            VaultSliderPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        // sliderIndex == -1 → submit 전용
        return sliderIndex == -1 ? SLIDER_SUBMIT_ID : SLIDER_UPDATE_ID;
    }
}
