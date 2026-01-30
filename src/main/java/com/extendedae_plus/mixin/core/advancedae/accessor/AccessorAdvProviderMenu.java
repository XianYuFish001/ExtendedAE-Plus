package com.extendedae_plus.mixin.core.advancedae.accessor;

import com.extendedae_plus.mixin.bridge.HelperProviderMenu;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.gui.advpatternprovider.AdvPatternProviderMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvPatternProviderMenu.class)
public interface AccessorAdvProviderMenu extends HelperProviderMenu {
    @Accessor("logic")
    AdvPatternProviderLogic getProviderLogicAdv();
}
