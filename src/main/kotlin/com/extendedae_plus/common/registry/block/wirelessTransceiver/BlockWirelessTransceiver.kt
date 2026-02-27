package com.extendedae_plus.common.registry.block.wirelessTransceiver

import appeng.block.AEBaseEntityBlock
import appeng.menu.MenuOpener
import appeng.menu.locator.MenuLocators
import com.extendedae_plus.common.init.EAEPMenuTypes
import com.fish.fishlib.util.extension.invoke
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.world.phys.BlockHitResult

class BlockWirelessTransceiver : AEBaseEntityBlock<TileWirelessTransceiver>(metalProps()) {
    init {
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(PropertyMaster, false)
                .setValue(PropertyPowered, false)
                .setValue(PropertyLocked, false)
        )
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(PropertyMaster, PropertyPowered, PropertyLocked)
    }

    public override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        val blockEntity = level.getBlockEntity(pos) ?: return super.useWithoutItem(state, level, pos, player, hitResult)

        if (level.isClientSide()) return InteractionResult.SUCCESS

        MenuOpener.open(
            EAEPMenuTypes.LabelLinkManageable(), player,
            MenuLocators.forBlockEntity(blockEntity)
        )
        return InteractionResult.CONSUME
    }

    companion object {
        @JvmField
        val PropertyMaster: Property<Boolean> = BooleanProperty.create("master_mode")
        @JvmField
        val PropertyPowered: Property<Boolean> = BooleanProperty.create("powered")
        @JvmField
        val PropertyLocked: Property<Boolean> = BooleanProperty.create("locked")
    }
}
