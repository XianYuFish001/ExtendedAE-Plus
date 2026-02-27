package com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedPattern

import appeng.api.crafting.IPatternDetails
import appeng.api.crafting.PatternDetailsHelper
import appeng.api.implementations.blockentities.PatternContainerGroup
import appeng.api.networking.crafting.ICraftingProvider
import appeng.api.stacks.AEItemKey
import appeng.api.stacks.KeyCounter
import appeng.util.inv.AppEngInternalInventory
import com.extendedae_plus.EAEPConfig
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.init.EAEPTiles
import com.fish.fishlib.util.extension.invoke
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixPattern
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import java.util.*

class TileAdvancedPattern(pos: BlockPos, blockState: BlockState?) : TileAssemblerMatrixPattern(pos, blockState) {
    private val patternInventory = AppEngInternalInventory(this, invSize, 1)
    private val patterns = ArrayList<IPatternDetails?>()

    init {
        this.patternInventory.setFilter(Filter(this::getLevel))
    }

    override fun saveAdditional(data: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(data, registries)
        this.patternInventory.writeToNBT(data, "pattern", registries)
    }

    override fun loadTag(data: CompoundTag, registries: HolderLookup.Provider) {
        super.loadTag(data, registries)
        this.patternInventory.readFromNBT(data, "pattern", registries)
    }

    override fun getPatternInventory() = this.patternInventory

    override fun getExposedInventory() = this.patternInventory

    override fun getTerminalPatternInventory() = this.patternInventory

    override fun updatePatterns() {
        this.patterns.clear()

        for (stack in this.patternInventory) {
            val details = PatternDetailsHelper.decodePattern(stack, this.level)
            if (details != null) {
                this.patterns.add(details)
            }
        }

        ICraftingProvider.requestUpdate(this.mainNode)
    }

    override fun addAdditionalDrops(level: Level, pos: BlockPos, drops: MutableList<ItemStack>) {
        super.addAdditionalDrops(level, pos, drops)

        for (pattern in this.patternInventory) drops.add(pattern)
    }

    override fun clearContent() {
        super.clearContent()
        this.patternInventory.clear()
    }

    override fun getAvailablePatterns() = this.patterns

    override fun pushPattern(
        patternDetails: IPatternDetails, inputHolder: Array<KeyCounter>
    ) = this.isFormed
            && this.mainNode.isActive
            && this.patterns.contains(patternDetails)
            && this.cluster.pushCraftingJob(patternDetails, inputHolder)

    override fun getTerminalGroup(): PatternContainerGroup {
        val icon = AEItemKey.of(EAEPItems.CoreAdvancedPattern)
        val name = if (this.hasCustomName()) this.customName else icon.getDisplayName()
        return PatternContainerGroup(
            icon,
            name,
            Collections.singletonList<Component>(
                Component.translatable("gui.extendedae.assembler_matrix.pattern"))
        )
    }

    override fun getType(): BlockEntityType<TileAdvancedPattern> = EAEPTiles.CoreAdvancedPattern()

    companion object {
        val invSize get() = INV_SIZE * EAEPConfig.CorePatternSlotMultiplier
    }
}
