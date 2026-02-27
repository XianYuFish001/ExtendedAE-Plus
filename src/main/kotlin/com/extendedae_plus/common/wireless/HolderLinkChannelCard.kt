package com.extendedae_plus.common.wireless

import appeng.api.networking.IManagedGridNode
import appeng.api.upgrades.IUpgradeInventory
import com.extendedae_plus.common.init.EAEPDataComponents
import com.extendedae_plus.common.registry.dataComponent.DataChannelCard
import com.extendedae_plus.common.wireless.host.HostGeneric
import net.minecraft.world.level.block.entity.BlockEntity
import java.util.concurrent.atomic.AtomicReference

open class HolderLinkChannelCard(
    private val mainNode: IManagedGridNode?,
    private val getterBlockEntity: () -> BlockEntity?,
    private val getterUpgradeInventory: () -> IUpgradeInventory?
) {
    private var linkSlave: LinkSlave? = null
        get() {
            if (field == null)
                field = LinkSlave(HostGeneric(this.getterBlockEntity,  this.mainNode!!::getNode))
            return field
        }
    private var lastData: DataChannelCard? = null

    open fun onUpgradesChanged() {
        val data = this.findChannelCard()

        if (data == null && this.lastData != null) {
            this.linkSlave?.onUnloadOrRemove()
            this.lastData = null
            return
        }

        if (data != null && this.lastData != data) {
            this.linkSlave?.updateInfo(data.label.pack(), data.owner)
            this.lastData = data
        }
    }

    private fun findChannelCard(): DataChannelCard? {
        val upgradesInv = this.getterUpgradeInventory() ?: return null
        val data = AtomicReference<DataChannelCard?>()
        upgradesInv.forEach { card ->
            if (data.get() != null) return@forEach
            if (!card.has(EAEPDataComponents.CardChannel)) return@forEach
            data.set(card.get(EAEPDataComponents.CardChannel))
        }
        return data.get()
    }

    open fun onTickingInitialize() = this.linkSlave?.register()

    open fun needsInitialize() = this.lastData != null && (this.linkSlave?.connected() == false)

    companion object {
        @JvmField
        val Empty: HolderLinkChannelCard = object : HolderLinkChannelCard(
            null, { null }, { null }
        ) {
            override fun onUpgradesChanged() { }
            override fun onTickingInitialize() { }
            override fun needsInitialize() = false
        }
    }
}
