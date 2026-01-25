package com.extendedae_plus.mixin.core.ae2.accessor;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.WidgetStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ScreenStyle.class)
public interface AccessorStyleWidgets {
    @Accessor("widgets")
    Map<String, WidgetStyle> getWidgets();
}
