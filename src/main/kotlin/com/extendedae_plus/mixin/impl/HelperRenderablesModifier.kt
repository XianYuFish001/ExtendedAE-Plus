package com.extendedae_plus.mixin.impl

import com.extendedae_plus.mixin.core.minecraft.accessor.AccessorScreen
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.screens.Screen

object HelperRenderablesModifier {
    fun <T> addRenderableWidget(
        screen: Screen,
        widget: T
    ) where T : GuiEventListener,
            T : Renderable,
            T : NarratableEntry {
        if (screen !is AccessorScreen) return
        screen.renderables.add(widget)
        screen.children.add(widget)
        screen.narratables.add(widget)
    }

    fun removeWidget(screen: Screen, widget: GuiEventListener) {
        if (screen !is AccessorScreen) return

        if (widget is Renderable) screen.renderables.remove(widget)
        if (widget is NarratableEntry) screen.narratables.remove(widget)
        screen.children().remove(widget)
    }
}
