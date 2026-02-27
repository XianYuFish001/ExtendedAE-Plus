package com.extendedae_plus.common.registry.menu

import appeng.api.util.IConfigManager
import appeng.core.definitions.AEItems
import appeng.menu.guisync.GuiSync
import appeng.menu.implementations.UpgradeableMenu
import com.extendedae_plus.common.init.EAEPMenuTypes
import com.extendedae_plus.common.init.EAEPSettings
import com.extendedae_plus.common.registry.part.ticker.EnergyExtractor
import com.extendedae_plus.common.registry.part.ticker.PartTicker
import com.extendedae_plus.common.registry.part.ticker.PartTicker.StateTicker
import com.fish.fishlib.util.extension.invoke
import it.unimi.dsi.fastutil.shorts.ShortSet
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.Slot
import net.minecraft.world.level.ItemLike

class MenuTicker(
    containerID: Int,
    invPlayer: Inventory,
    host: PartTicker
) : UpgradeableMenu<PartTicker>(
    EAEPMenuTypes.Ticker(),
    containerID,
    invPlayer,
    host
) {
    @GuiSync(701)
    var tickerState = StateTicker.Enabled
    @GuiSync(702)
    var stateEnergy = true
    @GuiSync(703)
    var costMultiplier = 1.0
    @GuiSync(704)
    var energyCost = 1.0

    var speedMultiplier = 1L
    var remainingRatio = 1.0
    var targetBlock: ItemLike? = null

    var refreshAction = { }

    init {
        host.setLogic(this)
    }

    fun recalculateCardsEffects() {
        this.speedMultiplier = EnergyExtractor.calculateMultiplier(this.upgrades)
        this.remainingRatio = EnergyExtractor.calculateEnergyRemainingRatio(
            this.upgrades.getInstalledUpgrades(AEItems.ENERGY_CARD)
        )
    }

    override fun loadSettingsFromHost(configManager: IConfigManager) {
        this.redStoneMode = configManager.getSetting(EAEPSettings.modeRedstoneOptional)
        this.tickerState = this.host.configManager.getSetting(EAEPSettings.stateTicker)
    }

    override fun onSlotChange(s: Slot) {
        super.onSlotChange(s)
        this.recalculateCardsEffects()
        this.refreshAction()
    }

    override fun onServerDataSync(updatedFields: ShortSet) {
        super.onServerDataSync(updatedFields)
        this.recalculateCardsEffects()
        this.refreshAction()
    }

    fun updateTargetBlock(targetBlock: ItemLike) {
        this.targetBlock = targetBlock
    }
}
