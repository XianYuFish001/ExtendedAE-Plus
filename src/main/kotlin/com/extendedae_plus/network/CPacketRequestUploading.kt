package com.extendedae_plus.network

import com.extendedae_plus.mixin.helper.BridgeProviderList
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.CPacketGeneric
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer

/**
 * C2S: 请求当前终端可见的样板供应器列表（用于弹窗选择）。
 */
@FishNetworkPacket("request_uploading")
object CPacketRequestUploading : CPacketGeneric {
    override fun handleServer(player: ServerPlayer) {
        SPacketProvidersInfo.send(player,
            (player.containerMenu as? BridgeProviderList ?: return))
    }

    @PacketStreamCodec
    val streamCodec: StreamCodec<FriendlyByteBuf, CPacketRequestUploading> =
        StreamCodec.unit(CPacketRequestUploading)
}
