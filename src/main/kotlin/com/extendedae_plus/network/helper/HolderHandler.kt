package com.extendedae_plus.network.helper

import com.fish.fishlib.network.HandlerClient
import com.fish.fishlib.network.base.SPacketGeneric

internal fun interface HolderHandler {
    operator fun invoke(): HandlerClient<out SPacketGeneric>

    companion object {
        val Empty = HolderHandler { HandlerClient { } }
    }
}