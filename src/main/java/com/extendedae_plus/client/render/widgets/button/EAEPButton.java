package com.extendedae_plus.client.render.widgets.button;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.IconButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class EAEPButton extends IconButton {
    protected static final Pattern PATTERN_NEW_LINE = Pattern.compile("\\n", Pattern.LITERAL);

    protected float scale = 1F;
    private final int widthOriginal;
    private final int heightOriginal;

    public EAEPButton(Consumer<EAEPButton> onPress) {
        super(button -> {
            if (!(button instanceof EAEPButton eaepButton)) return;
            onPress.accept(eaepButton);
        });
        this.widthOriginal = this.width;
        this.heightOriginal = this.height;

        this.updateTooltip();
    }

    @Override
    public void onPress() {
        super.onPress();
        this.updateTooltip();
    }

    protected void updateTooltip() {
        if (this.getNonnullAction().hasName())
            this.setMessage(this.buildMessage(
                    this.getNonnullAction().getName(),
                    this.getNonnullAction().getTooltip()));
    }

    public void setScale(float scale) {
        this.scale = scale;
        this.width = (int) Math.floor(this.widthOriginal * scale);
        this.height = (int) Math.floor(this.heightOriginal * scale);
    }

    @Override
    public Rect2i getTooltipArea() {
        var area = super.getTooltipArea();
        if (this.scale == 1F) return area;
        area.setWidth((int) Math.floor(area.getWidth() * this.scale));
        area.setHeight((int) Math.floor(area.getHeight() * this.scale));
        return area;
    }

    public abstract @Nullable EAEPActionItems getAction();

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

            while (i + 30 < sb.length() && (i = sb.lastIndexOf(" ", i + 30)) != -1) {
                sb.replace(i, i + 1, "\n");
            }

            return Component.literal(name + "\n" + sb);
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        if (!this.visible) return;
        var blitter = this.getIconBlitter();
        var item = this.getItemOverlay();

        var stackPose = guiGraphics.pose();
        stackPose.pushPose();
        stackPose.translate(getX(), getY(), 0);
        stackPose.scale(this.scale, this.scale, 1.0f);
        stackPose.translate(-getX(), -getY(), 0);

        var yOffset = isHovered() ? 1 : 0;

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

        stackPose.popPose();
    }

    @Override
    public void setHalfSize(boolean halfSize) {
        // HalfSize is unsupported
    }

    @Override
    public boolean isHalfSize() {
        return false;
    }
}
