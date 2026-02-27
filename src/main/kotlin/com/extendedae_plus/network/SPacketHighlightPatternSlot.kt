package com.extendedae_plus.network

import appeng.api.stacks.AEKey
import com.extendedae_plus.network.helper.HandlersClient
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.SPacketGeneric
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

/**
 * S2C: 指示客户端对某个 AEKey 的样板进行高亮/取消高亮（仅作用于接收该包的客户端）。
 * 使用 NeoForge 1.21 Payload API。
 */
@FishNetworkPacket("highlight_pattern_slot")
@JvmRecord
data class SPacketHighlightPatternSlot(val key: AEKey, val highlight: Boolean) : SPacketGeneric {
    override val handlerClient
        get() = HandlersClient.SlotPatternHighlight()

    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, SPacketHighlightPatternSlot> =
            StreamCodec.composite(
                AEKey.STREAM_CODEC, SPacketHighlightPatternSlot::key,
                ByteBufCodecs.BOOL, SPacketHighlightPatternSlot::highlight,
                ::SPacketHighlightPatternSlot
            )
    }
}


