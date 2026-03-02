package com.extendedae_plus.client.render.widgets.button

import appeng.client.gui.Icon
import appeng.client.gui.widgets.IconButton
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.Rect2i
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import java.util.regex.Pattern
import kotlin.math.max

abstract class EAEPButton(onPress: (EAEPButton) -> Unit) : IconButton(onPress@{
    onPress(it as? EAEPButton ?: return@onPress)
}) {
    var scale = 1f
        set(value) {
            field = value
            this.width = (this.widthOriginal * value).toInt()
            this.height = (this.heightOriginal * value).toInt()
        }
    private val widthOriginal = this.width
    private val heightOriginal = this.height

    override fun onPress() {
        super.onPress()
        this.updateTooltip()
    }

    protected fun updateTooltip() {
        val action = this.nonnullAction
        if (action.text.string.isEmpty()) return
        this.message = this.buildMessage(action.text, action.tooltip)
    }

    override fun getTooltipArea(): Rect2i {
        val area = super.getTooltipArea()
        if (this.scale == 1f) return area
        area.width = (this.widthOriginal * this.scale).toInt()
        area.height = (this.heightOriginal * this.scale).toInt()
        return area
    }

    abstract val action: EAEPActionItems?

    private val nonnullAction
        get() = this.action ?: EAEPActionItems.BackingOut

    override fun getIcon() = this.nonnullAction.aeIcon

    protected val iconBlitter
        get() = this.nonnullAction.iconBlitter

    protected fun buildMessage(i18nName: Component, i18nTooltip: Component?): Component {
        val name = i18nName.string
        if (i18nTooltip == null) {
            return Component.literal(name)
        } else {
            var value = i18nTooltip.string
            value = PATTERN_NEW_LINE.matcher(value).replaceAll("\n")
            val sb = StringBuilder(value)
            var i = max(sb.lastIndexOf("\n"), 0)

            while (i + 30 < sb.length && (sb.lastIndexOf(" ", i + 30).also { i = it }) != -1) {
                sb.replace(i, i + 1, "\n")
            }

            return Component.literal(name + "\n" + sb)
        }
    }

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partial: Float) {
        if (!this.visible) return
        val blitter = this.iconBlitter
        val item = this.itemOverlay

        val stackPose = guiGraphics.pose()
        stackPose.pushPose()
        stackPose.translate(this.x.toFloat(), this.y.toFloat(), 0f)
        stackPose.scale(this.scale, this.scale, 1.0f)
        stackPose.translate(-this.x.toFloat(), -this.y.toFloat(), 0f)

        val yOffset = if (isHovered()) 1 else 0

        if (!this.isDisableBackground) {
            val bgIcon = if (isHovered())
                Icon.TOOLBAR_BUTTON_BACKGROUND_HOVER
            else
                if (this.isFocused)
                    Icon.TOOLBAR_BUTTON_BACKGROUND_FOCUS
                else
                    Icon.TOOLBAR_BUTTON_BACKGROUND

            bgIcon.blitter
                .dest(this.x - 1, this.y + yOffset, 18, 20)
                .zOffset(2)
                .blit(guiGraphics)
        }
        if (item != null) guiGraphics.renderItem(ItemStack(item), this.x, this.y + 1 + yOffset, 0, 3)
        else blitter.dest(this.x, this.y + 1 + yOffset).zOffset(3).blit(guiGraphics)

        stackPose.popPose()
    }

    override fun setHalfSize(halfSize: Boolean) {
        // HalfSize is unsupported
    }

    override fun isHalfSize() = false

    companion object {
        protected val PATTERN_NEW_LINE: Pattern = Pattern.compile("\\n", Pattern.LITERAL)
    }
}
