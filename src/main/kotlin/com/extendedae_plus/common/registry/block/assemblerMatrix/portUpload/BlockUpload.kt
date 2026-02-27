package com.extendedae_plus.common.registry.block.assemblerMatrix.portUpload

import appeng.menu.MenuOpener
import appeng.menu.locator.MenuLocators
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.init.EAEPMenuTypes
import com.fish.fishlib.util.extension.invoke
import com.glodblock.github.extendedae.common.blocks.matrix.BlockAssemblerMatrixBase
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.Property

class BlockUpload : BlockAssemblerMatrixBase<TileUpload>() {
    init {
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(PropertyLocked, false)
        )
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block?, BlockState?>) {
        super.createBlockStateDefinition(builder)
        builder.add(PropertyLocked)
    }

    override fun getPresentItem(): Item {
        return EAEPItems.PortUpload.get()
    }

    override fun openGui(tile: TileUpload, player: Player) {
        MenuOpener.open(
            EAEPMenuTypes.LabelLinkManageable(),
            player,
            MenuLocators.forBlockEntity(tile)
        )
    }

    companion object {
        @JvmField
        val PropertyLocked: Property<Boolean> = BooleanProperty.create("locked")
    }
}
