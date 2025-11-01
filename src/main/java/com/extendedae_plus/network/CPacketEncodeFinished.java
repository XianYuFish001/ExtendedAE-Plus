package com.extendedae_plus.network;

import com.extendedae_plus.ExtendedAEPlus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CPacketEncodeFinished() implements CustomPacketPayload {
    public static final Type<CPacketEncodeFinished> TYPE = new Type<>(
            ExtendedAEPlus.getLocation("encode_finished"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final CPacketEncodeFinished INSTANCE = new CPacketEncodeFinished();

    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketEncodeFinished> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public static void handle(final CPacketEncodeFinished packet, final IPayloadContext context) {
        if (context.player().level().isClientSide) PacketDistributor.sendToServer(RequestUploadingC2SPacket.INSTANCE);
    }
}
