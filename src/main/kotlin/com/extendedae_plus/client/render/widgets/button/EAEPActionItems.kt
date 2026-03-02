package com.extendedae_plus.client.render.widgets.button

import appeng.client.gui.Icon
import appeng.core.localization.ButtonToolTips
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.keyBuilder.Patterns
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

enum class EAEPActionItems(builder: (Builder) -> Builder) {
    BackingOut({ it.icon(Icon.TOOLBAR_BUTTON_BACKGROUND) }),

    Mul2({ it.icon(EAEPIcon.Mul2).group("scaling").toggleName() }),
    Div2({ it.icon(EAEPIcon.Div2).group("scaling").toggleName() }),
    Mul3({ it.icon(EAEPIcon.Mul3).group("scaling").toggleName() }),
    Div3({ it.icon(EAEPIcon.Div3).group("scaling").toggleName() }),
    Mul5({ it.icon(EAEPIcon.Mul5).group("scaling").toggleName() }),
    Div5({ it.icon(EAEPIcon.Div5).group("scaling").toggleName() }),

    BlockingDisabled({ it.icon(Icon.BLOCKING_MODE_NO).group("smart_blocking").tooltip("disabled") }),
    BlockingEnabled({ it.icon(EAEPIcon.BlockingTransparent).group("smart_blocking").tooltip("enabled") }),
    BlockingUnable({ it.icon(Icon.ARROW_RIGHT).group("smart_blocking").tooltip("disabled_by_super") }),

    DoublingDisabled({ it.icon(EAEPIcon.PatternSingle).group("smart_doubling").tooltip("disabled") }),
    DoublingEnabled({ it.icon(EAEPIcon.PatternMulti).group("smart_doubling").tooltip("enabled") }),

    TickerEnabled({ it.icon(Icon.AUTO_EXPORT_ON).group("state_ticker").tooltip("enabled") }),
    TickerDisabled({ it.icon(Icon.AUTO_EXPORT_OFF).group("state_ticker").tooltip("disabled") }),
    TickerBlacklisted({ it.icon(Icon.INVALID).group("state_ticker").tooltip("blacklisted") }),

    RedstoneIgnore({
        it.icon(Icon.REDSTONE_IGNORE).group("redstone_mode")
            .name(ButtonToolTips.RedstoneMode.text()).tooltip(ButtonToolTips.AlwaysActive.text())
    }),
    RedstoneLow({
        it.icon(Icon.REDSTONE_LOW).group("redstone_mode")
            .name(ButtonToolTips.RedstoneMode.text()).tooltip(ButtonToolTips.ActiveWithoutSignal.text())
    }),
    RedstoneHigh({
        it.icon(Icon.REDSTONE_HIGH).group("redstone_mode")
            .name(ButtonToolTips.RedstoneMode.text()).tooltip(ButtonToolTips.ActiveWithSignal.text())
    }),

    PriorityKeep({ it.icon(EAEPIcon.SaveCenter).group("priority_tool").tooltip("keep") }),
    PriorityIncrement({ it.icon(EAEPIcon.SaveUp).group("priority_tool").tooltip("increment") }),
    PriorityDecrement({ it.icon(EAEPIcon.SaveDown).group("priority_tool").tooltip("decrement") }),

    AliasAdd({ it.icon(EAEPIcon.SaveUp).group("recipe_alias").tooltip("add") }),
    AliasRemove({ it.icon(EAEPIcon.SaveDown).group("recipe_alias").tooltip("remove") }),

    RowSlotsVisible({ it.icon(EAEPIcon.ListWithChildren).group("row_slots_visible").tooltip("visible") }),
    RowSlotsInvisible({ it.icon(EAEPIcon.ListMulti).group("row_slots_visible").tooltip("invisible") }),

