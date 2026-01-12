package com.extendedae_plus.mixin.core.extendedae.accessor;

import appeng.client.gui.me.patternaccess.PatternContainerRecord;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com.glodblock.github.extendedae.client.gui.GuiExPatternTerminal$SlotsRow")
public interface AccessorExAccessScreenRows {
    @Accessor("container")
    PatternContainerRecord getContainer();
}