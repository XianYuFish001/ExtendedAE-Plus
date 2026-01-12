package com.extendedae_plus.common.registry.menu.host.linkLabel;

import com.extendedae_plus.common.wireless.linkApi.Label;

public interface HostLabelLink {
    Label.Data getLabelData();

    boolean setLabelData(Label.Data label, boolean force);

    default boolean isLockable() {
        return false;
    }

    default boolean isMasterable() {
        return false;
    }

    default boolean isLocked() {
        return false;
    }

    default boolean isMaster() {
        return false;
    }

    default void toggleLock() {
    }

    default void toggleMaster() {
    }
}
