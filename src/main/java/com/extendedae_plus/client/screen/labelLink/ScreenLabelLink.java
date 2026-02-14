package com.extendedae_plus.client.screen.labelLink;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.core.AppEng;
import com.extendedae_plus.common.registry.menu.labelLink.MenuLabelLink;
import com.extendedae_plus.common.wireless.linkApi.Label;
import com.extendedae_plus.util.keyBuilder.Patterns;
import com.extendedae_plus.util.keyBuilder.UtilKeyBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScreenLabelLink extends AEBaseScreen<MenuLabelLink> {
    private static final int ROW_HEIGHT = 18;

    protected static final Rect2i AREA_BACKGROUND = new Rect2i(0, 0, 176, 170);
    protected static final Rect2i AREA_SCROLLBAR_BACKGROUND_RIGHT = new Rect2i(342, 0, 21, 116);
    protected static final Rect2i AREA_HIGHLIGHTED_LABEL = new Rect2i(0, 170, 160, 18);

    protected final Scrollbar scrollbar;
    protected int selectedIndex = -1;

    protected List<MenuLabelLink.LabelMapped> labelsMapped = null;
    protected List<Label.Data> labels = List.of();

    public ScreenLabelLink(MenuLabelLink menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.scrollbar = this.widgets.addScrollBar("scrollbar", Scrollbar.BIG);
        this.scrollbar.setHeight(88);
    }

    protected void update() {
        this.updateLabels();
        this.scrollbar.setRange(0, this.labels.size() - 8, 2);
    }

    protected void updateLabels() {
        this.labels = this.labelsMapped.stream()
                .sorted(Comparator.comparingInt(MenuLabelLink.LabelMapped::serial))
                .map(MenuLabelLink.LabelMapped::data)
                .toList();
        this.selectedIndex = this.labels.indexOf(this.menu.getSelectedLabel());
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        int textColor = this.style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        var indexScroll = this.scrollbar.getCurrentScroll();

        for (int indexRow = 0; indexRow < 8; indexRow++) {
            if (indexRow + indexScroll >= this.labels.size()) continue;
            var label = this.labels.get(indexRow + indexScroll);

            guiGraphics.drawString(this.font, label.getDisplayValue(), 12,
                    24 + indexRow * ROW_HEIGHT - 1, textColor, false);
        }

        var hovered = this.getHoveredLineIndex(mouseX, mouseY);
        if (hovered == -1) return;

        var text = new ArrayList<Component>();
        var label = this.labels.get(hovered);
        if (Screen.hasShiftDown())
            this.appendAdvancedTooltip(text, label);
        else this.appendTooltip(text, label);

        text.removeIf(component -> component.getString().isBlank());
        if (text.isEmpty()) return;
        guiGraphics.renderComponentTooltip(this.font, text, mouseX - offsetX, mouseY - offsetY);
    }

    private void appendAdvancedTooltip(List<Component> tooltip, Label.Data data) {
        tooltip.add(UtilKeyBuilder.of(Patterns.screenTooltip)
                .addStr("label_type")
                .addStr(data.frequency() != null, "frequency", "label")
                .build());
        tooltip.add(UtilKeyBuilder.of(Patterns.screenTooltip)
                .addStr("label_link")
                .addStr("info_label")
                .addStr(data.placer() == null, "public")
                .args(data.placerName(), data.placer() == null ? ""
                        : data.placer().toString().substring(0, 8))
                .build());
        tooltip.add(UtilKeyBuilder.of(Patterns.screenTooltip)
                .addStr("label_link")
                .addStr("label_description")
                .addStr(data.description().getString().isBlank(), "empty")
                .build()
                .append(data.description()));
    }

    private void appendTooltip(List<Component> tooltip, Label.Data data) {
        tooltip.add(data.description());
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        this.blit(guiGraphics, offsetX, offsetY, AREA_BACKGROUND);

        this.renderScrollBarBackground(guiGraphics, offsetX, offsetY);

        if (this.selectedIndex == -1) return;
        var y = ROW_HEIGHT * (this.selectedIndex - this.scrollbar.getCurrentScroll()) + 17;
        if (y > ROW_HEIGHT * 8 || y < 0) return;
        this.blit(guiGraphics, offsetX + 8, offsetY + y, AREA_HIGHLIGHTED_LABEL);
    }

    protected void renderScrollBarBackground(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        this.blit(guiGraphics, offsetX + 174, offsetY, AREA_SCROLLBAR_BACKGROUND_RIGHT);
    }

    private int getHoveredLineIndex(int x, int y) {
        x = x - leftPos - 15;
        y = y - topPos - 19;
        if (x < 0 || y < 0) {
            return -1;
        }
        if (x >= ROW_HEIGHT * 9 || y >= 8 * ROW_HEIGHT) {
            return -1;
        }

        var rowIndex = this.scrollbar.getCurrentScroll() + y / ROW_HEIGHT;
        if (rowIndex < 0 || rowIndex >= this.labels.size()) {
            return -1;
        }
        return rowIndex;
    }

    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int button) {
        var indexRow = this.getHoveredLineIndex((int) xCoord, (int) yCoord);
        if (indexRow == -1)
            return super.mouseClicked(xCoord, yCoord, button);

        if (indexRow >= this.labels.size())
            return super.mouseClicked(xCoord, yCoord, button);

        this.labelsMapped.stream()
                .filter(mapped -> mapped.data().equals(this.labels.get(indexRow)))
                .findAny()
                .map(MenuLabelLink.LabelMapped::serial)
                .ifPresent(serial -> {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                        this.menu.selectLabel(serial);
                    else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !this.menu.isLocked())
                        this.menu.unregisterLabel(serial);
                });
        return true;
    }

    protected void blit(GuiGraphics guiGraphics, int offsetX, int offsetY, Rect2i srcRect) {
        guiGraphics.blit(AppEng.makeId("textures/guis/extendedae_plus/label_link.png"),
                offsetX, offsetY,
                srcRect.getX(), srcRect.getY(),
                srcRect.getWidth(), srcRect.getHeight(),
                512, 256);
    }

    public void setLabelsMapped(List<MenuLabelLink.LabelMapped> labelsMapped) {
        this.labelsMapped = labelsMapped;
        this.update();
    }
}
