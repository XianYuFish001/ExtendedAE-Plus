package com.extendedae_plus.network

import appeng.menu.me.items.PatternEncodingTermMenu
import com.extendedae_plus.common.impl.pattern.PatternUploader
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.CPacketGeneric
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer

@FishNetworkPacket("upload_terminal_pattern")
@JvmRecord
data class CPacketUploadTerminalPattern(val serverID: Int) : CPacketGeneric {
    override fun handleServer(player: ServerPlayer) {
        PatternUploader.uploadFromMenuEncoding(
            player.containerMenu as? PatternEncodingTermMenu ?: return,
            this.serverID
        )
    }

    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<FriendlyByteBuf, CPacketUploadTerminalPattern> =
            StreamCodec.composite(
                ByteBufCodecs.INT, CPacketUploadTerminalPattern::serverID,
                ::CPacketUploadTerminalPattern
            )
    }
}
