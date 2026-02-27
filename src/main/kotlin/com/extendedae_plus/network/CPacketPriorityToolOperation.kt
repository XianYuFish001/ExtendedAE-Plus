package com.extendedae_plus.network

import appeng.util.EnumCycler
import com.extendedae_plus.common.registry.item.priorityTool.DataPriority
import com.extendedae_plus.common.registry.menu.MenuPriorityTool
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.CPacketGeneric
import com.fish.fishlib.util.extension.optional
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import kotlin.jvm.optionals.getOrNull

@FishNetworkPacket("priority_tool_operation")
@JvmRecord
data class CPacketPriorityToolOperation(val priority: Int?, val rotateMode: Boolean) : CPacketGeneric {
    override fun handleServer(player: ServerPlayer) {
        val menu = player.containerMenu as? MenuPriorityTool ?: return
        val data: DataPriority = menu.data
        val newData = DataPriority(
            this.priority ?: data.priority,
            if (this.rotateMode)
                EnumCycler.next(data.modeTool)
            else
                data.modeTool
        )
        menu.data = newData
    }

    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, CPacketPriorityToolOperation> =
            StreamCodec.composite(
                ByteBufCodecs.optional(ByteBufCodecs.INT),
                CPacketPriorityToolOperation::priority.optional(),
                ByteBufCodecs.BOOL,
                CPacketPriorityToolOperation::rotateMode
            ) { priority, rotateMode ->
                CPacketPriorityToolOperation(priority.getOrNull(), rotateMode)
            }
    }
}
