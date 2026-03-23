@file:Suppress("FunctionName")

package com.extendedae_plus.mixin.helper

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.inventory.Slot
import java.util.function.Consumer

interface HelperPatternHighlightable {
    fun `eaep$getSlots`(): List<Slot>

    fun `eaep$drawHighlights`(drawer: Consumer<GuiGraphics>)

    companion object {
        val HelperPatternHighlightable.slots
            get() = this.`eaep$getSlots`()

        fun HelperPatternHighlightable.drawHighlights(drawer: Consumer<GuiGraphics>) =
            this.`eaep$drawHighlights`(drawer)
    }
}