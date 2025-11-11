package com.extendedae_plus.client.render.Button;

import appeng.client.gui.Icon;
import net.minecraft.network.chat.Component;

public enum EAEPActionItems {
    MUL2(EAEPIcon.MUL2, null, Component.literal("哈哈哈"), Component.translatable("emi.align.horizontal.left")),
    MUL5(EAEPIcon.MUL5),
    MUL10(EAEPIcon.MUL10),
    DIV2(EAEPIcon.DIV2),
    DIV5(EAEPIcon.DIV5),
    DIV10(EAEPIcon.DIV10);

    private final EAEPIcon icon;
    private final Icon aeIcon;
    private final Component name;
    private final Component tooltip;

    EAEPActionItems(EAEPIcon icon) {
        this(icon, null, Component.empty(), Component.empty());
    }

    EAEPActionItems(EAEPIcon icon, Icon aeIcon, Component name, Component tooltip) {
        this.icon = icon;
        this.aeIcon = aeIcon;
        this.name = name;
        this.tooltip = tooltip;
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
