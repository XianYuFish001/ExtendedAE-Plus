package com.extendedae_plus.common.wireless.linkApi

import com.mojang.logging.LogUtils
import java.lang.ref.WeakReference

object RegistryLink {
    private val Logger = LogUtils.getLogger()

    @get:Synchronized
    val labels = ArrayList<Label>()

    @Synchronized
    fun findMaster(info: Label) = info.master.get()

    @Synchronized
    fun clear() = this.labels.clear()

    @JvmStatic
    @Synchronized
    fun countListener(info: Label) = info.listeners.size

    @Synchronized
    internal fun registerOrGetLabel(data: Label.Data): Label {
        if (data.isEmpty) return Label.Empty
        val existing = labels
            .find(data::equals)
        val newLabel = Label(data)
        if (existing == null) labels.add(newLabel)
        return existing ?: newLabel
    }

    @JvmStatic
    @Synchronized
    fun removeLabel(data: Label.Data) {
        labels.filter(data::equals)
            .forEach { label ->
                label.master.get()?.let { master ->
                    master.onConnectionChanged(false)
                    master.label = Label.Empty
                }
                label.cleanupReference()
                label.listeners.forEach { reference ->
                    reference.get()?.let {
                        label.master.get()?.let(it::onMasterUnavailable)
                        it.emptyLabel()
                    }
                }
            }
        labels.removeIf(data::equals)
    }

    @JvmStatic
    @Synchronized
    fun registerMaster(master: ILinkHost): Boolean {
        val label = master.label
        if (!labels.contains(label)) {
            Logger.warn("[EAEP/link] 尝试为未注册的标签注册主端")
            return false
        }
        if (label.master.get()?.isRemoved == false) {
            Logger.warn("[EAEP/link] 同标签尝试绑定重复主端")
            return false
        }

        label.master = WeakReference(master)
        label.cleanupReference()
        label.listeners.forEach { it.get()?.onMasterAvailable(master) }

        return true
    }

    @JvmStatic
    @Synchronized
    fun registerListener(listener: ILinkListener, label: Label): Boolean {
        label.cleanupReference()
        label.listeners += WeakReference(listener)

        val master = label.master.get() ?: return false

        label.listeners.forEach {
            it.get()?.onMasterAvailable(master)
        }
        return true
    }

    @JvmStatic
    @Synchronized
    fun unregisterMaster(master: ILinkHost): Boolean {
        val label = master.label
        if (!labels.contains(label)) {
            Logger.warn("[EAEP/link] 尝试使用未注册的标签")
            return false
        }
        if (label.master.get()?.isRemoved == true) {
            Logger.warn("[EAEP/link] 尝试注销未注册/不同的主端")
            return false
        }

        label.master = WeakReference(null)
        label.cleanupReference()
        label.listeners.forEach { it.get()?.onMasterUnavailable(master) }
        return true
    }

    @JvmStatic
    @Synchronized
    fun unregisterListener(listener: ILinkListener, label: Label): Boolean {
        label.cleanupReference()
        label.listeners.removeIf { it.get() == listener }
        listener.onListenerRemoved()

        if (label.listeners.isEmpty())
            label.master.get()?.onConnectionChanged(false)
        return true
    }

    private fun Label.cleanupReference() = this.listeners.removeIf { it.get() == null }
}
