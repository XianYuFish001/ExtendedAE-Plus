package com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedPattern

import com.extendedae_plus.common.init.EAEPItems
import com.fish.fishlib.util.extension.invoke
import com.glodblock.github.extendedae.common.blocks.matrix.BlockAssemblerMatrixBase
import net.minecraft.world.item.BlockItem

class BlockAdvancedPattern : BlockAssemblerMatrixBase<TileAdvancedPattern>() {
    override fun getPresentItem(): BlockItem = EAEPItems.CoreAdvancedPattern()
}
