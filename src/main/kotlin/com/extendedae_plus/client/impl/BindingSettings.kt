package com.extendedae_plus.client.impl

import appeng.api.config.RedstoneMode
import appeng.api.config.Setting
import appeng.api.config.YesNo
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems
import com.extendedae_plus.common.init.EAEPSettings
import com.extendedae_plus.common.init.EAEPSettings.ValueEntry
import com.fish.fishlib.common.InitObject
import net.minecraft.world.item.Item
import net.neoforged.api.distmarker.Dist

object BindingSettings {
    private val Appearances = HashMap<ValueEntry, Appearance>()

    @InitObject(dist = [Dist.CLIENT])
    private fun bind() {
        Builder(EAEPSettings.stateTicker).bindAll(
            EAEPActionItems.TickerEnabled,
            EAEPActionItems.TickerDisabled,
            EAEPActionItems.TickerBlacklisted
        ).build()

        Builder(EAEPSettings.modeRedstoneOptional).bind(
            RedstoneMode.LOW_SIGNAL to EAEPActionItems.RedstoneLow,
            RedstoneMode.HIGH_SIGNAL to EAEPActionItems.RedstoneHigh,
            RedstoneMode.IGNORE to EAEPActionItems.RedstoneIgnore,
        ).build()

        Builder(EAEPSettings.smartBlocking).bindAll(
            EAEPActionItems.BlockingEnabled,
            EAEPActionItems.BlockingDisabled,
            EAEPActionItems.BlockingUnable
        ).build()

        Builder(EAEPSettings.smartDoubling).bind(
            YesNo.YES to EAEPActionItems.DoublingEnabled,
            YesNo.NO to EAEPActionItems.DoublingDisabled
        ).build()

        Builder(EAEPSettings.modeTransfer).bindAll(
            EAEPActionItems.MergeNone,
            EAEPActionItems.MergeAdjacency,
            EAEPActionItems.MergeIndependence
        ).build()
    }

    @JvmStatic
    fun <TEnum : Enum<TEnum>> findAppearance(setting: Setting<TEnum>, value: TEnum) =
        Appearances[ValueEntry(setting.name, value)]

    @JvmRecord
    data class Appearance(@JvmField val action: EAEPActionItems, @JvmField val item: Item?)

    private class Builder<TEnum : Enum<TEnum>>(private val setting: Setting<TEnum>) {
        private val entries = ArrayList<Pair<TEnum, Appearance>>()

        fun bindAll(vararg actions: EAEPActionItems) = also {
            val values = this.setting.values.first().javaClass.enumConstants
            require(values.size == actions.size) { "Unbound setting values" }

            actions.forEachIndexed { index, value ->
                this.entries += values[index] to Appearance(value, null)
            }
        }

        fun bind(vararg values: Pair<TEnum, EAEPActionItems>, item: List<Item>? = null) = also {
            require(item == null || item.size == values.size) { "Unbound item value" }
            values
                .mapIndexed { index, value ->
                    value.first to Appearance(value.second, item?.get(index))
                }
                .forEach(this.entries::add)
        }

        fun build() = this.entries.forEach { (setting, appearance) ->
            Appearances[ValueEntry(this.setting.name, setting)] = appearance
        }

    }
}