package com.extendedae_plus.network

import com.extendedae_plus.network.helper.HandlersClient
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.SPacketGeneric
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec

@FishNetworkPacket("encode_finished")
object SPacketEncodeFinished : SPacketGeneric {
    override val handlerClient = HandlersClient.EncodeFinished()

    @PacketStreamCodec
    val streamCodec: StreamCodec<RegistryFriendlyByteBuf, SPacketEncodeFinished> =
        StreamCodec.unit(SPacketEncodeFinished)
}
