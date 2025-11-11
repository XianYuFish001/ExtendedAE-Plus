package com.extendedae_plus.common.impl.storage.infinityCell;

import com.extendedae_plus.ExtendedAEPlus;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = ExtendedAEPlus.MODID)
public class HolderInfinityStorageManager {
    private static InfinityStorageManager storageManager;
    private static MinecraftServer storageManagerServer;

    @SubscribeEvent
    private static void onServerStarted(ServerStartedEvent event) {
        storageManagerServer = event.getServer();
        storageManager = InfinityStorageManager.getInstance(event.getServer());
    }

    @SubscribeEvent
    private static void onServerStopped(ServerStoppedEvent event) {
        if (storageManagerServer == event.getServer()) {
            storageManagerServer = null;
            storageManager = null;
        }
    }

    @Nullable
    public static InfinityStorageManager currentStorageManager() {
        return storageManager;
    }
}
