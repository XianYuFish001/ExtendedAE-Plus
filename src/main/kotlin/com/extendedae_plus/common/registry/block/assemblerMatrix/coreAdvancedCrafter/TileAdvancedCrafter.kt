package com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedCrafter

import appeng.api.crafting.IPatternDetails
import appeng.api.inventories.InternalInventory
import appeng.api.networking.IGridNode
import appeng.api.networking.security.IActionSource
import appeng.api.networking.ticking.TickRateModulation
import appeng.api.networking.ticking.TickingRequest
import appeng.api.stacks.GenericStack
import appeng.api.stacks.KeyCounter
import appeng.util.inv.AppEngInternalInventory
import appeng.util.inv.CombinedInternalInventory
import com.extendedae_plus.EAEPConfig
import com.extendedae_plus.common.init.EAEPTiles
import com.extendedae_plus.mixin.helper.HelperAssemblerMatrixModifier
import com.fish.fishlib.util.extension.invoke
import com.glodblock.github.extendedae.common.me.CraftingMatrixThread
import com.glodblock.github.extendedae.common.me.CraftingThread
import com.glodblock.github.extendedae.common.me.matrix.ClusterAssemblerMatrix
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixCrafter
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import java.util.stream.IntStream
import kotlin.math.min

class TileAdvancedCrafter(pos: BlockPos, blockState: BlockState) : TileAssemblerMatrixCrafter(pos, blockState) {
    private val threads: Array<CraftingThread>
    private val internalInv: InternalInventory
    private var states: Short = 0
    private var currentThread: Int = EAEPConfig.BaseCoreCrafterThreads

    init {
        val threads = ArrayList<CraftingThread>(maxThread)
        val inventories = arrayOfNulls<InternalInventory>(maxThread)
        IntStream.range(0, maxThread).forEach { thread ->
            threads += CraftingMatrixThread(
                this,
                { this.source },
                { this.changeState(thread, it) }
            )
            inventories[thread] = threads[thread].internalInventory
        }
        this.threads = threads.toTypedArray()
        this.internalInv = CombinedInternalInventory(*inventories)
    }

    private val source: IActionSource?
        get() = this.cluster.src

    private fun changeState(index: Int, state: Boolean) {
        val oldState = this.states > 0
        if (state) {
            this.states = (this.states.toInt() or (1 shl index)).toShort()
        } else {
            this.states = (this.states.toInt() and (1 shl index).inv()).toShort()
        }

        if (state) {
            if (!oldState) {
                this.mainNode.ifPresent { grid, node ->
                    grid?.tickManager?.wakeDevice(node)
                }
            }
        } else if (oldState && this.states <= 0) {
            this.mainNode.ifPresent { grid, node -> grid?.tickManager?.sleepDevice(node) }
        }
    }

    override fun usedThread(): Int {
        var used = 0
        for (threadIndex in 0..<this.currentThread) {
            val thread = this.threads[threadIndex]
            if (thread.currentPattern != null || !thread.internalInventory.isEmpty) used++
        }

        return used
    }

    override fun pushJob(patternDetails: IPatternDetails, inputHolder: Array<KeyCounter>): Boolean {
        for (thread in 0..<this.currentThread) {
            if (this.threads[thread].acceptJob(patternDetails, inputHolder, Direction.DOWN)) {
                (this.cluster as? HelperAssemblerMatrixModifier)?.`eaep$updateCrafter`(this)
                return true
            }
        }

        return false
    }

    override fun stop() {
        for (thread in this.threads) thread.stop()
    }

    override fun saveAdditional(data: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(data, registries)

        IntStream.range(0, maxThread).forEach { thread ->
            data.put(
                "#thread{$thread}",
                this.threads[thread].writeNBT(registries)
            )
        }

        val inventory = CompoundTag()
        IntStream.range(0, this.internalInv.size()).forEach { invIndex ->
            inventory.put(
                "item{$invIndex}",
                this.internalInv.getStackInSlot(invIndex).saveOptional(registries)
            )
        }
        data.put("inv", inventory)
    }

    override fun loadTag(data: CompoundTag, registries: HolderLookup.Provider) {
        super.loadTag(data, registries)

        IntStream.range(0, maxThread).forEach { thread ->
            val dataKey = "#thread{$thread}"
            if (!data.contains(dataKey)) return@forEach
            this.threads[thread].readNBT(data.getCompound(dataKey), registries)
        }

        val inventory = data.getCompound("inv")
        IntStream.range(0, this.internalInv.size()).forEach { invIndex ->
            val item = inventory.getCompound("item{$invIndex}")
            this.internalInv.setItemDirect(invIndex, ItemStack.parseOptional(registries, item))
        }
    }

    override fun add(cluster: ClusterAssemblerMatrix) =
        (cluster as? HelperAssemblerMatrixModifier)?.`eaep$addCrafter`(this) ?: Unit

    override fun getTickingRequest(node: IGridNode): TickingRequest {
        var isAwake = false

        for (threadIndex in 0..<this.currentThread) {
            val thread = this.threads[threadIndex]
            thread.recalculatePlan()
            thread.updateSleepiness()
            isAwake = isAwake || thread.isAwake
        }

        return TickingRequest(1, 1, !isAwake)
    }

    override fun tickingRequest(node: IGridNode, ticksSinceLastCall: Int): TickRateModulation {
        if (this.cluster == null) {
            return TickRateModulation.SLEEP
        } else {
            this.calculateCurrentThread()

            var rate = TickRateModulation.SLEEP

            for (threadIndex in 0..<this.currentThread) {
                val thread = this.threads[threadIndex]
                if (!thread.isAwake) continue

                val currentRate = thread.tick(min(this.cluster.speedCore, 5), ticksSinceLastCall)
                if (currentRate.ordinal > rate.ordinal) rate = currentRate
            }

            (this.cluster as? HelperAssemblerMatrixModifier)?.`eaep$updateCrafter`(this)
            return rate
        }
    }

    override fun saveChangedInventory(inv: AppEngInternalInventory) {
        for (t in this.threads) {
            if (inv == t.internalInventory) {
                t.recalculatePlan()
                break
            }
        }

        this.saveChanges()
    }

    override fun addAdditionalDrops(level: Level, pos: BlockPos, drops: MutableList<ItemStack>) {
        super.addAdditionalDrops(level, pos, drops)

        for (stack in this.internalInv) {
            val genericStack = GenericStack.unwrapItemStack(stack)
            if (genericStack != null) {
                genericStack.what().addDrops(genericStack.amount(), drops, level, pos)
            } else {
                drops.add(stack)
            }
        }
    }

    override fun clearContent() {
        super.clearContent()
        this.internalInv.clear()
    }

    private fun calculateCurrentThread() {
        val multiplier = Math.floorDiv(cluster.speedCore, 5)
        val amplification = EAEPConfig.CoreCrafterThreadAmplification
        val baseThreads = EAEPConfig.BaseCoreCrafterThreads
        this.currentThread = min(
            multiplier * amplification + baseThreads,
            maxThread
        )
    }

    override fun getType(): BlockEntityType<TileAdvancedCrafter> = EAEPTiles.CoreAdvancedCrafter()

    companion object {
        @JvmStatic
        val maxThread get() = EAEPConfig.MaximumCoreCrafterThreads
    }
}
