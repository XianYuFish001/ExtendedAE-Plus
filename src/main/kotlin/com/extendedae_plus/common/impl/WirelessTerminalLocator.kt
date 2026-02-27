package com.extendedae_plus.common.impl

import appeng.api.networking.IGrid
import appeng.items.tools.powered.WirelessTerminalItem
import appeng.menu.locator.ItemMenuHostLocator
import appeng.menu.locator.MenuLocators
import com.extendedae_plus.common.impl.menuLocator.CuriosItemLocator
import com.extendedae_plus.integration.helper.ContextModLoaded
import de.mari_023.ae2wtlib.api.terminal.ItemWT
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import top.theillusivec4.curios.api.CuriosApi
import java.util.concurrent.atomic.AtomicReference

object WirelessTerminalLocator {
    fun locate(player: Player): TerminalInfo? {
        val inventory = player.getInventory()

        if (ContextModLoaded.Curios()) {
            try {
                val info = AtomicReference<TerminalInfo>()
                CuriosApi.getCuriosInventory(player).ifPresent { handler ->
                    val resultOptional = handler.findFirstCurio { it.item is WirelessTerminalItem }
                    if (resultOptional.isEmpty) return@ifPresent
                    val result = resultOptional.get()
                    info.set(
                        TerminalInfo(
                            result.stack(), result.stack().item as WirelessTerminalItem,
                            LocatedSlotContext(
                                player, false, -1,
                                result.slotContext().identifier(), result.slotContext().index()
                            )
                        )
                    )
                }
                if (info.get() != null) return info.get()
            } catch (_: Throwable) {
            }
        }

        (inventory.offhand[0].item as? WirelessTerminalItem)?.let {
            return TerminalInfo(
                inventory.offhand[0],
                it,
                LocatedSlotContext(
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
                return TerminalInfo(
                    item,
                    it,
                    LocatedSlotContext(
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
    data class TerminalInfo(
        val terminalStack: ItemStack,
        val terminal: WirelessTerminalItem,
        val context: LocatedSlotContext
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
            if (this.isWTLibTerminal && menuLocator != null) {
                return (terminal as ItemWT)
                    .getMenuHost(context.player, menuLocator, null)!!
                    .getActionableNode()
                    ?.grid
            }

            return terminal.getLinkedGrid(
                terminalStack,
                context.player.level(),
                null
            )
        }
    }

    @JvmRecord
    data class LocatedSlotContext(
        val player: Player,
        val offhand: Boolean,
        val invIndex: Int,
        val curioType: String,
        val curioIndex: Int
    )
}
