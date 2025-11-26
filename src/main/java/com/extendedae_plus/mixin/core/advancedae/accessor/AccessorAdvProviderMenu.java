package com.extendedae_plus.mixin.core.advancedae.accessor;

import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvPatternProviderMenu.class)
public interface AccessorAdvProviderMenu {
    @Accessor(value = "logic", remap = false)
    AdvPatternProviderLogic eaep$getProviderLogic();
}
