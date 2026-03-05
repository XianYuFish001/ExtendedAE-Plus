package com.extendedae_plus.integration.impl.point

import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.entity.BlockEntity

interface IntegrationMekanism {
    fun openGui(tile: BlockEntity, player: Player)
}