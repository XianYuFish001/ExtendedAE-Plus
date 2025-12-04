package com.extendedae_plus.common.wireless.linkApi;

import com.extendedae_plus.EAEPConfig;
import com.extendedae_plus.util.WirelessTeamUtil;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

public class LinkRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<LinkInfo, WeakReference<ILinkHost>> activeMasters = new HashMap<>();
    private static final Map<LinkInfo, Set<WeakReference<ILinkListener>>> activeListeners = new HashMap<>();

    public static synchronized @Nullable ILinkHost findMaster(LinkInfo info) {
        if (!activeMasters.containsKey(info)) return null;
        return activeMasters.get(info).get();
    }

    public static synchronized int countListener(LinkInfo info) {
        if (!activeListeners.containsKey(info)) return 0;
        return activeListeners.get(info).size();
    }

    public static synchronized boolean registerMaster(ILinkHost master) {
        var info = LinkInfo.fromHost(master);
        if (info == null) return false;

        cleanupLinks(info);
        if (activeMasters.get(info) != null) {
            LOGGER.info("[EAEP] 同频尝试绑定重复主端, 取消注册");
            return false;
        }

        activeMasters.put(info, new WeakReference<>(master));

        var listeners = activeListeners.get(info);
        if (listeners == null) return true;
        for (var listener : Set.copyOf(listeners)) {
            listener.get().onMasterAvailable(master);
        }
        return true;
    }

    public static synchronized boolean registerListener(ILinkListener listener,
                                                        ServerLevel level,
                                                        long frequency,
                                                        @Nullable UUID placer) {
        var info = LinkInfo.fromFrequency(level, frequency, placer);
        if (info == null) return false;

        activeListeners.computeIfAbsent(info, ignore -> new HashSet<>())
                .add(new WeakReference<>(listener));

        var master = activeMasters.get(info);
        if (master == null) return false;
        if (!master.get().isEndpointRemoved())
            listener.onMasterAvailable(master.get());
        return true;
    }

    public static synchronized boolean unregisterMaster(ILinkHost master) {
        var info = LinkInfo.fromHost(master);
        if (info == null) return false;

        cleanupLinks(info);
        var existing = activeMasters.get(info);
        if (existing == null || existing.get() != master) return false;

        activeMasters.remove(info);

        var listeners = activeListeners.get(info);
        if (listeners == null) return true;
        master.onConnectionChanged(false);
        for (var listener : Set.copyOf(listeners)) {
            listener.get().onMasterUnavailable(master);
        }
        return true;
    }

    public static synchronized boolean unregisterListener(ILinkListener listener,
                                                          ServerLevel level,
                                                          long frequency,
                                                          @Nullable UUID placer) {
        var info = LinkInfo.fromFrequency(level, frequency, placer);
        if (info == null) return false;

        cleanupLinks(info);
        var existing = activeListeners.get(info);
        if (existing == null) return false;
        existing.removeIf(existingListener -> existingListener.get() == listener);
        listener.onListenerRemoved();
        if (existing.isEmpty()) {
            activeListeners.remove(info);
            var master = findMaster(LinkInfo.fromFrequency(level, frequency, placer));
            if (master != null) master.onConnectionChanged(false);
        }
        return true;
    }

    private static void cleanupLinks(LinkInfo info) {
        var master = activeMasters.get(info);
        if (master != null && master.get() == null) activeMasters.remove(info);

        var listeners = activeListeners.get(info);
        if (listeners == null) return;
        var clearedSlaves = listeners.stream()
                .map(listener -> {
                    if (listener.get() == null) return null;
                    else return listener;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        activeListeners.put(info, clearedSlaves);
    }

    public record LinkInfo(@Nullable ResourceKey<Level> keyLevel, long frequency, @Nullable UUID placer) {
        @Nullable
        public static LinkInfo fromHost(ILinkHost host) {
            ServerLevel level = host.getServerLevel();
            if (level == null || host.getFrequency() == 0L) return null;
            return fromFrequency(level, host.getFrequency(), host.getPlacer());
        }

        @Nullable
        public static LinkInfo fromFrequency(ServerLevel level, long frequency, UUID placer) {
            if (frequency == 0L) return null;
            var levelKey = EAEPConfig.WIRELESS_CROSS_DIM_ENABLE.getAsBoolean() ?
                    null : level.dimension();
            return new LinkInfo(levelKey, frequency, WirelessTeamUtil.getNetworkOwnerUUID(level, placer));
        }

        @Override
        public String toString() {
            return "Link{"
                    + (this.placer == null ? "public" : this.placer.toString().substring(0, 8))
                    + "@" + this.frequency
                    + ", " + (this.keyLevel == null ? "unknown" : this.keyLevel.location().getPath())
                    + "}";
        }
    }
}
