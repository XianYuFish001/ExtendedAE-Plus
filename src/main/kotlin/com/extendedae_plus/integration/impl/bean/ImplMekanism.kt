package com.extendedae_plus.integration.impl.bean

import com.extendedae_plus.integration.impl.point.IntegrationMekanism
import com.fish.fishlib.integration.BeanIntegration
import com.fish.fishlib.util.extension.unit
import mekanism.common.tile.base.TileEntityMekanism
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.entity.BlockEntity

@BeanIntegration("mekanism")
object ImplMekanism : IntegrationMekanism {
    override fun openGui(tile: BlockEntity, player: Player) =
        (tile as? TileEntityMekanism)?.openGui(player).unit()
}