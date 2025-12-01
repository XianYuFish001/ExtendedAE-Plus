package com.extendedae_plus.client.render.widgets.button;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.core.localization.ButtonToolTips;
import com.extendedae_plus.util.UtilKeyBuilder;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum EAEPActionItems {
    BACKING_OUT(EAEPIcon.fromAEIcon(Icon.INVALID), Component.empty(), Component.empty(), ""),
    MUL2(EAEPIcon.MUL2, "scaling"),
    DIV2(EAEPIcon.DIV2, "scaling"),
    MUL3(EAEPIcon.MUL3, "scaling"),
    DIV3(EAEPIcon.DIV3, "scaling"),
    MUL5(EAEPIcon.MUL5, "scaling"),
    DIV5(EAEPIcon.DIV5, "scaling"),

    DOUBLING_DISABLED(EAEPIcon.PATTERN_SINGLE, "smart_doubling", "disabled"),
    DOUBLING_ENABLED(EAEPIcon.PATTERN_MULTI, "smart_doubling", "enabled"),
    BLOCKING_DISABLED_BY_SUPER(EAEPIcon.fromAEIcon(Icon.ARROW_RIGHT), "smart_blocking", "disabled_by_super"),

    BLOCKING_DISABLED(EAEPIcon.fromAEIcon(Icon.BLOCKING_MODE_NO), "smart_blocking", "disabled"),
    BLOCKING_ENABLED(EAEPIcon.BLOCKING_TRANSPARENT, "smart_blocking", "enabled"),

    TICKER_ENABLED(EAEPIcon.fromAEIcon(Icon.AUTO_EXPORT_ON), "state_ticker", "enabled"),
    TICKER_DISABLED(EAEPIcon.fromAEIcon(Icon.AUTO_EXPORT_OFF), "state_ticker", "disabled"),
    TICKER_BLACKLISTED(EAEPIcon.fromAEIcon(Icon.INVALID), "state_ticker", "blacklisted"),

    REDSTONE_IGNORE(EAEPIcon.fromAEIcon(Icon.REDSTONE_IGNORE),
            ButtonToolTips.RedstoneMode.text(), ButtonToolTips.AlwaysActive.text(), "redstone_mode"),
    REDSTONE_LOW(EAEPIcon.fromAEIcon(Icon.REDSTONE_LOW),
            ButtonToolTips.RedstoneMode.text(), ButtonToolTips.ActiveWithoutSignal.text(), "redstone_mode"),
    REDSTONE_HIGH(EAEPIcon.fromAEIcon(Icon.REDSTONE_HIGH),
            ButtonToolTips.RedstoneMode.text(), ButtonToolTips.ActiveWithSignal.text(), "redstone_mode"),

    PRIORITY_KEEP(EAEPIcon.SAVE_CENTER, "priority_tool", "keep"),
    PRIORITY_INCREMENT(EAEPIcon.SAVE_UP, "priority_tool", "increment"),
    PRIORITY_DECREMENT(EAEPIcon.SAVE_DOWN, "priority_tool", "decrement"),

    ;

    private final IButtonIcon icon;
    private final Component name;
    private final Component tooltip;
    private final String actionGroup;

    public static final Map<String, List<EAEPActionItems>> GROUPED_ACTIONS = new HashMap<>();

    static {
        for (EAEPActionItems action : EAEPActionItems.values()) {
            if (!action.actionGroup.isEmpty())
                GROUPED_ACTIONS.computeIfAbsent(action.actionGroup,
                        ignored -> new ArrayList<>()).add(action);
        }
    }

    EAEPActionItems(IButtonIcon icon, String actionGroup) {
        this(icon, Component.empty(), Component.empty(), actionGroup);
    }

    EAEPActionItems(IButtonIcon icon, String actionGroup, String additionalKey) {
        this(
                icon,
                UtilKeyBuilder.of(UtilKeyBuilder.screenTooltip)
                        .addStr(actionGroup)
                        .build(),
                UtilKeyBuilder.of(UtilKeyBuilder.screenTooltip)
                        .addStr(actionGroup)
                        .addStr(additionalKey)
                        .build(),
                actionGroup
        );
    }

    EAEPActionItems(IButtonIcon icon, Component name, Component tooltip, String actionGroup) {
        this.icon = icon;
        this.name = name;
        this.tooltip = tooltip;
        this.actionGroup = actionGroup;
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

    public boolean hasName() {
        return !name.getString().isEmpty();
    }
    public Component getName() {
        return name;
    }
    public Component getTooltip() {
        return tooltip;
    }
}
