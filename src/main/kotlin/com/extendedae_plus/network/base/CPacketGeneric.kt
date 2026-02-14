package com.extendedae_plus.network.base

import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.network.handling.IPayloadContext

interface CPacketGeneric : PacketGeneric {
    fun handleServer(player: ServerPlayer)

    fun handle(context: IPayloadContext) {
        if (context.player() is ServerPlayer)
            this.handleServer(context.player() as ServerPlayer)
    }
}
