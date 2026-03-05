package com.extendedae_plus.common.init

import appeng.api.config.RedstoneMode
import appeng.api.config.Setting
import appeng.api.config.YesNo
import com.extendedae_plus.common.registry.part.ticker.PartTicker.StateTicker
import com.extendedae_plus.common.registry.settings.ModeEncodingTransfer
import com.extendedae_plus.common.registry.settings.StateSmartBlocking
import java.util.*

/** 使用EAEPCycleButton喵, 使用EAEPCycleButton谢谢喵 */
object EAEPSettings {
    @JvmField
    val Settings = HashMap<String, Setting<*>>()

    @JvmField
    val stateTicker = register(
        "state_ticker",
        StateTicker::class.java,
        StateTicker.Blacklisted
    )

    @JvmField
    val modeRedstoneOptional = register(
        "optional_redstone_mode",
        RedstoneMode::class.java,
        RedstoneMode.SIGNAL_PULSE
    )

    @JvmField
    val smartBlocking = register(
        "smart_blocking",
        StateSmartBlocking::class.java
    )

    @JvmField
    val smartDoubling = register(
        "smart_doubling",
        YesNo::class.java,
        YesNo.UNDECIDED
    )

    @JvmField
    val modeTransfer = register(
        "transfer_mode",
        ModeEncodingTransfer::class.java
    )

    private fun <TEnum : Enum<TEnum>> register(
        name: String, clazzSetting: Class<TEnum>, vararg valueInvalid: TEnum
    ): Setting<TEnum> {
        val values = EnumSet.allOf(clazzSetting)
        values.removeIf(valueInvalid::contains)
        val setting = Setting(name, clazzSetting, values)
        Settings[name] = setting
        return setting
    }

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
}
