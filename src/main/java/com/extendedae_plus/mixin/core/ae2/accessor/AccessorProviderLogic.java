package com.extendedae_plus.mixin.core.ae2.accessor;

import appeng.api.networking.IManagedGridNode;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PatternProviderLogic.class)
public interface AccessorProviderLogic {
    @Accessor("host")
    PatternProviderLogicHost getHost();

    @Accessor("mainNode")
    IManagedGridNode getGridNode();
}
