package com.extendedae_plus.common.wireless.linkApi;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.util.HashSet;

public class RegistryLink {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final HashSet<Label> labels = new HashSet<>();

    public static synchronized @Nullable ILinkHost findMaster(Label info) {
        return info.master.get();
    }

    public static synchronized HashSet<Label> getLabels() {
        return labels;
    }

    public static synchronized void clear() {
        labels.clear();
    }

    public static synchronized int countListener(Label info) {
        return info.listeners.size();
    }

    static synchronized Label registerOrGetLabel(Label.Data data) {
        if (data.isEmpty()) return Label.EMPTY;
        var existing = labels.stream()
                .filter(data::equals)
                .findAny();
        var newLabel = new Label(data);
        if (existing.isEmpty()) labels.add(newLabel);
        return existing.orElse(newLabel);
    }

    public static synchronized void removeLabel(Label.Data data) {
        labels.stream()
                .filter(data::equals)
                .forEach(label -> {
                    var master = label.master.get();
                    if (master != null) {
                        master.onConnectionChanged(false);
                        master.setLabel(Label.EMPTY);
                    }
                    cleanupReference(label);
                    label.listeners.forEach(listener -> {
                        listener.get().onMasterUnavailable(label.master.get());
                        listener.get().emptyLabel();
                    });
                });
        labels.removeIf(data::equals);
    }

    public static synchronized boolean registerMaster(ILinkHost master) {
        var label = master.getLabel();
        if (!labels.contains(label)) {
            LOGGER.warn("[EAEP/link] 尝试为未注册的标签注册主端");
            return false;
        }
        if (label.master.get() != null && !label.master.get().isRemoved()) {
            LOGGER.warn("[EAEP/link] 同标签尝试绑定重复主端");
            return false;
        }

        label.master = new WeakReference<>(master);
        cleanupReference(label);
        label.listeners.forEach(listener ->
                listener.get().onMasterAvailable(master));
        
        return true;
    }

    public static synchronized boolean registerListener(ILinkListener listener, Label label) {
        cleanupReference(label);
        label.listeners.add(new WeakReference<>(listener));
        
        var master = label.master.get();
        if (master == null) return false;
        
        label.listeners.forEach(reference ->
                reference.get().onMasterAvailable(master));
        return true;
    }

    public static synchronized boolean unregisterMaster(ILinkHost master) {
        var label = master.getLabel();
        if (!labels.contains(label)) {
            LOGGER.warn("[EAEP/link] 尝试使用未注册的标签");
            return false;
        }
        if (label.master.get() == null || label.master.get().isRemoved()) {
            LOGGER.warn("[EAEP/link] 尝试注销未注册/不同的主端");
            return false;
        }

        label.master = new WeakReference<>(null);
        cleanupReference(label);
        label.listeners.forEach(reference ->
                reference.get().onMasterUnavailable(master));
        return true;
    }

    public static synchronized boolean unregisterListener(ILinkListener listener, Label label) {
        cleanupReference(label);
        label.listeners.removeIf(reference ->
                reference.get() == listener);
        listener.onListenerRemoved();

        if (label.listeners.isEmpty() && label.master.get() != null) {
            label.master.get().onConnectionChanged(false);
        }
        return true;
    }

    private static void cleanupReference(Label label) {
        label.listeners.removeIf(reference ->
                reference.get() == null);
    }
}
