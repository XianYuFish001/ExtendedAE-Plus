package com.extendedae_plus.client.screen

import appeng.client.gui.AEBaseScreen
import appeng.client.gui.NumberEntryType
import appeng.client.gui.style.ScreenStyle
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems
import com.extendedae_plus.client.render.widgets.button.EAEPServerCycleButton
import com.extendedae_plus.client.render.widgets.button.SyncerEnumGeneric
import com.extendedae_plus.common.registry.menu.MenuPriorityTool
import com.extendedae_plus.network.CPacketPriorityToolOperation
import com.fish.fishlib.network.base.PacketGeneric.Companion.sendToServer
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class ScreenPriorityTool(
    menu: MenuPriorityTool, playerInventory: Inventory, title: Component, style: ScreenStyle
) : AEBaseScreen<MenuPriorityTool>(menu, playerInventory, title, style) {
    private val priority = widgets.addNumberEntryWidget("priority", NumberEntryType.UNITLESS)
    private val buttonCycleMode: EAEPServerCycleButton

    init {
        this.priority.setTextFieldStyle(style.getWidget("priorityInput"))
        this.priority.setMinValue(Int.MIN_VALUE.toLong())
        this.priority.setLongValue(menu.data.priority.toLong())
        this.priority.setOnChange(this::savePriority)
        this.priority.setOnConfirm {
            savePriority()
            this.onClose()
        }

        this.buttonCycleMode = EAEPServerCycleButton.Builder()
            .setTask(CPacketPriorityToolOperation(null, true))
            .addPart(EAEPActionItems.PriorityKeep)
            .addPart(EAEPActionItems.PriorityIncrement)
            .addPart(EAEPActionItems.PriorityDecrement)
            .setSyncer(SyncerEnumGeneric { menu.data.modeTool })
            .build()
        this.addToLeftToolbar(this.buttonCycleMode)
    }

    override fun updateBeforeRender() {
        super.updateBeforeRender()
        this.buttonCycleMode.updateState()
    }

    private fun savePriority() = CPacketPriorityToolOperation(
        this.priority.getIntValue().orElse(0), false
    ).sendToServer()
}
