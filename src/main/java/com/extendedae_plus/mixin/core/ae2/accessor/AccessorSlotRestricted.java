package com.extendedae_plus.mixin.core.ae2.accessor;

import appeng.menu.slot.RestrictedInputSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RestrictedInputSlot.class)
public interface AccessorSlotRestricted {
    @Accessor("which")
    RestrictedInputSlot.PlacableItemType getTypePlacable();
}
