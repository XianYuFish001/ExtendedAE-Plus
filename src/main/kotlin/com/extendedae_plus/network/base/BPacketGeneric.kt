package com.extendedae_plus.network.base

import net.minecraft.network.protocol.PacketFlow
import net.neoforged.neoforge.network.handling.IPayloadContext

interface BPacketGeneric : CPacketGeneric, SPacketGeneric {
    override fun handle(context: IPayloadContext) = when (context.flow()) {
        PacketFlow.CLIENTBOUND -> super<CPacketGeneric>.handle(context)
        PacketFlow.SERVERBOUND -> super<SPacketGeneric>.handle(context)
    }
}
