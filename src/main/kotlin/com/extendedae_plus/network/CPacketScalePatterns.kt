package com.extendedae_plus.network

import appeng.api.crafting.PatternDetailsHelper
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems
import com.extendedae_plus.mixin.helper.HelperProviderMenu
import com.extendedae_plus.util.extension.ExtensionScaledPattern.create
import com.extendedae_plus.util.extension.ExtensionScaledPattern.writeToStack
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.CPacketGeneric
import com.fish.fishlib.network.base.PacketGeneric.Companion.sendToServer
import com.fish.fishlib.util.extension.unit
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer

/**
 * C2S：请求对当前打开的样板供应器执行样板数量缩放（倍增或除法）。
 */
@FishNetworkPacket("scale_patterns")
@JvmRecord
data class CPacketScalePatterns(val scale: Int) : CPacketGeneric {
    override fun handleServer(player: ServerPlayer) = (player.containerMenu as? HelperProviderMenu)
        ?.invPattern
        ?.forEach {
            PatternDetailsHelper.decodePattern(it, player.serverLevel())
                ?.create(this.scale.toLong(), false)
                ?.writeToStack(it)
        }.unit()

    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<FriendlyByteBuf, CPacketScalePatterns> =
            StreamCodec.composite(
                ByteBufCodecs.INT, CPacketScalePatterns::scale,
                ::CPacketScalePatterns
            )

        @JvmStatic
        fun send(action: EAEPActionItems) = when (action) {
            EAEPActionItems.Mul2 -> 2
            EAEPActionItems.Mul3 -> 3
            EAEPActionItems.Mul5 -> 5
            EAEPActionItems.Div2 -> -2
            EAEPActionItems.Div3 -> -3
            EAEPActionItems.Div5 -> -5
            else -> null
        }?.let(::CPacketScalePatterns)?.sendToServer()
    }
}
