package com.extendedae_plus.network;

import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.PacketGeneric;
import com.extendedae_plus.network.base.SPacketGeneric;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EAEPNetworkPacket
public class SPacketEncodeFinished implements SPacketGeneric {
    public static final Type<SPacketEncodeFinished> TYPE = PacketGeneric.createType("encode_finished");

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final SPacketEncodeFinished INSTANCE = new SPacketEncodeFinished();

    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketEncodeFinished> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void handleClient(LocalPlayer player) {
    }

    @Override
    public void handle(final IPayloadContext context) {
        context.reply(CPacketRequestUploading.INSTANCE);
    }
}
