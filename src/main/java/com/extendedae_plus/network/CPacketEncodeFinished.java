package com.extendedae_plus.network;

import com.extendedae_plus.network.base.CPacketGeneric;
import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EAEPNetworkPacket
public class CPacketEncodeFinished implements CPacketGeneric {
    public static final Type<CPacketEncodeFinished> TYPE = PacketGeneric.createType("encode_finished");

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final CPacketEncodeFinished INSTANCE = new CPacketEncodeFinished();

    public static final StreamCodec<RegistryFriendlyByteBuf, CPacketEncodeFinished> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void handleServer(ServerPlayer player) {
    }

    @Override
    public void handle(final IPayloadContext context) {
//        if (context.player().level().isClientSide) PacketDistributor.sendToServer(CPacketRequestUploading.INSTANCE);
        context.reply(CPacketRequestUploading.INSTANCE);
    }
}
