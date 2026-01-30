package com.extendedae_plus.mixin.core.ae2.accessor;

import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.implementations.PatternProviderMenu;
import com.extendedae_plus.mixin.bridge.HelperProviderMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PatternProviderMenu.class)
public interface AccessorProviderMenu extends HelperProviderMenu {
    @Accessor("logic")
    PatternProviderLogic getProviderLogicVanilla();
}
