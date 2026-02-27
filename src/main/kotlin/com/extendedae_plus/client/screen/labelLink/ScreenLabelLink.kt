package com.extendedae_plus.client.screen.labelLink

import appeng.client.gui.AEBaseScreen
import appeng.client.gui.style.PaletteColor
import appeng.client.gui.style.ScreenStyle
import appeng.client.gui.widgets.Scrollbar
import appeng.core.AppEng
import com.extendedae_plus.common.registry.menu.labelLink.MenuLabelLink
import com.extendedae_plus.common.registry.menu.labelLink.MenuLabelLink.LabelMapped
import com.extendedae_plus.common.wireless.linkApi.Label
import com.extendedae_plus.util.UtilKeyBuilder
import com.fish.fishlib.util.extension.unit
import com.fish.fishlib.util.keyBuilder.Patterns
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.Rect2i
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import org.lwjgl.glfw.GLFW

open class ScreenLabelLink(
    menu: MenuLabelLink,
    playerInventory: Inventory,
    title: Component,
    style: ScreenStyle
) : AEBaseScreen<MenuLabelLink>(menu, playerInventory, title, style) {
    protected val scrollbar: Scrollbar = this.widgets.addScrollBar("scrollbar", Scrollbar.BIG)
    protected var selectedIndex = -1

    protected lateinit var labelsMapped: MutableList<LabelMapped>
    protected var labels = ArrayList<Label.Data>()

    init {
        this.scrollbar.setHeight(88)
    }

    protected fun update() {
        this.updateLabels()
        this.scrollbar.setRange(0, this.labels.size - 8, 2)
    }

    protected fun updateLabels() {
        this.labels = labelsMapped
            .sortedWith(Comparator.comparingInt(LabelMapped::serial))
            .mapNotNullTo(ArrayList(), LabelMapped::data)
        this.selectedIndex = this.labels.indexOf(this.menu.selectedLabel)
    }

    override fun drawFG(guiGraphics: GuiGraphics, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        val textColor = this.style.getColor(PaletteColor.DEFAULT_TEXT_COLOR).toARGB()
        val indexScroll = this.scrollbar.currentScroll

        for (indexRow in 0..7) {
            if (indexRow + indexScroll >= this.labels.size) continue
            val label = this.labels[indexRow + indexScroll]

            guiGraphics.drawString(
                this.font, label.displayValue, 12,
                24 + indexRow * ROW_HEIGHT - 1, textColor, false
            )
        }

        val hovered = this.getHoveredLineIndex(mouseX, mouseY)
        if (hovered == -1) return

        val text = ArrayList<Component>()
        val label = this.labels[hovered]
        if (hasShiftDown()) this.appendAdvancedTooltip(text, label)
        else this.appendTooltip(text, label)

        text.removeIf { it.string.isBlank() }
        if (text.isEmpty()) return
        guiGraphics.renderComponentTooltip(this.font, text, mouseX - offsetX, mouseY - offsetY)
    }

    private fun appendAdvancedTooltip(tooltip: MutableList<Component>, data: Label.Data) =
        UtilKeyBuilder.of(Patterns.ScreenTooltip)
            .addStr("label_type")
            .bindCollection(tooltip)
            .addStr(data.frequency != null, "frequency", "label")
            .buildInto()
            .addStr("info_label")
            .addStr(data.placer != null, "public")
            .args(data.placerName, data.placer?.toString()?.substring(0, 8) ?: "")
            .buildInto()
            .addStr("label_description")
            .addStr(data.description().string.isBlank(), "empty")
            .buildInto { it.append(data.description()) }
            .unit()

    private fun appendTooltip(tooltip: MutableList<Component>, data: Label.Data) =
        tooltip.add(data.description()).unit()

    override fun drawBG(
        guiGraphics: GuiGraphics,
        offsetX: Int,
        offsetY: Int,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        this.blit(guiGraphics, offsetX, offsetY, AREA_BACKGROUND)

        this.renderScrollBarBackground(guiGraphics, offsetX, offsetY)

        if (this.selectedIndex == -1) return
        val y: Int = ROW_HEIGHT * (this.selectedIndex - this.scrollbar.currentScroll) + 17
        if (y > ROW_HEIGHT * 8 || y < 0) return
        this.blit(guiGraphics, offsetX + 8, offsetY + y, AREA_HIGHLIGHTED_LABEL)
    }

    protected open fun renderScrollBarBackground(guiGraphics: GuiGraphics, offsetX: Int, offsetY: Int) =
        this.blit(guiGraphics, offsetX + 174, offsetY, AREA_SCROLLBAR_BACKGROUND_RIGHT)

    private fun getHoveredLineIndex(x: Int, y: Int): Int {
        var x = x
        var y = y
        x = x - leftPos - 15
        y = y - topPos - 19
        if (x < 0 || y < 0) {
            return -1
        }
        if (x >= ROW_HEIGHT * 9 || y >= 8 * ROW_HEIGHT) {
            return -1
        }

        val rowIndex: Int = this.scrollbar.currentScroll + y / ROW_HEIGHT
        if (rowIndex < 0 || rowIndex >= this.labels.size) {
            return -1
        }
        return rowIndex
    }

    override fun mouseClicked(xCoord: Double, yCoord: Double, button: Int): Boolean {
        val indexRow = this.getHoveredLineIndex(xCoord.toInt(), yCoord.toInt())
        if (indexRow == -1) return super.mouseClicked(xCoord, yCoord, button)

        if (indexRow >= this.labels.size) return super.mouseClicked(xCoord, yCoord, button)

        this.labelsMapped
            .find { it.data == this.labels[indexRow] }
            ?.serial?.let {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                    this.menu.selectLabel(it)
                else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && !this.menu.isLocked)
                    this.menu.unregisterLabel(it)
            }
        return true
    }

    protected fun blit(guiGraphics: GuiGraphics, offsetX: Int, offsetY: Int, srcRect: Rect2i) {
        guiGraphics.blit(
            AppEng.makeId("textures/guis/extendedae_plus/label_link.png"),
            offsetX, offsetY,
            srcRect.x.toFloat(), srcRect.y.toFloat(),
            srcRect.width, srcRect.height,
            512, 256
        )
    }

    fun setLabels(labelsMapped: MutableList<LabelMapped>) {
        this.labelsMapped = labelsMapped
        this.update()
    }

    companion object {
        private const val ROW_HEIGHT = 18

        @JvmField
        protected val AREA_BACKGROUND: Rect2i = Rect2i(0, 0, 176, 170)
        protected val AREA_SCROLLBAR_BACKGROUND_RIGHT: Rect2i = Rect2i(342, 0, 21, 116)
        protected val AREA_HIGHLIGHTED_LABEL: Rect2i = Rect2i(0, 170, 160, 18)
    }
}
