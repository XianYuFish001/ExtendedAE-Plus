package com.extendedae_plus.common.registry.menu.host

import appeng.api.implementations.menuobjects.ItemMenuHost
import appeng.menu.locator.ItemMenuHostLocator
import com.extendedae_plus.common.init.EAEPDataComponents
import com.extendedae_plus.common.registry.item.priorityTool.DataPriority
import com.extendedae_plus.common.registry.item.priorityTool.ItemPriorityTool
import com.fish.fishlib.util.component
import com.fish.fishlib.util.extension.invoke
import net.minecraft.world.entity.player.Player

class HostPriorityTool(
    item: ItemPriorityTool, player: Player, locator: ItemMenuHostLocator
) : ItemMenuHost<ItemPriorityTool>(item, player, locator) {
    init {
        val itemStack = this.itemStack
        if (!itemStack.has(EAEPDataComponents.Priority)) {
            itemStack.set(
                EAEPDataComponents.Priority,
                DataPriority(0, DataPriority.ModeTool.Keep)
            )
        }
    }

    var data by this.itemStack.component(EAEPDataComponents.Priority())
}
