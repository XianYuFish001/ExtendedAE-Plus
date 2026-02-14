package com.extendedae_plus.network.base

import net.minecraft.client.player.LocalPlayer
import net.neoforged.neoforge.network.handling.IPayloadContext

interface SPacketGeneric : PacketGeneric {
    fun handleClient(player: LocalPlayer)

    fun handle(context: IPayloadContext) {
        if (context.player() is LocalPlayer)
            this.handleClient(context.player() as LocalPlayer)
    }
}
