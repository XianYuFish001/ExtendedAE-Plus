package com.extendedae_plus.client.render.widgets.button;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import com.extendedae_plus.util.UtilGetKey;
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

    ;

    private final IIcon icon;
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

    EAEPActionItems(IIcon icon, String actionGroup) {
        this(icon, Component.empty(), Component.empty(), actionGroup);
    }

    EAEPActionItems(IIcon icon, String actionGroup, String additionalKey) {
        this(
                icon,
                new UtilGetKey(UtilGetKey.screenTooltip)
                        .addStr(actionGroup)
                        .build(),
                new UtilGetKey(UtilGetKey.screenTooltip)
                        .addStr(actionGroup)
                        .addStr(additionalKey)
                        .build(),
                actionGroup
        );
    }

    EAEPActionItems(IIcon icon, Component name, Component tooltip, String actionGroup) {
        this.icon = icon;
        this.name = name;
        this.tooltip = tooltip;
        this.actionGroup = actionGroup;
    }

    public Blitter getIconBlitter() {
        if (icon != null) return icon.getBlitter();
        else return Icon.INVALID.getBlitter();
    }

    public IIcon getIcon() {
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
