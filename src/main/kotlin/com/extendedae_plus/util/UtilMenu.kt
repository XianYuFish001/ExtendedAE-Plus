package com.extendedae_plus.util

import appeng.blockentity.networking.CableBusBlockEntity
import com.extendedae_plus.integration.helper.ManagerIntegration
import com.extendedae_plus.integration.impl.point.IntegrationMekanism
import com.fish.fishlib.util.extension.cast
import com.fish.fishlib.util.extension.ifTrue
import com.fish.fishlib.util.extension.unit
import com.glodblock.github.extendedae.common.blocks.BlockBaseGui
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

object UtilMenu {
    fun open(
        level: Level,
        pos: BlockPos,
        side: Direction,
        player: Player
    ): Boolean {
        val state = level.getBlockState(pos) ?: return false
        val block = state.block
        val tile = level.getBlockEntity(pos) ?: return false

        when (block) {
            is BlockBaseGui<*> -> block.openGui(tile.cast(), player).unit(true)
            is MenuProvider -> player.openMenu(block, pos).isPresent
            else -> false
        }.ifTrue { return true }

        val mek = ManagerIntegration<IntegrationMekanism>()
        return when {
            tile is MenuProvider -> player.openMenu(tile, pos).isPresent
            mek?.hasGui(tile) == true -> mek.openGui(tile, player) == InteractionResult.CONSUME
            tile is CableBusBlockEntity -> tile
                .getPart(side.opposite)
                ?.onUseWithoutItem(player, Vec3.atCenterOf(pos).relative(side, 0.6))
                ?: false
            else -> false
        }
    }
}