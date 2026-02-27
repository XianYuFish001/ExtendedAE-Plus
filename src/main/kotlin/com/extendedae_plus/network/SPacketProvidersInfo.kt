package com.extendedae_plus.network

import com.extendedae_plus.common.impl.pattern.InfoProvider
import com.extendedae_plus.mixin.helper.BridgeProviderList
import com.extendedae_plus.network.helper.HandlersClient
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.SPacketGeneric
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.PacketDistributor

@FishNetworkPacket("provider_info")
@JvmRecord
data class SPacketProvidersInfo(val info: MutableList<InfoProvider>) : SPacketGeneric {
    override val handlerClient
        get() = HandlersClient.ProvidersInfo()

    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, SPacketProvidersInfo> =
            StreamCodec.composite(
                InfoProvider.streamCodec.apply(ByteBufCodecs.list()),
                SPacketProvidersInfo::info,
                ::SPacketProvidersInfo
            )

        @JvmStatic
        fun send(player: ServerPlayer, helper: BridgeProviderList) {
            val providers = helper.`eaep$getProviderList`()
            if (providers.isEmpty()) return

            val info = ArrayList<InfoProvider>()
            providers.forEach { (group, containers) ->
                var availableSlots = 0
                for (container in containers) {
                    val inv = container.terminalPatternInventory
                    for (indexStack in 0..<inv.size()) {
                        if (inv.getStackInSlot(indexStack).isEmpty) availableSlots++
                    }
                }
                info.add(InfoProvider(
                    group.name(),
                    group.icon(),
                    group.hashCode(),
                    availableSlots
                ))
            }

            PacketDistributor.sendToPlayer(player, SPacketProvidersInfo(info))
        }
    }
}
