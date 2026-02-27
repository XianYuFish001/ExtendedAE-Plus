package com.extendedae_plus.common.wireless

import com.extendedae_plus.common.wireless.linkApi.ILinkHost
import com.extendedae_plus.common.wireless.linkApi.Label
import com.extendedae_plus.common.wireless.linkApi.RegistryLink
import java.util.*

/**
 * 主收发器端逻辑：负责在频率变化/加载时向注册中心登记唯一主端，卸载时反注册。
 * 方块实体应在合适的生命周期中调用 register/unregister。
 */
class LinkMaster(private val host: ILinkHost) {
    private var registered = false

    var label: Label
        get() = this.host.label
        set(label) {
            if (this.host.label == label) return

            this.unregister()
            this.host.label = label
            this.register()
        }

    fun updateInfo(label: Label, placer: UUID?) {
        if (this.host.label == label
            && this.host.placer == placer
        ) return

        this.unregister()
        this.host.label = label
        this.host.placer = placer
        this.register()
    }

    fun setPlacer(placer: UUID?) {
        this.host.placer = placer
    }

    fun register(): Boolean {
        val level = this.host.serverLevel
        if (level == null || this.host.label.data.isEmpty) return false
        val succeed = RegistryLink.registerMaster(this.host)
        this.registered = succeed
        return succeed
    }

    fun unregister() {
        val level = this.host.serverLevel
        if (!this.registered || level == null || this.host.label.data.isEmpty) return
        RegistryLink.unregisterMaster(this.host)
        this.registered = false
    }

    fun connected() = RegistryLink.countListener(this.host.label) > 0

    fun onUnloadOrRemove() = this.unregister()
}
