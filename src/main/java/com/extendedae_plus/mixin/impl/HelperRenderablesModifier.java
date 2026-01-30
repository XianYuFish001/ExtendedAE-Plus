package com.extendedae_plus.mixin.impl;

import com.extendedae_plus.mixin.core.minecraft.accessor.AccessorScreen;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

public class HelperRenderablesModifier {
    public static <T extends GuiEventListener & Renderable & NarratableEntry> void
    addRenderableWidget(Screen screen, T widget) {
        if (!(screen instanceof AccessorScreen accessor)) return;
        screen.renderables.add(widget);
        accessor.getChildren().add(widget);
        accessor.getNarratables().add(widget);
    }

    public static void removeWidget(Screen screen, GuiEventListener widget) {
        if (!(screen instanceof AccessorScreen accessor)) return;

        if (widget instanceof Renderable)
            screen.renderables.remove(widget);
        if (widget instanceof NarratableEntry)
            accessor.getNarratables().remove(widget);
        accessor.getChildren().remove(widget);
    }
}
