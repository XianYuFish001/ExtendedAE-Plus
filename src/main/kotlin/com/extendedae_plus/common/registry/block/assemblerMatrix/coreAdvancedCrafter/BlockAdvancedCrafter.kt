package com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedCrafter

import com.extendedae_plus.common.init.EAEPItems
import com.fish.fishlib.util.extension.invoke
import com.glodblock.github.extendedae.common.blocks.matrix.BlockAssemblerMatrixBase
import net.minecraft.world.item.BlockItem

class BlockAdvancedCrafter : BlockAssemblerMatrixBase<TileAdvancedCrafter>() {
    override fun getPresentItem(): BlockItem = EAEPItems.CoreAdvancedCrafter()
}
