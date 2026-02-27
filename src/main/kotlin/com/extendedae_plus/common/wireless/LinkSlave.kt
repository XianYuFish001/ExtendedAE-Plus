package com.extendedae_plus.common.wireless

import appeng.api.networking.GridHelper
import appeng.api.networking.IGridConnection
import appeng.api.networking.IGridNode
import appeng.me.service.helpers.ConnectionWrapper
import com.extendedae_plus.common.wireless.linkApi.ILinkHost
import com.extendedae_plus.common.wireless.linkApi.ILinkListener
import com.extendedae_plus.common.wireless.linkApi.Label
import com.extendedae_plus.common.wireless.linkApi.RegistryLink
import java.util.*

class LinkSlave(private val host: ILinkHost) {
    private val listener = ListenerSlave()
    private val connection = ConnectionWrapper(null)

    fun register() {
        if (this.checkInfo()) return
        RegistryLink.registerListener(this.listener, this.host.label)
    }

    fun unregister() {
        if (this.checkInfo()) return
        RegistryLink.unregisterListener(this.listener, this.host.label)
    }

    private fun destroyConnection() {
        val nodeA: IGridNode?
        var nodeB: IGridNode? = null

        val connection = this.connection.connection
        if (connection != null) {
            nodeA = connection.a()
            nodeB = connection.b()
            connection.destroy()
        } else {
            nodeA = this.host.gridNode ?: return

            for (hostConnection in nodeA.connections) {
                if (hostConnection == null || hostConnection.isInWorld) continue
                val uncheckedNodeB = hostConnection.getOtherSide(nodeA)
                if (uncheckedNodeB.owner is ILinkHost) {
                    hostConnection.destroy()
                    nodeB = uncheckedNodeB
                }
            }
        }

        nodeA?.let {
            if (it.connections.isEmpty()) return@let
            it.grid?.tickManager?.wakeDevice(nodeA)
        }
        nodeB?.let {
            if (it.connections.isEmpty()) return@let
            it.grid?.tickManager?.wakeDevice(nodeB)
        }

        this.connection.connection = null
        this.host.onConnectionChanged(false)
    }

    private fun checkInfo() = this.host.label.data.isEmpty
            || this.host.isRemoved
            || this.host.serverLevel == null

    fun onUnloadOrRemove() {
        this.unregister()
        if (this.connected()) this.destroyConnection()
    }

    fun connected() = this.connection.connection != null

    var label: Label
        get() = this.host.label
        set(label) {
            if (this.host.label == label) return

            this.unregister()
            this.host.label = label
            this.register()
        }

    var placer: UUID?
        get() = this.host.placer
        set(placer) {
            if (placer != null && placer == this.host.placer) return

            this.unregister()
            this.host.placer = placer
            this.register()
        }

    fun updateInfo(label: Label, placer: UUID?) {
        if (this.host.label == label
            && this.host.placer === placer
        ) return

        this.unregister()
        this.host.label = label
        this.host.placer = placer
        this.register()
    }

    private inner class ListenerSlave : ILinkListener {
        override fun onMasterAvailable(master: ILinkHost) {
            if (this@LinkSlave.host.isRemoved || master.isRemoved) return
            this@LinkSlave.destroyConnection()

            val nodeA = this@LinkSlave.host.gridNode ?: return
            val nodeB = master.gridNode ?: return

            val connection = findConnection(nodeA, nodeB)
                ?: GridHelper.createConnection(nodeA, nodeB)

            this@LinkSlave.connection.connection = connection
            this@LinkSlave.host.onConnectionChanged(true)
            master.onConnectionChanged(true)
        }

        override fun onMasterUnavailable(master: ILinkHost) = this@LinkSlave.destroyConnection()

        override fun onListenerRemoved() = this@LinkSlave.destroyConnection()

        override fun emptyLabel() {
            this@LinkSlave.host.label = Label.Empty
        }
    }

    companion object {
        private fun findConnection(nodeA: IGridNode, nodeB: IGridNode): IGridConnection? {
            for (connection in nodeA.connections) {
                val connectionNodeA = connection.a()
                val connectionNodeB = connection.b()
                if ((connectionNodeA == nodeA || connectionNodeB == nodeA)
                    && (connectionNodeA == nodeB || connectionNodeB == nodeB)
                ) return connection
            }
            return null
        }
    }
}
