package com.extendedae_plus.integration.impl.bean

import com.extendedae_plus.integration.impl.point.IntegrationMekanism
import com.fish.fishlib.integration.BeanIntegration
import mekanism.common.tile.base.TileEntityMekanism
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.entity.BlockEntity

@BeanIntegration("mekanism")
object ImplMekanism : IntegrationMekanism {
    override fun openGui(tile: BlockEntity, player: Player) =
        (tile as? TileEntityMekanism)?.openGui(player) ?: InteractionResult.PASS

    override fun hasGui(tile: BlockEntity) =
        (tile as? TileEntityMekanism)?.hasGui() ?: false
}