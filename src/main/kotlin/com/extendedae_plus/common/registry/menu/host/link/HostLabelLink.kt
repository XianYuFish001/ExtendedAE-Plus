package com.extendedae_plus.common.registry.menu.host.link

import com.extendedae_plus.common.wireless.linkApi.Label

interface HostLabelLink {
    val labelData: Label.Data

    fun setLabelData(label: Label.Data, force: Boolean = false): Boolean

    val isLockable: Boolean
        get() = false

    val isMasterable: Boolean
        get() = false

    val isLocked: Boolean
        get() = false

    val isMaster: Boolean
        get() = false

    fun toggleLock() {
    }

    fun toggleMaster() {
    }
}