    LabelFrequency({ it.icon(EAEPIcon.CharF).group("label_type").tooltip("frequency") }),
    LabelLabel({ it.icon(EAEPIcon.CharL).group("label_type").tooltip("label") }),
    LabelPublic({ it.icon(EAEPIcon.Connected).group("label_mode").tooltip("public") }),
    LabelPrivate({ it.icon(EAEPIcon.Disconnected).group("label_mode").tooltip("private") }),
    LabelAdd({ it.icon(Icon.ENTER).group("label_add").name() }),
    LabelLocked({ it.icon(Icon.LOCKED).group("label_locked").tooltip("locked") }),
    LabelUnlocked({ it.icon(Icon.UNLOCKED).group("label_locked").tooltip("unlocked") }),

    TransceiverMaster({ it.icon(EAEPIcon.SignalSend).group("transceiver_mode").tooltip("master") }),
    TransceiverSlave({ it.icon(EAEPIcon.SignalReceive).group("transceiver_mode").tooltip("slave") }),

    MergeNone({ it.icon(EAEPIcon.MergeNone).group("transfer_mode").tooltip("none") }),
    MergeAdjacency({ it.icon(EAEPIcon.MergeAdjacency).group("transfer_mode").tooltip("merge_adjacency") }),
    MergeIndependence({ it.icon(Icon.INSCRIBER_SEPARATE_SIDES).group("transfer_mode").tooltip("independence") }),

    PatternUpload({ it.icon(Icon.ARROW_UP).group("pattern_upload").name() }),
    ;

    val icon: IButtonIcon
    @JvmField
    val text: Component
    @JvmField
    val tooltip: Component?
    val group: String

    init {
        val info = builder(Builder())
        this.icon = info.icon
        this.group = info.actionGroup
        this.text = if (info.nameVisible) info.name else Component.empty()
        this.tooltip = if (info.tooltipVisible) info.tooltip else null
    }

    val iconBlitter get() = icon.blitter

    val aeIcon get() = icon.aeIcon

    companion object {
        @JvmField
        val actions = HashMap<String, MutableList<EAEPActionItems>>()

        init {
            for (action in entries) {
                if (!action.group.isEmpty()) actions.computeIfAbsent(
                    action.group
                ) { ArrayList() } += action
            }
        }
    }

    private class Builder {
        var icon = EAEPIcon.fromAEIcon(Icon.TOOLBAR_BUTTON_BACKGROUND)
        var actionGroup = ""
        var name: Component = Component.empty()
        var nameVisible = true
        var tooltip: MutableComponent? = null
        var tooltipVisible = true

        fun icon(icon: IButtonIcon): Builder {
            this.icon = icon
            return this
        }

        fun icon(icon: Icon): Builder {
            return this.icon(EAEPIcon.fromAEIcon(icon))
        }

        fun group(group: String): Builder {
            this.actionGroup = group
            return this
        }

        fun name(name: Component): Builder {
            this.name = name
            return this
        }

        fun name(vararg name: String): Builder {
            val builder = UtilKeyBuilder.of(Patterns.ScreenTooltip)
            if (name.isNotEmpty()) for (key in name) builder.addStr(key)
            else builder.addStr(this.actionGroup)
            this.name = builder.build()
            return this
        }

        fun tooltip(tooltip: MutableComponent): Builder {
            if (this.name.string.isEmpty()) this.name()
            this.tooltip = tooltip
            return this
        }

        fun tooltip(vararg tooltip: String): Builder {
            if (this.name.string.isEmpty()) this.name()
            val builder = UtilKeyBuilder.of(Patterns.ScreenTooltip)
                .addStr(this.actionGroup)
            for (key in tooltip) builder.addStr(key)
            if (this.tooltip != null) this.tooltip!!.append(builder.build())
            else this.tooltip = builder.build()
            return this
        }

        fun toggleName(): Builder {
            this.nameVisible = !this.nameVisible
            if (!this.nameVisible) this.tooltipVisible = false
            return this
        }

        fun toggleTooltip(): Builder {
            this.tooltipVisible = this.nameVisible && !this.tooltipVisible
            return this
        }
    }
}
