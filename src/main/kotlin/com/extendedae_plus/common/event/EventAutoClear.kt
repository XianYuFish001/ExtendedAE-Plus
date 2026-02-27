package com.extendedae_plus.common.event

import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.client.impl.AliasGetter
import com.extendedae_plus.common.wireless.linkApi.RegistryLink
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.GameShuttingDownEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
object EventAutoClear {
    @SubscribeEvent
    fun onServerStopped(event: ServerStoppedEvent) {
        RegistryLink.clear()
    }

    @SubscribeEvent
    fun onGameShuttingDown(event: GameShuttingDownEvent) {
        AliasGetter.closeConfig()
    }
}
