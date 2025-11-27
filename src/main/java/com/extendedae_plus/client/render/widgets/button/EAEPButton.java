package com.extendedae_plus.client.render.widgets.button;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.IconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class EAEPButton extends IconButton {
    protected static final Pattern PATTERN_NEW_LINE = Pattern.compile("\\n", Pattern.LITERAL);

    public EAEPButton(Consumer<EAEPButton> onPress) {
        super(button -> {
            if (!(button instanceof EAEPButton eaepButton)) return;
            onPress.accept(eaepButton);
        });

        this.updateTooltip();
    }

    @Override
    public void onPress() {
        super.onPress();
        this.updateTooltip();
    }

    protected void updateTooltip() {
        if (this.getNonnullAction().hasName())
            this.setMessage(buildMessage(
                    this.getNonnullAction().getName(),
                    this.getNonnullAction().getTooltip()));
    }

    public @Nullable abstract EAEPActionItems getAction();

    private EAEPActionItems getNonnullAction() {
        var action = this.getAction();
        if (action == null) return EAEPActionItems.BACKING_OUT;
        else return action;
    }

    @Override
    protected Icon getIcon() {
        return this.getNonnullAction().getAEIcon();
    }

    protected Blitter getIconBlitter() {
        return this.getNonnullAction().getIconBlitter();
    }

    protected Component buildMessage(Component i18nName, @Nullable Component i18nTooltip) {
        String name = i18nName.getString();
        if (i18nTooltip == null) {
            return Component.literal(name);
        } else {
            String value = i18nTooltip.getString();
            value = PATTERN_NEW_LINE.matcher(value).replaceAll("\n");
            StringBuilder sb = new StringBuilder(value);
            int i = Math.max(sb.lastIndexOf("\n"), 0);

            while(i + 30 < sb.length() && (i = sb.lastIndexOf(" ", i + 30)) != -1) {
                sb.replace(i, i + 1, "\n");
            }

            return Component.literal(name + "\n" + sb);
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        if (this.visible) {
            var blitter = this.getIconBlitter();
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
                } else {
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
                else blitter.dest(getX(), getY() + 1 + yOffset).zOffset(3).blit(guiGraphics);
            }
        }
    }
}
