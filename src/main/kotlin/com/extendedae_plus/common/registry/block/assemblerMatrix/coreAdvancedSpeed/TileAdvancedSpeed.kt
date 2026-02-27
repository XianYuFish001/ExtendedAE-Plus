package com.extendedae_plus.common.registry.block.assemblerMatrix.coreAdvancedSpeed

import com.extendedae_plus.common.init.EAEPTiles
import com.extendedae_plus.mixin.helper.HelperAssemblerMatrixModifier
import com.fish.fishlib.util.extension.invoke
import com.glodblock.github.extendedae.common.me.matrix.ClusterAssemblerMatrix
import com.glodblock.github.extendedae.common.tileentities.matrix.TileAssemblerMatrixSpeed
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

class TileAdvancedSpeed(pos: BlockPos, blockState: BlockState) : TileAssemblerMatrixSpeed(pos, blockState) {
    override fun add(cluster: ClusterAssemblerMatrix) {
        for (i in 0..2) (cluster as HelperAssemblerMatrixModifier).`eaep$addSpeedCore`()
    }

    override fun getType(): BlockEntityType<TileAdvancedSpeed> = EAEPTiles.CoreAdvancedSpeed()
}
