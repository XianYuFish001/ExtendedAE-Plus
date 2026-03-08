package com.extendedae_plus.network

import com.extendedae_plus.common.registry.menu.labelLink.MenuLabelLink
import com.extendedae_plus.common.registry.menu.labelLink.MenuLabelLink.LabelMapped
import com.extendedae_plus.common.wireless.linkApi.RegistryLink
import com.extendedae_plus.integration.helper.ManagerIntegration
import com.extendedae_plus.integration.impl.point.IntegrationFTBTeams
import com.extendedae_plus.network.helper.HandlersClient
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.SPacketGeneric
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.PacketDistributor

@FishNetworkPacket("label_list")
@JvmRecord
data class SPacketLabelList(val labels: MutableList<LabelMapped>) : SPacketGeneric {
    override val handlerClient get() = HandlersClient.LabelList()

    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, SPacketLabelList> =
            StreamCodec.composite(
                LabelMapped.streamCodec.apply(ByteBufCodecs.list()),
                SPacketLabelList::labels,
                ::SPacketLabelList
            )

        @JvmStatic
        fun send(menu: MenuLabelLink) {
            val uuidPlayer = menu.player.uuid
            val uuidWrapped = ManagerIntegration<IntegrationFTBTeams>()
                ?.getTeamUUID(uuidPlayer)
                ?: uuidPlayer

            var serial = Int.MIN_VALUE
            val labels = RegistryLink.labels
                .filter { it.data.placer == uuidWrapped || it.data.placer == null }
                .mapTo(ArrayList()) { LabelMapped(serial++, it.data) }
            menu.setLabels(labels)
            PacketDistributor.sendToPlayer(menu.player as ServerPlayer, SPacketLabelList(labels))
        }
    }
}