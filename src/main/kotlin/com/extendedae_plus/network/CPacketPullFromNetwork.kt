package com.extendedae_plus.network

import appeng.api.stacks.AEItemKey
import appeng.api.stacks.AEKeyType
import appeng.api.stacks.GenericStack
import appeng.api.storage.StorageHelper
import appeng.me.helpers.PlayerSource
import appeng.menu.me.crafting.CraftAmountMenu
import com.extendedae_plus.EAEPConfig
import com.extendedae_plus.common.impl.WirelessTerminalLocator
import com.extendedae_plus.common.impl.WirelessTerminalLocator.InfoTerminal
import com.fish.fishlib.network.FishNetworkPacket
import com.fish.fishlib.network.PacketStreamCodec
import com.fish.fishlib.network.base.CPacketGeneric
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import kotlin.math.max

@FishNetworkPacket("pull_from_network")
@JvmRecord
data class CPacketPullFromNetwork(
    val stack: GenericStack, val doPull: Boolean, val toInventory: Boolean
) : CPacketGeneric {
    override fun handleServer(player: ServerPlayer) {
        val what = this.stack.what()
        if (what.type != AEKeyType.items()) return

        val info = WirelessTerminalLocator.locate(player) ?: return

        val terminalStack = info.terminalStack
        if (terminalStack.isEmpty) return

        val grid = info.grid() ?: return

        if (this.doPull
            && (setItem(player, info) || !EAEPConfig.CraftWhenToPullInsufficient)) return

        val craftingService = grid.craftingService
        if (craftingService.isCraftable(what)) openPlanMenu(player, info)
    }

    private fun setItem(player: ServerPlayer, info: InfoTerminal): Boolean {
        val itemKey = this.stack.what() as AEItemKey
        val amount = this.stack.amount()

        val grid = info.grid() ?: return false

        val energy = grid.energyService
        val storage = grid.storageService.inventory

        val extracted = StorageHelper.poweredExtraction(energy, storage, itemKey, amount, PlayerSource(player))
        if (extracted <= 0) return false

        val extractedStack = itemKey.toStack(extracted.toInt())
        val playerInv = player.getInventory()

        if (!this.toInventory) {
            val cursorStack = player.containerMenu.carried

            if (cursorStack.isEmpty) {
                player.containerMenu.carried = extractedStack.copyAndClear()
            } else if (ItemStack.isSameItemSameComponents(cursorStack, extractedStack)) {
                val maxStackSize = cursorStack.maxStackSize
                val totalAmount = cursorStack.count + extractedStack.count

                if (totalAmount <= maxStackSize) {
                    cursorStack.count = totalAmount
                    player.containerMenu.carried = cursorStack
                    extractedStack.count = 0
                } else {
                    val overflow = totalAmount - maxStackSize
                    cursorStack.count = maxStackSize
                    player.containerMenu.carried = cursorStack

                    val overflowStack = extractedStack.copyWithCount(overflow)
                    playerInv.add(overflowStack)
                    extractedStack.count = overflowStack.count
                }
            } else playerInv.add(extractedStack)
        } else playerInv.add(extractedStack)

        val powerUsage = max(0.5, (extracted - extractedStack.count) * 0.05)
        info.terminal.usePower(player, powerUsage, info.terminalStack)

        if (!extractedStack.isEmpty) StorageHelper.poweredInsert(
            energy,
            storage,
            itemKey,
            extractedStack.count.toLong(),
            PlayerSource(player)
        )

        player.containerMenu.broadcastChanges()
        return true
    }

    private fun openPlanMenu(player: ServerPlayer, info: InfoTerminal) = info.menuLocator?.let {
        CraftAmountMenu.open(
            player,
            it,
            this.stack.what(),
            this.stack.amount().toInt()
        )
    }

    companion object {
        @PacketStreamCodec
        val streamCodec: StreamCodec<RegistryFriendlyByteBuf, CPacketPullFromNetwork> =
            StreamCodec.composite(
                GenericStack.STREAM_CODEC, CPacketPullFromNetwork::stack,
                ByteBufCodecs.BOOL, CPacketPullFromNetwork::doPull,
                ByteBufCodecs.BOOL, CPacketPullFromNetwork::toInventory,
                ::CPacketPullFromNetwork
            )
    }
}
