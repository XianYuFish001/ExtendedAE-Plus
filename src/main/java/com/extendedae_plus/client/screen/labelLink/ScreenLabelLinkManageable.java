package com.extendedae_plus.client.screen.labelLink;

import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ConfirmableTextField;
import com.extendedae_plus.client.render.widgets.button.EAEPActionButton;
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems;
import com.extendedae_plus.client.render.widgets.button.EAEPCycleButton;
import com.extendedae_plus.client.render.widgets.button.EAEPServerCycleButton;
import com.extendedae_plus.common.registry.menu.labelLink.MenuLabelLink;
import com.extendedae_plus.common.wireless.linkApi.Label;
import com.extendedae_plus.integration.IntegrationFTBTeams;
import com.extendedae_plus.util.keyBuilder.Patterns;
import com.extendedae_plus.util.keyBuilder.UtilKeyBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.UUID;

public class ScreenLabelLinkManageable extends ScreenLabelLink {
    private static final Rect2i AREA_PANEL_MANAGER = new Rect2i(179, 0, 138, 47);
    private static final Rect2i AREA_SCROLLBAR_BACKGROUND_LEFT = new Rect2i(320, 0, 21, 116);
    private static final Rect2i AREA_TOOLBAR_CONTROLLER_2 = new Rect2i(179, 50, 44, 28);
    private static final Rect2i AREA_TOOLBAR_CONTROLLER_1 = new Rect2i(226, 50, 24, 28);

    private final EAEPCycleButton buttonLabelType;
    private final EAEPCycleButton buttonLabelMode;
    private final ConfirmableTextField fieldLabelValue;
    private final ConfirmableTextField fieldLabelDescription;
    private final EAEPServerCycleButton buttonLock;
    private final EAEPServerCycleButton buttonMaster;

    public ScreenLabelLinkManageable(MenuLabelLink menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.imageWidth += AREA_PANEL_MANAGER.getWidth() + 5;

        this.fieldLabelValue = this.initTextField("field_label_value");
        this.fieldLabelValue.setOnConfirm(this::onLabelRegister);
        this.fieldLabelValue.setPlaceholder(UtilKeyBuilder.of(Patterns.screen)
                .addStr("label_link")
                .addStr("label_value")
                .build());

        this.fieldLabelDescription = this.initTextField("field_label_description");
        this.fieldLabelDescription.setOnConfirm(this::onLabelRegister);
        this.fieldLabelDescription.setMaxLength(100);
        this.fieldLabelDescription.setPlaceholder(UtilKeyBuilder.of(Patterns.screen)
                .addStr("label_link")
                .addStr("label_description")
                .build());

        this.buttonLabelType = new EAEPCycleButton.Builder()
                .addPart(EAEPActionItems.labelLabel)
                .addPart(EAEPActionItems.labelFrequency)
                .build();
        this.widgets.add("button_label_type", this.buttonLabelType);

        this.buttonLabelMode = new EAEPCycleButton.Builder()
                .addPart(EAEPActionItems.labelPrivate)
                .addPart(EAEPActionItems.labelPublic)
                .build();
        this.widgets.add("button_label_mode", this.buttonLabelMode);

        this.widgets.add("button_label_add",
                new EAEPActionButton(EAEPActionItems.labelAdd,
                        $ -> this.onLabelRegister()));

        this.buttonLock = new EAEPServerCycleButton.Builder()
                .addPart(EAEPActionItems.labelUnlocked)
                .addPart(EAEPActionItems.labelLocked)
                .setTask(menu::toggleLock)
                .setSyncer(menu::isLocked)
                .build();
        this.buttonMaster = new EAEPServerCycleButton.Builder()
                .addPart(EAEPActionItems.transceiverSlave)
                .addPart(EAEPActionItems.transceiverMaster)
                .setTask(menu::toggleMaster)
                .setSyncer(menu::isMaster)
                .build();
        if (menu.isLockable())
            this.widgets.add("button_lock", this.buttonLock);
        if (menu.isMasterable())
            this.widgets.add("button_master", this.buttonMaster);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        if (this.menu.isLockable())
            this.buttonLock.updateState();
        if (this.menu.isMasterable())
            this.buttonMaster.updateState();
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        this.blit(guiGraphics, offsetX + AREA_BACKGROUND.getWidth() + 5, offsetY, AREA_PANEL_MANAGER);
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTicks);

        guiGraphics.drawString(this.font,
                UtilKeyBuilder.of(Patterns.screen)
                        .addStr("label_link")
                        .addStr("register")
                        .build(),
                offsetX + 186, offsetY + 3,
                this.style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB(),
                false);

        Rect2i areaToolbarController;
        if (this.menu.isLockable() && this.menu.isMasterable())
            areaToolbarController = AREA_TOOLBAR_CONTROLLER_2;
        else if (this.menu.isLockable() || this.menu.isMasterable())
            areaToolbarController = AREA_TOOLBAR_CONTROLLER_1;
        else areaToolbarController = null;
        if (areaToolbarController == null) return;

        this.blit(guiGraphics,
                offsetX + AREA_BACKGROUND.getWidth() + 5,
                offsetY + AREA_PANEL_MANAGER.getHeight() + 5,
                areaToolbarController);
    }

    @Override
    protected void renderScrollBarBackground(GuiGraphics guiGraphics, int offsetX, int offsetY) {
        this.blit(guiGraphics, offsetX - 19, offsetY, AREA_SCROLLBAR_BACKGROUND_LEFT);
    }

    private void onLabelRegister() {
        var value = this.fieldLabelValue.getValue().replace(" ", "");
        var description = this.fieldLabelDescription.getValue();

        UUID placer = null;
        if (this.buttonLabelMode.getAction() == EAEPActionItems.labelPrivate)
            placer = IntegrationFTBTeams.instance.getTeamUUID(this.getPlayer().getUUID())
                    .orElse(this.getPlayer().getUUID());

        Label.Data data;
        if (this.buttonLabelType.getAction() == EAEPActionItems.labelLabel) {
            data = Label.Data.of(value, placer, Component.literal(description));
        } else {
            try {
                data = Label.Data.of(
                        Long.decode(value), placer, Component.literal(description));
            } catch (NumberFormatException e) {
                this.fieldLabelValue.setValue(value.replaceAll("[^0-9]", ""));
                return;
            }
        }
        if (data.isEmpty()) return;
        this.menu.registerLabel(data);
    }

    private ConfirmableTextField initTextField(String id) {
        var fieldStyle = this.getStyle().getWidget(id);
        var field = new ConfirmableTextField(this.getStyle(),
                this.font,
                fieldStyle.getLeft() == null ? 0 : fieldStyle.getLeft(),
                fieldStyle.getTop() == null ? 0 : fieldStyle.getTop(),
                fieldStyle.getWidth(),
                fieldStyle.getHeight());
        field.setBordered(false);
        field.setMaxLength(50);
        field.setTextColor(0xFFFFFF);
        field.setSelectionColor(0xFF000080);
        field.setVisible(true);
        this.widgets.add(id, field);
        return field;
    }

    @Override
    protected boolean shouldAddToolbar() {
        return false;
    }
}
