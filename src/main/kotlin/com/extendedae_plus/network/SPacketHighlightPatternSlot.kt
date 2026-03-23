package com.extendedae_plus.network

import appeng.api.stacks.AEKey
import com.extendedae_plus.network.helper.HandlersClient
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.SPacketGeneric
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec

/**
 * @see com.extendedae_plus.network.helper.ImplHandlersClient.SlotPatternHighlight
 */
@FishNetworkPacket("highlight_pattern_slot")
@JvmRecord
data class SPacketHighlightPatternSlot(val key: AEKey) : SPacketGeneric {
    override val handlerClient
        get() = HandlersClient.SlotPatternHighlight()

    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, SPacketHighlightPatternSlot> =
            StreamCodec.composite(
                AEKey.STREAM_CODEC, SPacketHighlightPatternSlot::key,
                ::SPacketHighlightPatternSlot
            )
    }
}


