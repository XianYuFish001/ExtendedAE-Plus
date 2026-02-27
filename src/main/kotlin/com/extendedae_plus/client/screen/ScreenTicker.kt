package com.extendedae_plus.client.screen

import appeng.api.config.RedstoneMode
import appeng.client.gui.implementations.UpgradeableScreen
import appeng.client.gui.style.ScreenStyle
import appeng.client.gui.widgets.CommonButtons
import appeng.client.gui.widgets.ServerSettingToggleButton
import appeng.client.gui.widgets.SettingToggleButton
import appeng.core.network.serverbound.ConfigButtonPacket
import appeng.util.Platform
import com.extendedae_plus.common.init.EAEPItems
import com.extendedae_plus.common.init.EAEPSettings
import com.extendedae_plus.common.registry.menu.MenuTicker
import com.extendedae_plus.common.registry.part.ticker.PartTicker.StateTicker
import com.extendedae_plus.util.UtilGui.renderFakeItemScalable
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.keyBuilder.Patterns
import com.fish.fishlib.util.keyBuilder.getMap
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.network.PacketDistributor

class ScreenTicker(
    menu: MenuTicker, playerInventory: Inventory, title: Component, style: ScreenStyle
) : UpgradeableScreen<MenuTicker>(menu, playerInventory, title, style) {
    private val buttonStateSwitcher: SettingToggleButton<StateTicker>
    private val buttonRSMode: SettingToggleButton<RedstoneMode>

    init {
        menu.refreshAction = { this.textTooltip() }

        this.buttonStateSwitcher = SettingToggleButton(
            EAEPSettings.stateTicker, StateTicker.Enabled,
            { button, reversed ->
                if (StateTicker.Blacklisted == this.menu.tickerState) return@SettingToggleButton
                PacketDistributor.sendToServer(ConfigButtonPacket(
                    button.setting, reversed
                ))
            }
        )
        this.buttonRSMode = ServerSettingToggleButton(
            EAEPSettings.modeRedstoneOptional, RedstoneMode.IGNORE
        )

        this.addToLeftToolbar(this.buttonStateSwitcher)
        this.addToLeftToolbar(this.buttonRSMode)
        this.addToLeftToolbar(CommonButtons.togglePowerUnit())
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks)

        guiGraphics.renderFakeItemScalable(
            ItemStack(this.menu.targetBlock ?: return),
            this.leftPos + 92,
            this.topPos + 28,
            4f
        )
    }

    override fun updateBeforeRender() {
        super.updateBeforeRender()

        this.buttonStateSwitcher.set(this.menu.tickerState)
        this.buttonRSMode.set(this.menu.redStoneMode)

        this.textTooltip()
    }

    private fun textTooltip() {
        val speedMultiplier: Long
        val energyCost: Double
        val remainingRatio: Double
        val costMultiplier: Double
        val blacklisted = this.menu.tickerState == StateTicker.Blacklisted
        if (blacklisted) {
            speedMultiplier = 1L
            energyCost = 0.0
            remainingRatio = 1.0
            costMultiplier = 1.0
        } else {
            speedMultiplier = this.menu.speedMultiplier
            energyCost = this.menu.energyCost
            remainingRatio = this.menu.remainingRatio
            costMultiplier = this.menu.costMultiplier
        }

        val builder = UtilKeyBuilder.of(Patterns.Screen)
            .item(EAEPItems.Ticker)
            .newHashMap<String>()

        builder.addStr(
            when (this.menu.tickerState) {
                StateTicker.Blacklisted -> "blacklisted"
                StateTicker.Disabled -> "disabled"
                StateTicker.Enabled -> {
                    if (this.menu.stateEnergy) "enabled" else "needs_energy"
                }
            }
        ).buildInto("state", plain = true)
        builder.args(speedMultiplier)
            .buildInto("speed_multiplier")
        builder.args(Platform.formatPower(energyCost, false))
            .buildInto("energy_cost")
        builder.args("%.2f%%".format(remainingRatio))
            .buildInto("power_ratio")
        builder.args("%.2fx".format(costMultiplier))
            .buildInto("cost_multiplier")

        builder.getMap()?.forEach(this::setTextContent)
    }
}
