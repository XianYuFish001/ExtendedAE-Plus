package com.extendedae_plus.network

import com.extendedae_plus.common.impl.pattern.PatternUploader
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.CPacketGeneric
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer

/**
 * C2S: 从样板访问终端上传玩家背包中的编码样板到指定的样板供应器。
 * 适用于 ExtendedAE 的 GuiExPatternTerminal 或 AE2 的 PatternAccessTermScreen。
 */
@FishNetworkPacket("upload_inventory_pattern")
@JvmRecord
data class CPacketUploadInventoryPattern(
    val playerSlotIndex: Int, val providerId: Long
) : CPacketGeneric {
    override fun handleServer(player: ServerPlayer) =
        PatternUploader.uploadFromInventory(player, this.playerSlotIndex, this.providerId)

    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<FriendlyByteBuf, CPacketUploadInventoryPattern> =
            StreamCodec.composite(
                ByteBufCodecs.INT, CPacketUploadInventoryPattern::playerSlotIndex,
                ByteBufCodecs.VAR_LONG, CPacketUploadInventoryPattern::providerId,
                ::CPacketUploadInventoryPattern
            )
    }
}
