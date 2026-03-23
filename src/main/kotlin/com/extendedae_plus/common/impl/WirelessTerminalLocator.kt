package com.extendedae_plus.common.impl

import appeng.api.networking.IGrid
import appeng.items.tools.powered.WirelessTerminalItem
import appeng.menu.locator.ItemMenuHostLocator
import appeng.menu.locator.MenuLocators
import com.extendedae_plus.common.impl.menuLocator.CuriosItemLocator
import com.extendedae_plus.integration.helper.ContextModLoaded
import com.fish.fishlib.util.extension.cast
import de.mari_023.ae2wtlib.api.terminal.ItemWT
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import top.theillusivec4.curios.api.CuriosApi
import kotlin.jvm.optionals.getOrNull

object WirelessTerminalLocator {
    fun locate(player: Player): InfoTerminal? {
        val inventory = player.getInventory()

        if (ContextModLoaded.Curios())
            CuriosApi.getCuriosInventory(player)
                .getOrNull()
                ?.let {
                    val result = it
                        .findFirstCurio { it.item is WirelessTerminalItem }
                        .getOrNull()
                        ?: return@let null
                    InfoTerminal(
                        result.stack(),
                        result.stack().item.cast(),
                        ContextSlotLocated(
                            player,
                            false,
                            -1,
                            result.slotContext().identifier(),
                            result.slotContext().index()
                        )
                    )
                }?.let { return it }

        (inventory.offhand[0].item as? WirelessTerminalItem)?.let {
            return InfoTerminal(
                inventory.offhand[0],
                it,
                ContextSlotLocated(
                    player,
                    true,
                    -1,
                    "",
                    -1
                )
            )
        }

        for (indexInv in inventory.items.indices) {
            val item = inventory.items[indexInv]
            (item.item as? WirelessTerminalItem)?.let {
                return InfoTerminal(
                    item,
                    it,
                    ContextSlotLocated(
                        player,
                        false,
                        indexInv,
                        "",
                        -1
                    )
                )
            }
        }

        return null
    }

    @JvmRecord
    data class InfoTerminal(
        val terminalStack: ItemStack,
        val terminal: WirelessTerminalItem,
        val context: ContextSlotLocated
    ) {
        val isWTLibTerminal: Boolean
            get() {
                return if (ContextModLoaded.AE2wtlib()) terminalStack.item is ItemWT
                else false
            }

        val menuLocator: ItemMenuHostLocator?
            get() {
                val curioType = context.curioType
                val curioIndex = context.curioIndex
                val slot = context.invIndex

                return when {
                    curioIndex >= 0 -> CuriosItemLocator(curioType, curioIndex, null)
                    context.offhand -> MenuLocators.forHand(context.player, InteractionHand.OFF_HAND)
                    slot >= 0 -> MenuLocators.forInventorySlot(slot)
                    else -> null
                }
            }

        fun grid(): IGrid? {
            val menuLocator = this.menuLocator

            return if (this.isWTLibTerminal && menuLocator != null) {
                (terminal as ItemWT)
                    .getMenuHost(context.player, menuLocator, null)!!
                    .getActionableNode()
                    ?.grid
            } else terminal.getLinkedGrid(
                terminalStack,
                context.player.level(),
                null
            )
        }
    }

    @JvmRecord
    data class ContextSlotLocated(
        val player: Player,
        val offhand: Boolean,
        val invIndex: Int,
        val curioType: String,
        val curioIndex: Int
    )
}
