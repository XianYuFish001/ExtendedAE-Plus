package com.extendedae_plus.common.registry.menu

import appeng.menu.AEBaseMenu
import appeng.menu.guisync.GuiSync
import com.extendedae_plus.common.init.EAEPMenuTypes
import com.extendedae_plus.common.registry.item.priorityTool.DataPriority
import com.extendedae_plus.common.registry.item.priorityTool.DataPriority.ModeTool
import com.extendedae_plus.common.registry.menu.host.HostPriorityTool
import com.fish.fishlib.util.extension.invoke
import net.minecraft.world.entity.player.Inventory

class MenuPriorityTool(
    id: Int,
    playerInventory: Inventory,
    private val host: HostPriorityTool
) : AEBaseMenu(
    EAEPMenuTypes.PriorityTool(),
    id,
    playerInventory,
    host
) {
    @GuiSync(0)
    var priority = 0

    @GuiSync(1)
    var mode = ModeTool.Keep

    var data
        get() = DataPriority(this.priority, this.mode)
        set(data) {
            this.host.data = data

            val hostData = host.data
            this.priority = hostData.priority
            this.mode = hostData.modeTool
        }

    init {
        val data = host.data
        this.priority = data.priority
        this.mode = data.modeTool
    }
}
