package com.extendedae_plus.common.event;

import com.extendedae_plus.ExtendedAEPlus;
import com.extendedae_plus.common.wireless.linkApi.RegistryLink;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
public class EventAutoClear {
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        RegistryLink.clear();
    }
}
