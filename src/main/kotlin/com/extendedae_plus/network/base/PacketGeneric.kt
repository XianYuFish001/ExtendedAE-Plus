package com.extendedae_plus.network.base

import net.minecraft.network.protocol.common.custom.CustomPacketPayload

interface PacketGeneric : CustomPacketPayload {
    override fun type() = Initializer.Types[this::class.simpleName]
            ?: throw IllegalStateException("Unknown NetworkPacket: ${this::class.simpleName}")
}
