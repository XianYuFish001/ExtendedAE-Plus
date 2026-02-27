package com.extendedae_plus.common.init

import appeng.api.config.RedstoneMode
import appeng.api.config.Setting
import appeng.api.config.YesNo
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems
import com.extendedae_plus.common.registry.part.ticker.PartTicker.StateTicker
import com.extendedae_plus.common.registry.settings.ModeEncodingTransfer
import com.extendedae_plus.common.registry.settings.StateSmartBlocking
import net.minecraft.world.item.Item
import java.util.*

/** 使用EAEPCycleButton喵, 使用EAEPCycleButton谢谢喵 */
object EAEPSettings {
    @JvmField
    val Settings = HashMap<String, Setting<*>>()
    private val Appearances = HashMap<ValueEntry, ButtonAppearance>()

    @JvmField
    val stateTicker = register("state_ticker", StateTicker::class.java)
        .bindAll(
            EAEPActionItems.TickerEnabled,
            EAEPActionItems.TickerDisabled,
            EAEPActionItems.TickerBlacklisted
        ).setInvalidValue(StateTicker.Blacklisted)
        .build()
    @JvmField
    val modeRedstoneOptional = register("optional_redstone_mode", RedstoneMode::class.java)
            .addPart(RedstoneMode.IGNORE, EAEPActionItems.RedstoneIgnore)
            .addPart(RedstoneMode.LOW_SIGNAL, EAEPActionItems.RedstoneLow)
            .addPart(RedstoneMode.HIGH_SIGNAL, EAEPActionItems.RedstoneHigh)
            .build()
    @JvmField
    val smartBlocking = register("smart_blocking", StateSmartBlocking::class.java)
            .bindAll(
                EAEPActionItems.BlockingEnabled,
                EAEPActionItems.BlockingDisabled,
                EAEPActionItems.BlockingUnable
            ).build()
    @JvmField
    val smartDoubling = register("smart_doubling", YesNo::class.java)
        .addPart(YesNo.YES, EAEPActionItems.DoublingEnabled)
        .addPart(YesNo.NO, EAEPActionItems.DoublingDisabled)
        .build()
    @JvmField
    val modeTransfer = register("transfer_mode", ModeEncodingTransfer::class.java)
            .bindAll(
                EAEPActionItems.MergeNone,
                EAEPActionItems.MergeAdjacency,
                EAEPActionItems.MergeIndependence
            ).build()

    private fun <TEnum : Enum<TEnum>> register(name: String, clazzSetting: Class<TEnum>) = Builder(name, clazzSetting)

    @JvmStatic
    fun <TEnum : Enum<TEnum>> findAppearance(setting: Setting<TEnum>, value: TEnum) =
        Appearances[ValueEntry(setting.name, value)]

    @JvmRecord
    data class ValueEntry(val setting: String, val value: Enum<*>) {
        override fun hashCode() = Objects.hash(this.setting, this.value)

        override fun equals(obj: Any?): Boolean {
            if (obj == null) return false
            if (this.javaClass != obj.javaClass) return false
            val other = obj as ValueEntry
            return other.setting == this.setting && other.value === this.value
        }
    }

    @JvmRecord
    data class ButtonAppearance(@JvmField val action: EAEPActionItems, @JvmField val item: Item?)

    private class Builder<TEnum : Enum<TEnum>>(private val name: String, private val clazzSetting: Class<TEnum>) {
        private val entryPairs = ArrayList<Pair<TEnum, ButtonAppearance>>()
        private var invalidValues: EnumSet<TEnum>? = null

        fun bindAll(vararg actions: EAEPActionItems): Builder<TEnum> {
            val values = EnumSet.allOf<TEnum>(this.clazzSetting)
            require(values.size == actions.size) { "Found unbound setting values" }

            for ((index, value) in values.withIndex()) {
                this.addPart(value, actions[index])
            }
            return this
        }

        fun addPart(value: TEnum, action: EAEPActionItems, item: Item? = null): Builder<TEnum> {
            this.entryPairs.add(Pair(value, ButtonAppearance(action, item)))
            return this
        }

        @SafeVarargs
        fun setInvalidValue(vararg invalidValues: TEnum): Builder<TEnum> {
            this.invalidValues = EnumSet.noneOf(this.clazzSetting)
            this.invalidValues?.addAll(invalidValues)
            return this
        }

        fun build(): Setting<TEnum> {
            val boundValues = EnumSet.noneOf<TEnum>(this.clazzSetting)
            this.entryPairs.forEach { entry ->
                Appearances[ValueEntry(this.name, entry.first)] = entry.second
                boundValues.add(entry.first)
            }
            this.invalidValues?.let { boundValues.removeAll(it) }

            val setting = Setting<TEnum>(this.name, this.clazzSetting, boundValues)
            Settings[this.name] = setting

            return setting
        }
    }
}
