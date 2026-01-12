package com.extendedae_plus.mixin.core.ae2.accessor;

import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.RestrictedInputSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PatternEncodingTermMenu.class)
public interface AccessorEncodingMenu {
    @Accessor("encodedPatternSlot")
    RestrictedInputSlot getSlotEncoded();

    @Accessor("blankPatternSlot")
    RestrictedInputSlot getSlotBlank();
}
