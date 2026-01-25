package com.extendedae_plus.client.screen;

import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.localization.GuiText;
import appeng.menu.me.items.PatternEncodingTermMenu;
import com.extendedae_plus.client.impl.AliasGetter;
import com.extendedae_plus.client.render.widgets.button.EAEPActionButton;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.common.impl.pattern.InfoProvider;
import com.extendedae_plus.network.CPacketUploadTerminalPattern;
import com.extendedae_plus.util.UtilKeyBuilder;
import guideme.document.LytRect;
import guideme.render.SimpleRenderContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ScreenProviderList<TMenu extends PatternEncodingTermMenu,
        TScreen extends PatternEncodingTermScreen<TMenu>> extends AESubScreen<TMenu, TScreen> {
    public static final String PATH_STYLE = "/screens/extendedae_plus/provider_list.json";

    private static final int GUI_WIDTH = 195;
    private static final int GUI_TOP_AND_BOTTOM_PADDING = 54;
    private static final int GUI_PADDING_X = 8;
    private static final int GUI_PADDING_Y = 6;
    private static final int GUI_HEADER_HEIGHT = 34;
    private static final int GUI_FOOTER_HEIGHT = 8;
    private static final int COLUMNS = 9;
    private static final int PATTERN_PROVIDER_NAME_MARGIN_X = 2;
    private static final int TEXT_MAX_WIDTH = 155;
    private static final int ROW_HEIGHT = 18;

    private static final Rect2i AREA_HEAD = new Rect2i(0, 0, GUI_WIDTH, GUI_HEADER_HEIGHT);
    private static final Rect2i AREA_TAIL = new Rect2i(0, 162 + 16, GUI_WIDTH, GUI_FOOTER_HEIGHT);
    private static final Rect2i AREA_SCROLLER_HEAD_PROVIDER_FOCUSED = new Rect2i(0, 34, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i AREA_SCROLLER_HEAD_PROVIDER = new Rect2i(0, 52, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i AREA_SCROLLER_PROVIDER_FOCUSED = new Rect2i(0, 70, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i AREA_SCROLLER_PROVIDER = new Rect2i(0, 88, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i AREA_SCROLLER_TAIL_PROVIDER_FOCUSED = new Rect2i(0, 106, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i AREA_SCROLLER_TAIL_PROVIDER = new Rect2i(0, 124, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i AREA_PROVIDER_FOCUSED = new Rect2i(0, 142, GUI_WIDTH, ROW_HEIGHT);
    private static final Rect2i AREA_PROVIDER = new Rect2i(0, 160, GUI_WIDTH, ROW_HEIGHT);

    private final AETextField fieldSearch;
    private final AETextField fieldAlias;
    private final Scrollbar scrollbar;

    private int visibleRows = 0;
    private int focusedRow = -1;

    private final List<AliasGetter.KeywordGroup> queries = new ArrayList<>();
    private int selectedQueryIndex;
    private String customQuery = "";
    private boolean queryRefresh;

    private final List<InfoProvider> providers;
    private final List<InfoProvider> providersFiltered = new ArrayList<>();

    public ScreenProviderList(TScreen previous,
                              List<InfoProvider> providers) {
        super(previous, PATH_STYLE);
        this.providers = providers;

        this.imageWidth = GUI_WIDTH;

        var styleTerminal = AEConfig.instance().getTerminalStyle();
        this.addToLeftToolbar(new SettingToggleButton<>(
                Settings.TERMINAL_STYLE, styleTerminal, this::toggleTerminalStyle));

        this.widgets.add("button_back",
                new TabButton(Icon.BACK,
                        this.getMenu().getHost().getMainMenuIcon().getHoverName(),
                        btn -> this.returnToParent())
        );

        this.addToLeftToolbar(new EAEPActionButton(EAEPActionItems.ALIAS_ADD,
                $ -> this.addMapping()));
        this.addToLeftToolbar(new EAEPActionButton(EAEPActionItems.ALIAS_REMOVE,
                $ -> this.removeMappings()));

        this.scrollbar = this.widgets.addScrollBar("scrollbar", Scrollbar.BIG);
        this.scrollbar.setHeight(5 * ROW_HEIGHT);

        this.queries.addAll(AliasGetter.getRecipeKeywords());
        AliasGetter.recipeKeywords.clear();
        this.selectedQueryIndex = this.queries.isEmpty() ? -1 : 0;

        this.fieldSearch = this.widgets.addTextField("field_search");
        this.fieldSearch.setMaxLength(64);
        this.fieldSearch.setResponder(value -> {
            if (value.equals(this.selectedQuery().getDescription().getString())) return;
            if (this.queries.contains(AliasGetter.KeywordGroup.literal(value))) return;
            this.queries.clear();
            this.selectedQueryIndex = -1;
            this.customQuery = value;
            this.queryRefresh = true;
        });
        this.fieldSearch.setPlaceholder(GuiText.SearchPlaceholder.text());

        this.fieldAlias = this.widgets.addTextField("field_alias");
        this.fieldAlias.setPlaceholder(UtilKeyBuilder.of(UtilKeyBuilder.screen)
                .addStr("provider_list")
                .addStr("alias")
                .build());
    }

    @Override
    protected void init() {
        this.visibleRows = Math.max(6, config.getTerminalStyle().getRows(
                (this.height - GUI_HEADER_HEIGHT - GUI_FOOTER_HEIGHT - GUI_TOP_AND_BOTTOM_PADDING) / ROW_HEIGHT));
        this.imageHeight = GUI_HEADER_HEIGHT + GUI_FOOTER_HEIGHT + this.visibleRows * ROW_HEIGHT;
        super.init();

        this.queryRefresh = true;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.queryRefresh) {
            this.queryRefresh = false;
            this.fieldSearch.setValue(this.selectedQuery().getDescription().getString());
            this.rebuildKeywordsTooltip();
            this.updateInfo();
            this.scrollbar.setRange(0, this.providersFiltered.size() - this.visibleRows, 2);
        }
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        int textColor = this.style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB();
        var indexScroll = this.scrollbar.getCurrentScroll();

        for (int indexRow = 0; indexRow < this.visibleRows; indexRow++) {
            if (indexScroll + indexRow >= this.providersFiltered.size()) continue;

            var provider = this.providersFiltered.get(indexScroll + indexRow);

            if (provider.icon() != null) {
                var renderContext = new SimpleRenderContext(LytRect.empty(), guiGraphics);
                renderContext.renderItem(
                        provider.icon().getReadOnlyStack(),
                        GUI_PADDING_X + PATTERN_PROVIDER_NAME_MARGIN_X,
                        GUI_PADDING_Y + GUI_HEADER_HEIGHT + indexRow * ROW_HEIGHT - 1,
                        8,
                        8);
            }

            var name = provider.name().copy()
                    .append(" [" + provider.availableSlots() + "]");

            var text = Language.getInstance().getVisualOrder(
                    this.font.substrByWidth(name, TEXT_MAX_WIDTH - 10));

            guiGraphics.drawString(this.font, text, GUI_PADDING_X + PATTERN_PROVIDER_NAME_MARGIN_X + 10,
                    GUI_PADDING_Y + GUI_HEADER_HEIGHT + indexRow * ROW_HEIGHT - 1, textColor, false);
        }
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        this.blit(guiGraphics, offsetX, offsetY, AREA_HEAD);

        var indexScroll = this.scrollbar.getCurrentScroll();
        var currentY = offsetY + GUI_HEADER_HEIGHT;

        this.blit(guiGraphics, offsetX, currentY + this.visibleRows * ROW_HEIGHT, AREA_TAIL);

        for (int indexRow = 0; indexRow < this.visibleRows; indexRow++) {
            this.blit(guiGraphics, offsetX, currentY, this.getRowRenderPart(indexRow, indexScroll));
            currentY += ROW_HEIGHT;
        }
    }

    private Rect2i getRowRenderPart(int indexRow, int indexScroll) {
        var flagFocused = indexRow + indexScroll == this.focusedRow;
        if (indexRow == 0)
            return flagFocused
                    ? AREA_SCROLLER_HEAD_PROVIDER_FOCUSED
                    : AREA_SCROLLER_HEAD_PROVIDER;
        else if (indexRow < 5)
            return flagFocused
                    ? AREA_SCROLLER_PROVIDER_FOCUSED
                    : AREA_SCROLLER_PROVIDER;
        else if (indexRow == 5)
            return flagFocused
                    ? AREA_SCROLLER_TAIL_PROVIDER_FOCUSED
                    : AREA_SCROLLER_TAIL_PROVIDER;
        else return flagFocused
                    ? AREA_PROVIDER_FOCUSED
                    : AREA_PROVIDER;
    }

    private void updateInfo() {
        this.providersFiltered.clear();
        this.providers.forEach(info -> {
            if (!this.selectedQuery().matches(info.name().getString(), info.i18nKey())) return;
            this.providersFiltered.add(info);
        });
        this.focusedRow = this.providersFiltered.isEmpty() ? -1 : 0;
    }

    private void select(int indexProvider) {
        var info = this.providersFiltered.get(indexProvider);
        if (info == null) return;

        PacketDistributor.sendToServer(new CPacketUploadTerminalPattern(info.serverID()));
        this.returnToParent();
    }

    private AliasGetter.KeywordGroup selectedQuery() {
        if (this.queryIndexValid())
            return this.queries.get(this.selectedQueryIndex);
        return AliasGetter.KeywordGroup.literal(this.customQuery);
    }

    private void toggleTerminalStyle(SettingToggleButton<TerminalStyle> button, boolean backwards) {
        var next = button.getNextValue(backwards);
        AEConfig.instance().setTerminalStyle(next);
        button.set(next);
        this.reinitialize();
    }

    private void reinitialize() {
        this.children().removeAll(this.renderables);
        this.renderables.clear();
        this.init();
    }

    private void rebuildKeywordsTooltip() {
        if (this.queries.size() <= 1) {
            this.fieldSearch.setTooltip(Tooltip.create(Component.empty()));
            return;
        }

        MutableComponent candidateQuery = UtilKeyBuilder.of(UtilKeyBuilder.screenTooltip)
                .addStr("provider_list")
                .addStr("candidate_keywords")
                .build();
        for (int i = 0; i < this.queries.size(); i++) {
            var group = this.queries.get(i);
            if (i == selectedQueryIndex) candidateQuery
                    .append(Component.literal("\n→ ").withStyle(ChatFormatting.GREEN))
                    .append(group.getDescription());
            else candidateQuery.append("\n").append(group.getDescription().copy().withStyle(ChatFormatting.GRAY));
        }
        this.fieldSearch.setTooltip(Tooltip.create(candidateQuery));
    }

    private int getHoveredLineIndex(double x, double y) {
        x = x - leftPos - GUI_PADDING_X;
        y = y - topPos - GUI_HEADER_HEIGHT;
        if (x < 0 || y < 0) {
            return -1;
        }
        if (x >= ROW_HEIGHT * COLUMNS || y >= visibleRows * ROW_HEIGHT) {
            return -1;
        }

        var rowIndex = this.scrollbar.getCurrentScroll() + y / ROW_HEIGHT;
        if (rowIndex < 0 || rowIndex >= this.providersFiltered.size()) {
            return -1;
        }
        return (int) rowIndex;
    }

    private void blit(GuiGraphics guiGraphics, int offsetX, int offsetY, Rect2i srcRect) {
        var texture = AppEng.makeId("textures/guis/extendedae_plus/provider_list.png");
        guiGraphics.blit(texture, offsetX, offsetY, srcRect.getX(), srcRect.getY(), srcRect.getWidth(),
                srcRect.getHeight());
    }

    private boolean queryIndexValid() {
        return this.selectedQueryIndex >= 0
                && this.selectedQueryIndex < this.queries.size();
    }

    private void addMapping() {
        var selectedQuery = this.selectedQuery();
        var searchKey = selectedQuery.getDescription().getString();
        var aliasToSet = this.fieldAlias.getValue().trim();
        var player = Minecraft.getInstance().player;

        if (selectedQuery.isEmpty() || aliasToSet.isEmpty()) {
            if (player != null) player.displayClientMessage(
                    UtilKeyBuilder.of(UtilKeyBuilder.message)
                            .addStr("provider_list")
                            .addStr("add_alias")
                            .addStr(selectedQuery.isEmpty(), "empty_query", "empty_alias")
                            .build(),
                    false);
            return;
        }

        if (AliasGetter.addOrUpdateAlias(searchKey, aliasToSet)) {
            if (player != null) player.displayClientMessage(
                    UtilKeyBuilder.of(UtilKeyBuilder.message)
                            .addStr("provider_list")
                            .addStr("add_alias")
                            .addStr("success")
                            .args(searchKey, aliasToSet)
                            .build(),
                    false);

            // 将刚添加的中文名写入搜索框，作为当前查询
            this.queries.clear();

            var newAliasGroup = AliasGetter.KeywordGroup.literal(aliasToSet);
            this.queries.addFirst(newAliasGroup);
            this.selectedQueryIndex = 0;

            this.fieldSearch.setValue(aliasToSet);
            this.queryRefresh = true;
        } else {
            if (player != null) player.displayClientMessage(
                    UtilKeyBuilder.of(UtilKeyBuilder.message)
                            .addStr("provider_list")
                            .addStr("add_alias")
                            .addStr("failed")
                            .args(aliasToSet)
                            .build(),
                    false);
        }
    }

    private void removeMappings() {
        var aliasToDelete = this.fieldAlias.getValue();
        var player = Minecraft.getInstance().player;
        if (aliasToDelete.isEmpty()) {
            if (player != null) player.displayClientMessage(
                    UtilKeyBuilder.of(UtilKeyBuilder.message)
                            .addStr("provider_list")
                            .addStr("delete_alias")
                            .addStr("empty_alias")
                            .build(),
                    false);
            this.fieldAlias.setValue(this.fieldSearch.getValue());
            return;
        }
        int removed = AliasGetter.removeAliases(aliasToDelete);
        if (removed > 0) {
            if (player != null) player.displayClientMessage(
                    UtilKeyBuilder.of(UtilKeyBuilder.message)
                            .addStr("provider_list")
                            .addStr("delete_alias")
                            .addStr("success")
                            .args(removed, aliasToDelete)
                            .build(),
                    false);
            this.queryRefresh = true;
            this.queries.clear();
        } else {
            if (player != null) player.displayClientMessage(
                    UtilKeyBuilder.of(UtilKeyBuilder.message)
                            .addStr("provider_list")
                            .addStr("delete_alias")
                            .addStr("failed")
                            .args(aliasToDelete)
                            .build(),
                    false);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.focusedRow >= 0 && (keyCode == GLFW.GLFW_KEY_ENTER
                || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            this.select(this.focusedRow);
            return true;
        }

        if (this.providersFiltered.isEmpty())
            return super.keyPressed(keyCode, scanCode, modifiers);

        var direction = 0;
        if (keyCode == GLFW.GLFW_KEY_UP)
            direction = -1;
        else if (keyCode == GLFW.GLFW_KEY_DOWN)
            direction = 1;
        else return super.keyPressed(keyCode, scanCode, modifiers);

        var indexScroll = this.scrollbar.getCurrentScroll();
        if ((this.focusedRow == this.visibleRows + indexScroll - 1 && direction == 1)
                || (this.focusedRow == indexScroll && direction == -1))
            this.scrollbar.setCurrentScroll(indexScroll + direction);
        this.focusedRow = Math.clamp(this.focusedRow + direction, 0, this.providersFiltered.size() - 1);
        return true;
    }

    @Override
    public boolean mouseClicked(double xCoord, double yCoord, int button) {
        if (button == 1) {
            if (this.fieldSearch.isMouseOver(xCoord, yCoord)) {
                this.fieldSearch.setValue("");
            } else if (this.fieldAlias.isMouseOver(xCoord, yCoord)) {
                this.fieldAlias.setValue("");
            }
        } else if (button == 0) {
            var indexProvider = this.getHoveredLineIndex(xCoord, yCoord);
            if (indexProvider >= 0) {
                this.focusedRow = indexProvider + this.scrollbar.getCurrentScroll();
                return true;
            }
        }

        return super.mouseClicked(xCoord, yCoord, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            var indexProvider = this.getHoveredLineIndex(mouseX, mouseY);
            if (indexProvider >= 0 && this.focusedRow == indexProvider + this.scrollbar.getCurrentScroll()) {
                this.select(indexProvider);
                return true;
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double deltaX, double deltaY) {
        if (this.queries.size() <= 1 || this.fieldSearch == null)
            return super.mouseScrolled(x, y, deltaX, deltaY);
        if (Minecraft.getInstance().player != null)
            Minecraft.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.1F, 1.0F);

        boolean reverse = deltaY > 0;

        if (this.fieldSearch.isMouseOver(x, y) && this.queryIndexValid()) {
            if (this.selectedQueryIndex == -1) this.selectedQueryIndex = 0;

            if (reverse && this.selectedQueryIndex == 0) this.selectedQueryIndex = this.queries.size() - 1;
            else if (reverse) this.selectedQueryIndex--;
            else if (this.selectedQueryIndex == this.queries.size() - 1) this.selectedQueryIndex = 0;
            else this.selectedQueryIndex++;

            this.queryRefresh = true;
            return true;
        }

        return super.mouseScrolled(x, y, deltaX, deltaY);
    }

    @Override
    protected void changeFocus(ComponentPath path) {
        super.changeFocus(path);
        this.focusedRow = -1;
    }

    @Override
    public void onClose() {
        this.returnToParent();
    }
}
