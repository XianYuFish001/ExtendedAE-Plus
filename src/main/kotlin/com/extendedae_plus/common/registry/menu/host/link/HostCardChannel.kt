package com.extendedae_plus.common.registry.menu.host.link

import appeng.api.implementations.menuobjects.ItemMenuHost
import appeng.menu.locator.ItemMenuHostLocator
import com.extendedae_plus.common.registry.dataComponent.DataChannelCard
import com.extendedae_plus.common.registry.item.upgradeCard.CardChannel
import com.extendedae_plus.common.wireless.linkApi.Label
import net.minecraft.world.entity.player.Player

class HostCardChannel(
    item: CardChannel, player: Player, locator: ItemMenuHostLocator
) : ItemMenuHost<CardChannel>(item, player, locator), HostLabelLink {
    override val labelData: Label.Data
        get() = DataChannelCard.getLabel(this.itemStack)

    override fun setLabelData(label: Label.Data, force: Boolean): Boolean {
        DataChannelCard.setLabel(this.itemStack, label, true)
        return true
    }
}
