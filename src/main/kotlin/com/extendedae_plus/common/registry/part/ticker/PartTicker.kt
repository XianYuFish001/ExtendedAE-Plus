package com.extendedae_plus.common.registry.part.ticker

import appeng.api.config.RedstoneMode
import appeng.api.networking.IGridNode
import appeng.api.networking.ticking.IGridTickable
import appeng.api.networking.ticking.TickRateModulation
import appeng.api.networking.ticking.TickingRequest
import appeng.api.parts.IPartCollisionHelper
import appeng.api.parts.IPartItem
import appeng.api.util.IConfigManagerBuilder
import appeng.items.parts.PartModels
import appeng.menu.MenuOpener
import appeng.menu.locator.MenuLocators
import appeng.parts.PartModel
import appeng.parts.automation.UpgradeablePart
import com.extendedae_plus.ExtendedAEPlus
import com.extendedae_plus.common.init.EAEPMenuTypes
import com.extendedae_plus.common.init.EAEPSettings
import com.extendedae_plus.common.registry.menu.MenuTicker
import com.extendedae_plus.common.registry.part.ticker.EnergyExtractor.calculateAndExtractEnergy
import com.extendedae_plus.common.registry.part.ticker.ParserTickerConfig.isBlockBlacklisted
import com.fish.fishlib.util.extension.invoke
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

class PartTicker(partItem: IPartItem<*>) : UpgradeablePart(partItem) {
    private var logic: MenuTicker? = null

    private var speedMultiplier = 1L
    private var costMultiplier = 1.0
    private var energyCost = 1.0

    init {
        this.mainNode
            .setIdlePowerUsage(1.0)
            .addService(IGridTickable::class.java, TickerTicker())
    }

    override fun registerSettings(builder: IConfigManagerBuilder) {
        super.registerSettings(builder)
        builder.registerSetting(EAEPSettings.stateTicker, StateTicker.Enabled)
        builder.registerSetting(EAEPSettings.modeRedstoneOptional, RedstoneMode.IGNORE)
    }

    private fun prepareTick(tile: BlockEntity) {
        if (this.speedMultiplier <= 1) return
        if (this.configManager.getSetting(EAEPSettings.stateTicker) != StateTicker.Enabled) return
        if (this.gridNode == null) return

        if (
            when (this.configManager.getSetting(EAEPSettings.modeRedstoneOptional)) {
                RedstoneMode.LOW_SIGNAL -> this.host.hasRedstone()
                RedstoneMode.HIGH_SIGNAL -> !this.host.hasRedstone()
                else -> false
            }
        ) return

        val ticker = tile.blockState.getTicker(this.level, tile.type) ?: return

        val extracted = calculateAndExtractEnergy(this, this.speedMultiplier, this.costMultiplier)
        this.logic?.stateEnergy = extracted
        if (!extracted) return

        this.applyTick(tile, ticker)
    }

    @Suppress("unchecked_cast")
    private fun <TBlockEntity : BlockEntity> applyTick(blockEntity: TBlockEntity, ticker: BlockEntityTicker<*>) {
        val level = blockEntity.getLevel() ?: return
        for (i in 0..<this.speedMultiplier)
                (ticker as BlockEntityTicker<TBlockEntity>)
                    .tick(level, blockEntity.blockPos, blockEntity.blockState, blockEntity)
    }

    override fun readFromNBT(extra: CompoundTag, registries: HolderLookup.Provider) {
        super.readFromNBT(extra, registries)
        this.recalculateAll()
    }

    override fun upgradesChanged() = this.recalculateAll()

    override fun addToWorld() {
        super.addToWorld()

        val level = this.level
        val blockEntity = this.blockEntity
        val side = this.side
        if (level.isClientSide() || blockEntity == null || side == null) return

        this.changeTarget(level.getBlockState(blockEntity.blockPos.relative(side)))
    }

    override fun onNeighborChanged(level: BlockGetter, pos: BlockPos, neighbor: BlockPos) {
        if (this.side == null) return
        if (pos.relative(this.side) != neighbor) return

        this.changeTarget(level.getBlockState(neighbor))
    }

    private fun changeTarget(target: BlockState) {
        if (target.isAir) {
            this.costMultiplier = 1.0
            this.configManager.putSetting(EAEPSettings.stateTicker, StateTicker.Disabled)
        } else {
            this.costMultiplier = ParserTickerConfig.getBlockExternalMultiplier(target)
            if (isBlockBlacklisted(target)) this.configManager
                .putSetting(EAEPSettings.stateTicker, StateTicker.Blacklisted)
        }
        this.recalculateEnergyCost()
        this.logic?.let {
            it.costMultiplier = this.costMultiplier
            it.updateTargetBlock(target.block)
        }
    }

    override fun getRSMode(): RedstoneMode =
        this.configManager.getSetting(EAEPSettings.modeRedstoneOptional)

    private fun recalculateAll() {
        this.speedMultiplier = EnergyExtractor.calculateMultiplier(this.upgrades)
        this.recalculateEnergyCost()
        this.logic?.energyCost = this.energyCost
    }

    private fun recalculateEnergyCost() {
        this.energyCost = EnergyExtractor.calculateEnergyCost(
            this.upgrades,
            this.speedMultiplier,
            this.costMultiplier
        )
    }

    override fun onUseWithoutItem(player: Player, pos: Vec3): Boolean {
        if (player.level().isClientSide()) return true
        return MenuOpener.open(EAEPMenuTypes.Ticker(), player, MenuLocators.forPart(this))
    }

    fun setLogic(logic: MenuTicker) {
        this.logic = logic
        logic.energyCost = this.energyCost
        logic.costMultiplier = this.costMultiplier
        logic.recalculateCardsEffects()

        val face = this.side ?: return
        logic.updateTargetBlock(
            this.level.getBlockState(
                this.blockEntity.blockPos.relative(face)
            ).block
        )
    }

    override fun getStaticModels() = when {
        this.isActive && this.isPowered -> ModelNetworked
        this.isPowered -> ModelOn
        else -> ModelOff
    }

    override fun getUpgradeSlots() = 8

    override fun getBoxes(collisionHelper: IPartCollisionHelper) {
        collisionHelper.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0)
        collisionHelper.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 14.0)
    }

    private inner class TickerTicker : IGridTickable {
        override fun getTickingRequest(node: IGridNode) =
            TickingRequest(1, 1, false)

        override fun tickingRequest(node: IGridNode, ticksSinceLastCall: Int): TickRateModulation {
            if (side == null) return TickRateModulation.SLEEP
            if (configManager.getSetting(EAEPSettings.stateTicker) != StateTicker.Enabled) return TickRateModulation.SLEEP

            val targetBlockEntity = level.getBlockEntity(blockEntity.blockPos.relative(side))
            if (targetBlockEntity == null || !isActive) return TickRateModulation.SLOWER

            prepareTick(targetBlockEntity)
            return TickRateModulation.IDLE
        }
    }

    enum class StateTicker {
        Enabled, Disabled, Blacklisted
    }

    companion object {
        val LocationModelBase = ExtendedAEPlus.getLocation("part/ticker_base")

        @PartModels
        val ModelOff: PartModel = PartModel(
            LocationModelBase,
            ExtendedAEPlus.getLocation("part/ticker_off")
        )

        @PartModels
        val ModelOn: PartModel = PartModel(
            LocationModelBase,
            ExtendedAEPlus.getLocation("part/ticker_on")
        )

        @PartModels
        val ModelNetworked: PartModel = PartModel(
            LocationModelBase,
            ExtendedAEPlus.getLocation("part/ticker_formed")
        )
    }
}
