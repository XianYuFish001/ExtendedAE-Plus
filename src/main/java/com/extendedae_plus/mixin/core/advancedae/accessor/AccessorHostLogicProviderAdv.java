package com.extendedae_plus.mixin.core.advancedae.accessor;

import com.extendedae_plus.mixin.helper.HelperProviderHost;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogic;
import net.pedroksl.advanced_ae.common.logic.AdvPatternProviderLogicHost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvPatternProviderLogic.class)
public interface AccessorHostLogicProviderAdv extends HelperProviderHost {
    @Accessor("host")
    AdvPatternProviderLogicHost getHostAdv();
}
