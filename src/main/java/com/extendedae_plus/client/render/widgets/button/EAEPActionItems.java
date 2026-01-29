package com.extendedae_plus.client.render.widgets.button;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.core.localization.ButtonToolTips;
import com.extendedae_plus.util.UtilKeyBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public enum EAEPActionItems {
    backingOut(builder -> builder.icon(Icon.TOOLBAR_BUTTON_BACKGROUND)),

    mul2(builder -> builder.icon(EAEPIcon.mul2).group("scaling").toggleName()),
    div2(builder -> builder.icon(EAEPIcon.div2).group("scaling").toggleName()),
    mul3(builder -> builder.icon(EAEPIcon.mul3).group("scaling").toggleName()),
    div3(builder -> builder.icon(EAEPIcon.div3).group("scaling").toggleName()),
    mul5(builder -> builder.icon(EAEPIcon.mul5).group("scaling").toggleName()),
    div5(builder -> builder.icon(EAEPIcon.div5).group("scaling").toggleName()),

    blockingDisabled(builder -> builder.icon(Icon.BLOCKING_MODE_NO).group("smart_blocking").tooltip("disabled")),
    blockingEnabled(builder -> builder.icon(EAEPIcon.blockingTransparent).group("smart_blocking").tooltip("enabled")),
    blockingUnable(builder -> builder.icon(Icon.ARROW_RIGHT).group("smart_blocking").tooltip("disabled_by_super")),

    doublingDisabled(builder -> builder.icon(EAEPIcon.patternSingle).group("smart_doubling").tooltip("disabled")),
    doublingEnabled(builder -> builder.icon(EAEPIcon.patternMulti).group("smart_doubling").tooltip("enabled")),

    tickerEnabled(builder -> builder.icon(Icon.AUTO_EXPORT_ON).group("state_ticker").tooltip("enabled")),
    tickerDisabled(builder -> builder.icon(Icon.AUTO_EXPORT_OFF).group("state_ticker").tooltip("disabled")),
    tickerBlacklisted(builder -> builder.icon(Icon.INVALID).group("state_ticker").tooltip("blacklisted")),

    redstoneIgnore(builder -> builder.icon(Icon.REDSTONE_IGNORE).group("redstone_mode")
            .name(ButtonToolTips.RedstoneMode.text()).tooltip(ButtonToolTips.AlwaysActive.text())),
    redstoneLow(builder -> builder.icon(Icon.REDSTONE_LOW).group("redstone_mode")
            .name(ButtonToolTips.RedstoneMode.text()).tooltip(ButtonToolTips.ActiveWithoutSignal.text())),
    redstoneHigh(builder -> builder.icon(Icon.REDSTONE_HIGH).group("redstone_mode")
            .name(ButtonToolTips.RedstoneMode.text()).tooltip(ButtonToolTips.ActiveWithSignal.text())),

    priorityKeep(builder -> builder.icon(EAEPIcon.saveCenter).group("priority_tool").tooltip("keep")),
    priorityIncrement(builder -> builder.icon(EAEPIcon.saveUp).group("priority_tool").tooltip("increment")),
    priorityDecrement(builder -> builder.icon(EAEPIcon.saveDown).group("priority_tool").tooltip("decrement")),

    aliasAdd(builder -> builder.icon(EAEPIcon.saveUp).group("recipe_alias").tooltip("add")),
    aliasRemove(builder -> builder.icon(EAEPIcon.saveDown).group("recipe_alias").tooltip("remove")),

    rowSlotsVisible(builder -> builder.icon(EAEPIcon.listWithChildren).group("row_slots_visible").tooltip("visible")),
    rowSlotsInvisible(builder -> builder.icon(EAEPIcon.listMulti).group("row_slots_visible").tooltip("invisible")),

    labelFrequency(builder -> builder.icon(EAEPIcon.charF).group("label_type").tooltip("frequency")),
    labelLabel(builder -> builder.icon(EAEPIcon.charL).group("label_type").tooltip("label")),
    labelPublic(builder -> builder.icon(EAEPIcon.connected).group("label_mode").tooltip("public")),
    labelPrivate(builder -> builder.icon(EAEPIcon.disconnected).group("label_mode").tooltip("private")),
    labelAdd(builder -> builder.icon(Icon.ENTER).group("label_add").name()),
    labelLocked(builder -> builder.icon(Icon.LOCKED).group("label_locked").tooltip("locked")),
    labelUnlocked(builder -> builder.icon(Icon.UNLOCKED).group("label_locked").tooltip("unlocked")),

    transceiverMaster(builder -> builder.icon(EAEPIcon.signalSend).group("transceiver_mode").tooltip("master")),
    transceiverSlave(builder -> builder.icon(EAEPIcon.signalReceive).group("transceiver_mode").tooltip("slave")),

    mergeNone(builder -> builder.icon(EAEPIcon.mergeNone).group("transfer_mode").tooltip("none")),
    mergeAdjacency(builder -> builder.icon(EAEPIcon.mergeAdjacency).group("transfer_mode").tooltip("merge_adjacency")),
    mergeIndependence(builder -> builder.icon(Icon.INSCRIBER_SEPARATE_SIDES).group("transfer_mode").tooltip("independence")),
    
    patternUpload(builder -> builder.icon(Icon.ARROW_UP).group("pattern_upload").name()),

    ;

    private final IButtonIcon icon;
    private final Component name;
    private final @Nullable Component tooltip;
    private final String actionGroup;

    public static final Map<String, List<EAEPActionItems>> actions = new HashMap<>();

    static {
        for (var action : EAEPActionItems.values()) {
            if (!action.actionGroup.isEmpty())
                actions.computeIfAbsent(action.actionGroup,
                        $ -> new ArrayList<>()).add(action);
        }
    }

    EAEPActionItems(UnaryOperator<Builder> builder) {
        var info = builder.apply(new Builder());
        this.icon = info.icon;
        this.actionGroup = info.actionGroup;
        this.name = info.nameVisible ? info.name : Component.empty();
        this.tooltip = info.tooltipVisible ? info.tooltip : null;
    }

    public Blitter getIconBlitter() {
        if (icon != null) return icon.getBlitter();
        else return Icon.INVALID.getBlitter();
    }

    public String getGroup() {
        return actionGroup;
    }

    public IButtonIcon getIcon() {
        return icon;
    }

    public Icon getAEIcon() {
        return icon.getAEIcon();
    }

    public Component getName() {
        return name;
    }

    public @Nullable Component getTooltip() {
        return tooltip;
    }

    private static class Builder {
        private IButtonIcon icon = EAEPIcon.fromAEIcon(Icon.TOOLBAR_BUTTON_BACKGROUND);
        private String actionGroup = "";
        private Component name = Component.empty();
        private boolean nameVisible = true;
        private Component tooltip;
        private boolean tooltipVisible = true;

        private Builder icon(IButtonIcon icon) {
            this.icon = icon;
            return this;
        }

        private Builder icon(Icon icon) {
            return this.icon(EAEPIcon.fromAEIcon(icon));
        }

        private Builder group(String group) {
            this.actionGroup = group;
            return this;
        }

        private Builder name(Component name) {
            this.name = name;
            return this;
        }

        private Builder name(String... name) {
            var builder = UtilKeyBuilder.of(UtilKeyBuilder.screenTooltip);
            if (name.length > 0) for (var key : name) builder.addStr(key);
            else builder.addStr(this.actionGroup);
            this.name = builder.build();
            return this;
        }

        private Builder tooltip(Component tooltip) {
            if (this.name.getString().isEmpty()) this.name();
            this.tooltip = tooltip;
            return this;
        }

        private Builder tooltip(String... tooltip) {
            if (this.name.getString().isEmpty()) this.name();
            var builder = UtilKeyBuilder.of(UtilKeyBuilder.screenTooltip)
                    .addStr(this.actionGroup);
            for (var key : tooltip) builder.addStr(key);
            if (this.tooltip != null) ((MutableComponent) this.tooltip).append(builder.build());
            else this.tooltip = builder.build();
            return this;
        }

        private Builder toggleName() {
            this.nameVisible = !this.nameVisible;
            if (!this.nameVisible) this.tooltipVisible = false;
            return this;
        }

        private Builder toggleTooltip() {
            this.tooltipVisible = this.nameVisible && !this.tooltipVisible;
            return this;
        }
    }
}
