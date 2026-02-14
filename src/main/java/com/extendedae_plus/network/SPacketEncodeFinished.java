package com.extendedae_plus.network;

import com.extendedae_plus.network.base.EAEPNetworkPacket;
import com.extendedae_plus.network.base.SPacketGeneric;
import com.extendedae_plus.network.helper.HelperHandlerClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@EAEPNetworkPacket("encode_finished")
public class SPacketEncodeFinished implements SPacketGeneric {
    public static final SPacketEncodeFinished INSTANCE = new SPacketEncodeFinished();

    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketEncodeFinished> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void handleClient(LocalPlayer player) {
        HelperHandlerClient.instance.encodeFinished(player);
    }
}
