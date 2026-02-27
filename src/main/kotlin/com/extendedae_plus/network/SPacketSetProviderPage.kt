package com.extendedae_plus.network

import com.extendedae_plus.network.helper.HandlersClient
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.SPacketGeneric
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

/**
 * S2C: 指示客户端在已打开的样板供应器界面切换到指定页
 */
@FishNetworkPacket("set_provider_page")
@JvmRecord
data class SPacketSetProviderPage(val page: Int) : SPacketGeneric {
    override val handlerClient
        get() = HandlersClient.ProviderPage()

    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, SPacketSetProviderPage> =
            StreamCodec.composite(
                ByteBufCodecs.INT, SPacketSetProviderPage::page,
                ::SPacketSetProviderPage
            )
    }
}


