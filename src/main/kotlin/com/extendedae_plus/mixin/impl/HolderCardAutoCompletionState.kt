package com.extendedae_plus.mixin.impl

import appeng.api.crafting.IPatternDetails
import appeng.api.features.IPlayerRegistry
import appeng.api.networking.IManagedGridNode
import appeng.api.networking.crafting.ICraftingCPU
import appeng.api.upgrades.IUpgradeInventory
import appeng.core.network.clientbound.CraftingJobStatusPacket
import appeng.me.cluster.implementations.CraftingCPUCluster
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.mixin.core.advancedae.accessor.AccessorCraftingLogicAdv
import com.extendedae_plus.mixin.core.ae2.accessor.AccessorCraftingLogic
import com.extendedae_plus.mixin.helper.HelperCraftingJob
import com.fish.fishlib.network.base.PacketGeneric.Companion.sendToPlayer
import net.minecraft.util.Tuple
import net.minecraft.world.level.block.entity.BlockEntity
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU
import java.util.concurrent.atomic.AtomicBoolean

open class HolderCardAutoCompletionState(
    private val mainNode: IManagedGridNode?,
    private val tile: () -> BlockEntity?,
    private val inventoryUpgrade: () -> IUpgradeInventory?
) {
    var cardAvailable = false
        private set

    open fun onUpgradesChanged() {
        val upgradesInv = this.inventoryUpgrade() ?: return
        val state = AtomicBoolean(false)
        upgradesInv.forEach { card ->
            if (!state.get() && card.`is`(EAEPItems.CardAutoCompletion))
                state.set(true)
        }
        this.cardAvailable = state.get()
    }

    fun completeJob(pattern: IPatternDetails) = this.taskProcess { canceler, helper ->
        val infoTask = helper.tasks

        val progress = infoTask.entries
            .find { pattern == it.key }
            ?.value
            ?.value
            ?: return@taskProcess
        if (progress > 1) return@taskProcess

        canceler()
    }

    private fun taskProcess(
        processor: (() -> Unit, HelperCraftingJob) -> Unit
    ) {
        if (!this.cardAvailable) return

        val serviceCrafting = this.mainNode?.grid?.craftingService ?: return

        val contextProcess = Tuple<(() -> Unit)?, HelperCraftingJob?>(null, null)
        serviceCrafting.cpus
            .filter(ICraftingCPU::isBusy)
            .forEach { cpu ->
                contextProcess.a = cpu::cancelJob
                contextProcess.b =
                    ((cpu as? CraftingCPUCluster)?.craftingLogic as? AccessorCraftingLogic)?.job as? HelperCraftingJob
                        ?: ((cpu as? AdvCraftingCPU)?.craftingLogic as? AccessorCraftingLogicAdv)?.job as? HelperCraftingJob
                                ?: return@forEach
            }
        val canceler = contextProcess.a ?: return
        val helper = contextProcess.b ?: return
        processor(canceler, helper)

        val player = IPlayerRegistry.getConnected(
            (tile()?.level?.server ?: return), helper.playerID ?: return
        ) ?: return
        CraftingJobStatusPacket(
            helper.link.craftingID,
            helper.outputFinal.what,
            helper.outputFinal.amount,
            helper.remainingAmount,
            CraftingJobStatusPacket.Status.FINISHED
        ).sendToPlayer(player)
    }

    companion object {
        @JvmField
        val Empty: HolderCardAutoCompletionState = object : HolderCardAutoCompletionState(
            null, { null }, { null }
        ) {
            override fun onUpgradesChanged() = Unit
        }
    }
}
