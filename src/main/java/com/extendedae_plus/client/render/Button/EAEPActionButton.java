package com.extendedae_plus.client.render.Button;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.IconButton;
import com.extendedae_plus.ExtendedAEPlus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class EAEPActionButton extends IconButton {
    private static final Pattern PATTERN_NEW_LINE = Pattern.compile("\\n", Pattern.LITERAL);
    private EAEPIcon icon = null;
    private Icon aeIcon = null;

    public EAEPActionButton(EAEActionItems action, Runnable onPress) {
        this(action, a -> onPress.run());
    }

    public EAEPActionButton(EAEActionItems action, Consumer<EAEActionItems> onPress) {
        super(button -> onPress.accept(action));
        Component i18nName = null;
        Component i18nTooltip = null;
        boolean hasName = false;
        this.icon = switch (action) {
            case MULTIPLY2 -> EAEPIcon.MULTIPLY2;
            case MULTIPLY5 -> EAEPIcon.MULTIPLY5;
            case MULTIPLY10 -> EAEPIcon.MULTIPLY10;
            case DIVIDE2 -> EAEPIcon.DIVIDE2;
            case DIVIDE5 -> EAEPIcon.DIVIDE5;
            case DIVIDE10 -> EAEPIcon.DIVIDE10;
        };
        if (hasName) i18nName = Component.translatable(String.format("%s.%s.%s",
                "button", ExtendedAEPlus.MODID, action.name().toLowerCase()));
        if (i18nName != null) this.setMessage(buildMessage(i18nName, i18nTooltip));
    }

    @Override
    protected Icon getIcon() {
        return aeIcon == null ? Icon.INVALID : aeIcon;
    }

    protected EAEPIcon getEAEPIcon() {
        return icon;
    }

    private Component buildMessage(Component i18nName, @Nullable Component i18nTooltip) {
        String name = i18nName.getString();
        if (i18nTooltip == null) {
            return Component.literal(name);
        } else {
            String value = i18nTooltip.getString();
            value = PATTERN_NEW_LINE.matcher(value).replaceAll("\n");
            StringBuilder sb = new StringBuilder(value);
            int i = sb.lastIndexOf("\n");
            if (i <= 0) {
                i = 0;
            }

            while(i + 30 < sb.length() && (i = sb.lastIndexOf(" ", i + 30)) != -1) {
                sb.replace(i, i + 1, "\n");
            }

            return Component.literal(name + "\n" + sb);
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        if (this.visible) {
            var icon = this.getEAEPIcon();
            var aeIcon = this.getIcon();
            var item = this.getItemOverlay();

            if (this.isHalfSize()) {
                this.width = 8;
                this.height = 8;
            }

            var yOffset = isHovered() ? 1 : 0;

            if (this.isHalfSize()) {
                if (!isDisableBackground()) {
                    Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().dest(getX(), getY()).zOffset(10).blit(guiGraphics);
                }
                if (item != null) {
                    guiGraphics.renderItem(new ItemStack(item), getX(), getY(), 0, 20);
                } else if (icon != null) {
                    Blitter blitter = icon.getBlitter();
                    if (!this.active) blitter.opacity(0.5f);
                    blitter.dest(getX(), getY()).zOffset(20).blit(guiGraphics);
                } else {
                    Blitter blitter = aeIcon.getBlitter();
                    if (!this.active) blitter.opacity(0.5f);
                    blitter.dest(getX(), getY()).zOffset(20).blit(guiGraphics);
                }
            } else {
                if (!isDisableBackground()) {
                    Icon bgIcon = isHovered() ? Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER
                            : isFocused() ? Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS : Icon.TOOLBAR_BUTTON_BACKGROUND;

                    bgIcon.getBlitter()
                            .dest(getX() - 1, getY() + yOffset, 18, 20)
                            .zOffset(2)
                            .blit(guiGraphics);
                }
                if (item != null)
                    guiGraphics.renderItem(new ItemStack(item), getX(), getY() + 1 + yOffset, 0, 3);
                else if (icon != null)
                    icon.getBlitter().dest(getX(), getY() + 1 + yOffset).zOffset(3).blit(guiGraphics);
                else aeIcon.getBlitter().dest(getX(), getY() + 1 + yOffset).zOffset(3).blit(guiGraphics);
            }
        }
    }
}
