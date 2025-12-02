package com.extendedae_plus.mixin.core.extendedae.accessor;

import appeng.client.gui.me.patternaccess.PatternContainerRecord;
import appeng.client.gui.widgets.AETextField;
import com.glodblock.github.extendedae.client.gui.GuiExPatternTerminal;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
@Mixin(value = GuiExPatternTerminal.class, remap = false)
public interface GuiExPatternTerminalAccessor {
    @Accessor("searchField")
    AETextField getSearchField();

    @Accessor("infoMap")
    HashMap<Long, GuiExPatternTerminal.PatternProviderInfo> getInfoMap();

    @Accessor("byId")
    HashMap<Long, PatternContainerRecord> getIDMap();
}