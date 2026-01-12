package com.extendedae_plus.mixin.core.extendedae.accessor;

import appeng.client.gui.me.patternaccess.PatternContainerRecord;
import appeng.client.gui.widgets.AETextField;
import com.glodblock.github.extendedae.client.gui.GuiExPatternTerminal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;

@Mixin(GuiExPatternTerminal.class)
public interface AccessorExAccessScreen {
    @Accessor("searchField")
    AETextField getSearchField();

    @Accessor("infoMap")
    HashMap<Long, GuiExPatternTerminal.PatternProviderInfo> getInfoMap();

    @Accessor("byId")
    HashMap<Long, PatternContainerRecord> getIDMap();
}