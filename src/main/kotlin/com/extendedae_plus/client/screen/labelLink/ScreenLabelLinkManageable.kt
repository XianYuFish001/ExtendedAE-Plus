package com.extendedae_plus.client.screen.labelLink

import appeng.client.gui.style.PaletteColor
import appeng.client.gui.style.ScreenStyle
import appeng.client.gui.widgets.ConfirmableTextField
import com.extendedae_plus.client.render.widgets.button.EAEPActionButton
import com.extendedae_plus.client.render.widgets.button.EAEPActionItems
import com.extendedae_plus.client.render.widgets.button.EAEPCycleButton
import com.extendedae_plus.client.render.widgets.button.EAEPServerCycleButton
import com.extendedae_plus.common.registry.menu.labelLink.MenuLabelLink
import com.extendedae_plus.common.wireless.linkApi.Label
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.keyBuilder.Patterns
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.Rect2i
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import java.util.*

class ScreenLabelLinkManageable(
    menu: MenuLabelLink, playerInventory: Inventory, title: Component, style: ScreenStyle
) : ScreenLabelLink(menu, playerInventory, title, style) {
    private val buttonLabelType: EAEPCycleButton
    private val buttonLabelMode: EAEPCycleButton
    private val fieldLabelValue: ConfirmableTextField
    private val fieldLabelDescription: ConfirmableTextField
    private val buttonLock: EAEPServerCycleButton
    private val buttonMaster: EAEPServerCycleButton

    init {
        this.imageWidth += AREA_PANEL_MANAGER.width + 5

        this.fieldLabelValue = this.initTextField("field_label_value")
        this.fieldLabelValue.setOnConfirm(this::onLabelRegister)
        this.fieldLabelValue.placeholder = UtilKeyBuilder.of(Patterns.Screen)
            .addStr("label_link")
            .addStr("label_value")
            .build()

        this.fieldLabelDescription = this.initTextField("field_label_description")
        this.fieldLabelDescription.setOnConfirm(this::onLabelRegister)
        this.fieldLabelDescription.setMaxLength(100)
        this.fieldLabelDescription.placeholder = UtilKeyBuilder.of(Patterns.Screen)
            .addStr("label_link")
            .addStr("label_description")
            .build()

        this.buttonLabelType = EAEPCycleButton.Builder()
            .addPart(EAEPActionItems.LabelLabel)
            .addPart(EAEPActionItems.LabelFrequency)
            .build()
        this.widgets.add("button_label_type", this.buttonLabelType)

        this.buttonLabelMode = EAEPCycleButton.Builder()
            .addPart(EAEPActionItems.LabelPrivate)
            .addPart(EAEPActionItems.LabelPublic)
            .build()
        this.widgets.add("button_label_mode", this.buttonLabelMode)

        this.widgets.add(
            "button_label_add",
            EAEPActionButton(
                EAEPActionItems.LabelAdd
            ) { this.onLabelRegister() })

        this.buttonLock = EAEPServerCycleButton.Builder()
            .addPart(EAEPActionItems.LabelUnlocked)
            .addPart(EAEPActionItems.LabelLocked)
            .setTask(menu::toggleLock)
            .setSyncer(menu::isLocked)
            .build()
        this.buttonMaster = EAEPServerCycleButton.Builder()
            .addPart(EAEPActionItems.TransceiverSlave)
            .addPart(EAEPActionItems.TransceiverMaster)
            .setTask(menu::toggleMaster)
            .setSyncer(menu::isMaster)
            .build()
        if (menu.isLockable) this.widgets.add("button_lock", this.buttonLock)
        if (menu.isMasterable) this.widgets.add("button_master", this.buttonMaster)
    }

    override fun updateBeforeRender() {
        super.updateBeforeRender()
        if (this.menu.isLockable) this.buttonLock.updateState()
        if (this.menu.isMasterable) this.buttonMaster.updateState()
    }

    override fun drawBG(
        guiGraphics: GuiGraphics,
        offsetX: Int,
        offsetY: Int,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        this.blit(guiGraphics, offsetX + AREA_BACKGROUND.width + 5, offsetY, AREA_PANEL_MANAGER)
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTicks)

        guiGraphics.drawString(
            this.font,
            UtilKeyBuilder.of(Patterns.Screen)
                .addStr("label_link")
                .addStr("register")
                .build(),
            offsetX + 186, offsetY + 3,
            this.style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB(),
            false
        )

        val areaToolbarController = when {
            this.menu.isLockable && this.menu.isMasterable -> AREA_TOOLBAR_CONTROLLER_2
            this.menu.isLockable || this.menu.isMasterable -> AREA_TOOLBAR_CONTROLLER_1
            else -> return
        }

        this.blit(
            guiGraphics,
            offsetX + AREA_BACKGROUND.width + 5,
            offsetY + AREA_PANEL_MANAGER.height + 5,
            areaToolbarController
        )
    }

    override fun renderScrollBarBackground(guiGraphics: GuiGraphics, offsetX: Int, offsetY: Int) =
        this.blit(guiGraphics, offsetX - 19, offsetY, AREA_SCROLLBAR_BACKGROUND_LEFT)

    private fun onLabelRegister() {
        val value = this.fieldLabelValue.value.replace(" ", "")
        val description = this.fieldLabelDescription.value

        var placer: UUID? = null
        if (this.buttonLabelMode.action == EAEPActionItems.LabelPrivate)
            placer = this.player.uuid

        val data = if (this.buttonLabelType.action == EAEPActionItems.LabelLabel) {
            Label.Data.of(value, placer, Component.literal(description))
        } else {
            try {
                Label.Data.of(
                    value.toLong(), placer, Component.literal(description)
                )
            } catch (_: NumberFormatException) {
                this.fieldLabelValue.value = value.replace("[^0-9]".toRegex(), "")
                return
            }
        }
        if (data.isEmpty) return
        this.menu.registerLabel(data)
    }

    private fun initTextField(id: String): ConfirmableTextField {
        val fieldStyle = this.style.getWidget(id)
        val field = ConfirmableTextField(
            this.style,
            this.font,
            fieldStyle.left ?: 0,
            fieldStyle.top ?: 0,
            fieldStyle.width,
            fieldStyle.height
        )
        field.isBordered = false
        field.setMaxLength(50)
        field.setTextColor(0xFFFFFF)
        field.setSelectionColor(-0xffff80)
        field.isVisible = true
        this.widgets.add(id, field)
        return field
    }

    override fun shouldAddToolbar() = false

    companion object {
        private val AREA_PANEL_MANAGER = Rect2i(179, 0, 138, 47)
        private val AREA_SCROLLBAR_BACKGROUND_LEFT = Rect2i(320, 0, 21, 116)
        private val AREA_TOOLBAR_CONTROLLER_2 = Rect2i(179, 50, 44, 28)
        private val AREA_TOOLBAR_CONTROLLER_1 = Rect2i(226, 50, 24, 28)
    }
}
