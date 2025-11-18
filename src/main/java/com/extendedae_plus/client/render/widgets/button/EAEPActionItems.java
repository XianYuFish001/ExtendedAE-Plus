package com.extendedae_plus.client.render.widgets.button;

import appeng.client.gui.Icon;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum EAEPActionItems {
    MUL2(EAEPIcon.MUL2, "scaling"),
    DIV2(EAEPIcon.DIV2, "scaling"),
    MUL3(EAEPIcon.MUL3, "scaling"),
    DIV3(EAEPIcon.DIV3, "scaling"),
    MUL5(EAEPIcon.MUL5, "scaling"),
    DIV5(EAEPIcon.DIV5, "scaling");

    private final EAEPIcon icon;
    private final Icon aeIcon;
    private final Component name;
    private final Component tooltip;
    private final String actionGroup;

    public static final Map<String, List<EAEPActionItems>> GROUPED_ACTIONS = new HashMap<>();

    static {
        for (EAEPActionItems action : EAEPActionItems.values()) {
            if (!action.actionGroup.isEmpty())
                GROUPED_ACTIONS.computeIfAbsent(action.actionGroup,
                        ignore -> new ArrayList<>()).add(action);
        }
    }

    EAEPActionItems(EAEPIcon icon) {
        this(icon, null, Component.empty(), Component.empty(), "");
    }

    EAEPActionItems(EAEPIcon icon, String actionGroup) {
        this(icon, null, Component.empty(), Component.empty(), actionGroup);
    }

    EAEPActionItems(EAEPIcon icon, Icon aeIcon, Component name, Component tooltip, String actionGroup) {
        this.icon = icon;
        this.aeIcon = aeIcon;
        this.name = name;
        this.tooltip = tooltip;
        this.actionGroup = actionGroup;
    }

    public EAEPIcon getIcon() {
        return icon;
    }

    public boolean hasAEIcon() {
        return aeIcon != null;
    }
    public Icon getAEIcon() {
        return hasAEIcon() ? aeIcon : Icon.INVALID;
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
